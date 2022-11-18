package org.transitclock.integration_tests;

import com.amazonaws.auth.BasicAWSCredentials;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfiguredTestsRunner {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguredTestsRunner.class);

    @Test
    public void runTests() {
        // Load in config file
        Document testsDoc = getConfiguredTests();
        NodeList configuredTests = testsDoc.getElementsByTagName("test");

        // Download resources for configured tests
        syncS3ToDisk(configuredTests);

        // Run tests
        try {
            List<Class> testClassNames = new ArrayList<Class>();
            for (int i = 0; i < configuredTests.getLength(); i++) {
                Node testNode = configuredTests.item(i);
                Element testElement = (Element) testNode;
                testClassNames.add(Class.forName(testElement.getElementsByTagName("testClassName").item(0).getTextContent()));
            }

            Result result = JUnitCore.runClasses(testClassNames.toArray(new Class[0]));

            for (Failure failure : result.getFailures()) {
                System.out.println(failure.toString());
            }

            System.out.println(result.wasSuccessful());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Document getConfiguredTests() {
        try {
            Path configPath = Paths.get("src/test/resources");

            File configFile = new File(configPath.toString(), "configuredTests.xml");

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

    private static void syncS3ToDisk(NodeList configuredTests) {

        logger.info("Starting sync to disk from S3...");

        try {
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(new DefaultAWSCredentialsProviderChain())
                    .withRegion("us-east-1")
                    .build();

            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn("")
                    .withRoleSessionName(UUID.randomUUID().toString());

//            AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
//            Credentials sessionCredentials = roleResponse.getCredentials();

            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                    "",
                    "");

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
                String testDirectory = testElement.getElementsByTagName("resourcesDirectory").item(0).getTextContent();
                if (testDirectory != "") {
                    MultipleFileDownload x = tm.downloadDirectory("camsys-met-integration", "tests/" + testDirectory, new File(resourcesDirPath));
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
