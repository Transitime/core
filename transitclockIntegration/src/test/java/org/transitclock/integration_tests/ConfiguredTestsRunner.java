package org.transitclock.integration_tests;

import com.amazonaws.auth.BasicAWSCredentials;
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
import java.util.ArrayList;
import java.util.List;

public class ConfiguredTestsRunner {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguredTestsRunner.class);
    private static final String DEFAULT_MVN_CMD = "mvn";
    private static String defaultTestClass = "org.transitclock.integration_tests.prediction.EnvironmentBasedPredictionAccuracyIntegrationTestImpl";

    private static String bucketName = "camsys-met-integration";
    private static String keyPrefix = "tests/";

    @Test
    public void runTests() throws Exception {
        // Load in config file
        Document testsDoc = getConfiguredTests();
        NodeList configuredTests = testsDoc.getElementsByTagName("test");

        // Download resources for configured tests
        syncS3ToDisk(configuredTests);

        // Run tests
        try {

            for (int i = 0; i < configuredTests.getLength(); i++) {
                Element testNode = (Element) configuredTests.item(i);
                IntegrationTestEnvironment ite = createEnvironment(testNode);
                logger.info("running test {}", ite.getName());
                IntegrationTestResult itr = forkTestClass(defaultTestClass, ite);
                logger.info("back from test {} with rc={}", ite.getName(), itr.getReturnCode());
                logger.info("copied output follows:");
                logger.info(itr.getOutput());
                logger.info("finished loop iteration {}", i);
            }

        } catch (Throwable e) {
            logger.error("test setup failure: {}", e, e);
        }
        logger.error("exiting for cleanup!");
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
        cmdAndArgs.add("-Dtest=" + testClass);
        addToCommandLine(cmdAndArgs, environment);
        String prettyPrint = prettyPrintArgs(cmdAndArgs);
        logger.info("executing cmd: " + prettyPrint);
        ProcessBuilder pb = new ProcessBuilder(cmdAndArgs);
        pb.redirectError();
        pb.redirectOutput();
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
        int returnCode = -1;
        try {
            logger.info("waiting on result....");
            returnCode = process.waitFor();
            logger.info("process rc=" + returnCode);
        } catch (InterruptedException e) {
            returnCode = -99;
        }

        IntegrationTestResult itr = new IntegrationTestResult(returnCode);
        try {
            itr.setOutput(copy(process.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return itr;
    }

    // copy input stream to a string for logging
    private String copy(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        inputStream.transferTo(baos);
        return baos.toString();
    }

    private void addToCommandLine(List<String> cmdAndArgs, IntegrationTestEnvironment environment) {
        addArg(cmdAndArgs, "it.name", environment.getName());
        addArg(cmdAndArgs, "it.apc", environment.getApc());
        addArg(cmdAndArgs, "it.avl", environment.getAvl());
        addArg(cmdAndArgs, "it.gtfs", environment.getGtfs());
        addArg(cmdAndArgs, "it.history", environment.getHistory());
        addArg(cmdAndArgs, "it.predictions", environment.getPredictions());
        addArg(cmdAndArgs, "it.config", environment.getConfig());
    }

    private void addArg(List<String> cmdAndArgs, String property, String value) {
        if (value != null) {
            cmdAndArgs.add("-D" + property + "=" + value);
        }
    }

    private IntegrationTestEnvironment createEnvironment(Element testNode) {
        NodeList testNameNode = testNode.getElementsByTagName("testClassName");
        Node item = testNameNode.item(0);
        IntegrationTestEnvironment ite = new IntegrationTestEnvironment();
        ite.setName(nullSafeGet(testNode, "id"));
        ite.setAvl(nullSafeGet(testNode, "avl"));
        ite.setGtfs(nullSafeGet(testNode, "gtfs"));
        ite.setHistory(nullSafeGet(testNode, "history"));
        ite.setPredictions(nullSafeGet(testNode, "predictions"));
        ite.setConfig(nullSafeGet(testNode, "config"));

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


    private Document getConfiguredTests() {
        try {
            InputStream resourceAsStream = this.getClass().getClassLoader()
                    .getResourceAsStream("configuredTests.xml");
            if (resourceAsStream == null) throw  new FileNotFoundException("configuredTests.xml not found in classpath");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = null;
            builder = factory.newDocumentBuilder();

            Document testsDoc = null;
            testsDoc = builder.parse(resourceAsStream);
            testsDoc.getDocumentElement().normalize();

            return testsDoc;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void syncS3ToDisk(NodeList configuredTests) {

        logger.info("Starting sync to disk from S3...");

        try {
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

            String resourcesDirPath = "src/test/resources/";

            logger.info("Starting xfer.");

            TransferManager tm = TransferManagerBuilder.standard().withS3Client(AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build()).build();
            for (int i = 0; i < configuredTests.getLength(); i++) {
                Node testNode = configuredTests.item(i);
                Element testElement = (Element) testNode;
                String testDirectory = testElement.getElementsByTagName("id").item(0).getTextContent();
                if (testDirectory != "") {
                    logger.info("downloading {} from s3://{}/{}", testDirectory, bucketName, keyPrefix);
                    MultipleFileDownload x = tm.downloadDirectory(bucketName, keyPrefix + testDirectory, new File(resourcesDirPath));
                    x.waitForCompletion();
                }
            }
            tm.shutdownNow();

            logger.info("Complete.");

        } catch (AmazonClientException | InterruptedException e) {
            logger.error("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
