package org.transitclock.integration_tests;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConfiguredTestsRunner {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguredTestsRunner.class);
    private static final String DEFAULT_MVN_CMD = "mvn";
    private static final String CONFIG_FILE = "src/test/resources/tests/configuredTests.xml";
    private static final String DEFAULT_HSQL_CONFIG_FILE = "classpath:transitclockConfigHsql.xml";
    private static final String OUTPUT_DIRECTORY = "reports";
    private static final String RESULT_DIRECTORY = "target/classes/reports";
    private static String defaultTestClass = "org.transitclock.integration_tests.prediction.EnvironmentBasedPredictionAccuracyIntegrationTestImpl";

    private static String bucketName = "camsys-met-integration";
    private static String keyPrefix = "tests/";

    @Test
    public void runTests() throws Exception {

        // Download resources for configured tests
        File configFile = syncS3ToDisk();

        // Load in config file
        Document testsDoc = getConfiguredTests(configFile);
        NodeList configuredTests = testsDoc.getElementsByTagName("test");
        String runId = getRunId();
        String resultsDirectory = getResultsDirectory(runId);

        // Run tests
        try {

            for (int i = 0; i < configuredTests.getLength(); i++) {
                Element testNode = (Element) configuredTests.item(i);
                IntegrationTestEnvironment ite = createEnvironment(testNode, runId, resultsDirectory);
                logger.info("running test {}", ite.getName());
                IntegrationTestResult itr = forkTestClass(defaultTestClass, ite);
                logger.info("back from test {} with rc={}", ite.getName(), itr.getReturnCode());
                logger.info("finished loop iteration {}", i);
            }

        } catch (Throwable e) {
            logger.error("test setup failure: {}", e, e);
        } finally {
            logger.info("in finally with resultsDirectory=" + resultsDirectory);
            pushResultsBackToS3(resultsDirectory);
        }
        logger.error("exiting for cleanup!");
    }

    private static void pushResultsBackToS3(String resultsDirectory) throws InterruptedException {
        if (System.getProperty("test.s3.skipSync") != null) {
            TransferManager tm = getTransferManager();
            // put transitime/transitclockIntegration/target/classes/reports/* to
            // s3://<bucket>/results/YYYY-MM-DDTHH:MM:SS/
            File directory = new File(RESULT_DIRECTORY);
            String keyPrefix = OUTPUT_DIRECTORY;
            MultipleFileUpload x = tm.uploadDirectory(bucketName, keyPrefix, directory, true);
            logger.info("uploading results to S3 at s3://{}/{}", bucketName, keyPrefix);
            x.waitForCompletion();
            tm.shutdownNow();
            logger.info("uploading complete to S3 at s3://{}/{}", bucketName, keyPrefix);
        } else {
            logger.info("not uploading results as configuration says not to!");
        }
    }

    private static String getRunId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.format(new Date());
    }
    private static String getResultsDirectory(String runId) {
        return RESULT_DIRECTORY + "/" + runId;
    }

    // this is a bit strange:  but here we recusrively invoke mvn
    // to execute the EnvironmentBasedPredictionAccuracyIntegrationTestImpl class
    private IntegrationTestResult forkTestClass(String testClass, IntegrationTestEnvironment environment) throws IOException, InterruptedException {
        List<String> cmdAndArgs = new ArrayList<>();
        cmdAndArgs.add(DEFAULT_MVN_CMD);
        cmdAndArgs.add("test");
        cmdAndArgs.add("-Dsurefie.failOnFlakeCount=0");
        cmdAndArgs.add("-DreuseForks=false");
        cmdAndArgs.add("-Dsurefire.useSystemClassLoader=false");
        cmdAndArgs.add("-Dlogback.configurationFile=logbackIntegration.xml");
        cmdAndArgs.add("-Dtest=" + testClass);
        addToCommandLine(cmdAndArgs, environment);
        String prettyPrint = prettyPrintArgs(cmdAndArgs);
        logger.info("executing cmd: " + prettyPrint);
        ProcessBuilder pb = new ProcessBuilder(cmdAndArgs);
        Process process = pb.start();
        return createResult(process);
    }

    private String prettyPrintArgs(List<String> cmdAndArgs) {
        StringBuffer sb = new StringBuffer();
        for (String s: cmdAndArgs) {
            sb.append(s).append(" ");
        }
        return sb.toString();
    }

    // block on the sub process and capture return code
    private IntegrationTestResult createResult(Process process) {
        int returnCode = -100;
        try {
            while (returnCode == -100) {
                copyStreamNoWait(process.getInputStream(), System.out);
                copyStreamNoWait(process.getErrorStream(), System.err);

                boolean finished = process.waitFor(1, TimeUnit.SECONDS);
                if (finished) {
                    returnCode = process.exitValue();
                    // grab any remaining output
                    copyStreamNoWait(process.getInputStream(), System.out);
                    copyStreamNoWait(process.getErrorStream(), System.err);
                }
            }
        } catch (InterruptedException e) {
            returnCode = -99;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new IntegrationTestResult(returnCode);
    }

    private void copyStreamNoWait(InputStream in, PrintStream out) throws IOException {
        int size = in.available();
        byte buff[] = new byte[size];
        in.read(buff);
        out.write(buff);
    }

    private void addToCommandLine(List<String> cmdAndArgs, IntegrationTestEnvironment environment) {
        addArg(cmdAndArgs, "it.name", environment.getName());
        addArg(cmdAndArgs, "it.apc", environment.getApc());
        addArg(cmdAndArgs, "it.avl", environment.getAvl());
        addArg(cmdAndArgs, "it.gtfs", environment.getGtfs());
        addArg(cmdAndArgs, "it.history", environment.getHistory());
        addArg(cmdAndArgs, "it.predictions", environment.getPredictions());
        addArg(cmdAndArgs, "it.config", environment.getConfig());
        addArg(cmdAndArgs, "it.runid", environment.getRunId());
        addArg(cmdAndArgs, "transitclock.logging.dir", environment.getLoggingDir());
        addArg(cmdAndArgs, "transitclock.test.id", environment.getName());
    }

    private void addArg(List<String> cmdAndArgs, String property, String value) {
        if (value != null) {
            cmdAndArgs.add("-D" + property + "=" + value);
        }
    }

    private IntegrationTestEnvironment createEnvironment(Element testNode, String runId, String outputDirectory) {
        NodeList testNameNode = testNode.getElementsByTagName("testClassName");
        Node item = testNameNode.item(0);
        IntegrationTestEnvironment ite = new IntegrationTestEnvironment();
        ite.setName(nullSafeGet(testNode, "id"));
        ite.setAvl(nullSafeGet(testNode, "avl"));
        ite.setGtfs(nullSafeGet(testNode, "gtfs"));
        ite.setHistory(nullSafeGet(testNode, "history"));
        ite.setPredictions(nullSafeGet(testNode, "predictions"));
        ite.setConfig(nullSafeGet(testNode, "config") + ";" + DEFAULT_HSQL_CONFIG_FILE);
        ite.setLoggingDir(outputDirectory);
        ite.setRunId(runId);

        return ite;
    }

    private String nullSafeGet(Element testNode, String property) {
        NodeList properties = testNode.getElementsByTagName(property);
        if (properties == null || properties.getLength() == 0)
            return null;
        Node item = properties.item(0);
        if (item == null) return null;
        return item.getTextContent();
    }


    private Document getConfiguredTests(File configFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = null;
            builder = factory.newDocumentBuilder();

            Document testsDoc = null;
            testsDoc = builder.parse(configFile);
            testsDoc.getDocumentElement().normalize();

            return testsDoc;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File syncS3ToDisk() {
        File configFile = new File(CONFIG_FILE);

        if (System.getProperty("test.s3.skipSync") != null) {
            logger.info("Configuration set to skip sync, assuming files are present locally");
            return configFile;
        }

        String resourcesDirPath = "src/test/resources/";


        logger.info("Starting sync to disk from S3...");

        try {
            TransferManager tm = getTransferManager();
            tm.download(bucketName, keyPrefix, configFile);
            logger.info("downloading content from s3://{}/{}", bucketName, keyPrefix);
            MultipleFileDownload x = tm.downloadDirectory(bucketName, keyPrefix, new File(resourcesDirPath));
            x.waitForCompletion();
            tm.shutdownNow();

            logger.info("Complete.");

        } catch (AmazonClientException | InterruptedException e) {
            logger.error("Exception: {}", e.getMessage(), e);
            return null;
        }
        return configFile;
    }

    private static TransferManager getTransferManager() {
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion("us-east-1")
                .build();

        // Add appropriate credentials
//             AssumeRoleRequest roleRequest = new AssumeRoleRequest()
//                     .withRoleArn("")
//                     .withRoleSessionName(UUID.randomUUID().toString());

//            AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
//            Credentials sessionCredentials = roleResponse.getCredentials();

        // Add appropriate credentials
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                System.getProperty("test.s3.apikey"),
                System.getProperty("test.s3.secret"));

        AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        logger.info("Got credentials.");

        logger.info("Starting xfer.");

        TransferManager tm = TransferManagerBuilder.standard().withS3Client(AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build()).build();
        return tm;
    }
}
