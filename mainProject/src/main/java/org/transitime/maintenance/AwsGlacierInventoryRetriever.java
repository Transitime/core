/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.maintenance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * For retrieving an AWS Glacier inventory file for a vault. The inventory file
 * shows all of the entries in the inventory along with the archiveId. This
 * archiveId can be used to upload the data stored in glacier.
 *
 * @author SkiBu Smith
 *
 */
public class AwsGlacierInventoryRetriever {
    
	private final AmazonGlacierClient glacierClient;
    public final AmazonSQSClient sqsClient;
    public final AmazonSNSClient snsClient;

    // Note: need to have created a sns topic for the account using
    // https://console.aws.amazon.com/sns/
    public static String snsTopicName = "glacier";
    
    // Note: need to have created a sqs topic for the account using
    // https://console.aws.amazon.com/sqs/
    public static String sqsQueueName = "glacier";
    
    public String sqsQueueARN;
    public String sqsQueueURL;
    public String snsTopicARN;
    public String snsSubscriptionARN;

    // For polling for job completion for when retrieving inventory
    public static long sleepTimeMsec = 5 * Time.MS_PER_MIN; 
    

	private static final Logger logger = LoggerFactory
			.getLogger(AwsGlacierInventoryRetriever.class);

	/********************** Member Functions **************************/

	public AwsGlacierInventoryRetriever(String region) {
		// Get credentials from credentials file, environment variable, or 
		// Java property. 
		// See http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html
		AWSCredentialsProviderChain credentialsProvider = 
				new DefaultAWSCredentialsProviderChain();
		AWSCredentials credentials = credentialsProvider.getCredentials();
		logger.debug("Read in credentials AWSAccessKeyId={} AWSSecretKey={}...",
				credentials.getAWSAccessKeyId(), 
				credentials.getAWSSecretKey().substring(0, 4));
		
		// Create the glacier client and set to specified region.
		glacierClient = new AmazonGlacierClient(credentials);
		glacierClient.setEndpoint("https://glacier." + region + ".amazonaws.com");
		
		// Set up params needed for retrieving vault inventory
        sqsClient = new AmazonSQSClient(credentials);
        sqsClient.setEndpoint("https://sqs." + region + ".amazonaws.com");
        snsClient = new AmazonSNSClient(credentials);
        snsClient.setEndpoint("https://sns." + region + ".amazonaws.com");
        setupSQS();
        setupSNS();
	}
	
	/**
	 * For retrieving vault inventory. For initializing SQS for determining when
	 * job completed. Does nothing if member snsTopicName is null. Sets members
	 * sqsQueueURL, sqsQueueARN, and sqsClient.
	 */
    private void setupSQS() {
		// If no sqsQueueName setup then simply return
		if (sqsQueueName == null)
			return;

		CreateQueueRequest request = new CreateQueueRequest()
				.withQueueName(sqsQueueName);
		CreateQueueResult result = sqsClient.createQueue(request);
		sqsQueueURL = result.getQueueUrl();

		GetQueueAttributesRequest qRequest = new GetQueueAttributesRequest()
				.withQueueUrl(sqsQueueURL).withAttributeNames("QueueArn");

		GetQueueAttributesResult qResult = sqsClient
				.getQueueAttributes(qRequest);
		sqsQueueARN = qResult.getAttributes().get("QueueArn");

		Policy sqsPolicy = new Policy().withStatements(new Statement(
				Effect.Allow).withPrincipals(Principal.AllUsers)
				.withActions(SQSActions.SendMessage)
				.withResources(new Resource(sqsQueueARN)));
		Map<String, String> queueAttributes = new HashMap<String, String>();
		queueAttributes.put("Policy", sqsPolicy.toJson());
		sqsClient.setQueueAttributes(new SetQueueAttributesRequest(sqsQueueURL,
				queueAttributes));
	}

	/**
	 * For retrieving vault inventory. For initializing SNS for determining when
	 * job completed. Does nothing if member snsTopicName is null. Sets members
	 * snsTopicARN and snsSubscriptionARN.
	 */
    void setupSNS() {
		// If no snsTopicName setup then simply return
		if (snsTopicName == null)
			return;

		CreateTopicRequest request = new CreateTopicRequest()
				.withName(snsTopicName);
		CreateTopicResult result = snsClient.createTopic(request);
		snsTopicARN = result.getTopicArn();

		SubscribeRequest request2 = new SubscribeRequest()
				.withTopicArn(snsTopicARN).withEndpoint(sqsQueueARN)
				.withProtocol("sqs");
		SubscribeResult result2 = snsClient.subscribe(request2);

		snsSubscriptionARN = result2.getSubscriptionArn();
	}
    
    /**
     * For retrieving vault inventory. 
     * 
     * @param vaultName
     * @return
     */
	private String initiateVaultInventoryJobRequest(String vaultName) {
		JobParameters jobParameters = new JobParameters().withType(
				"inventory-retrieval").withSNSTopic(snsTopicARN);

		InitiateJobRequest request = new InitiateJobRequest().withVaultName(
				vaultName).withJobParameters(jobParameters);

		InitiateJobResult response = glacierClient.initiateJob(request);

		return response.getJobId();
	}

	/**
	 * For retrieving vault inventory. 
	 * 
	 * @param jobId
	 * @param sqsQueueUrl
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private Boolean waitForJobToComplete(String jobId, String sqsQueueUrl)
			throws JsonParseException, IOException {
		logger.info("Waiting for job to complete. jobId={}", jobId);
		
		Boolean messageFound = false;
		Boolean jobSuccessful = false;
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getFactory();

		while (!messageFound) {
			List<Message> msgs = sqsClient.receiveMessage(
					new ReceiveMessageRequest(sqsQueueUrl)
							.withMaxNumberOfMessages(10)).getMessages();

			if (msgs.size() > 0) {
				for (Message m : msgs) {
					@SuppressWarnings("deprecation")
					JsonParser jpMessage = factory
							.createJsonParser(m.getBody());
					JsonNode jobMessageNode = mapper.readTree(jpMessage);
					String jobMessage = jobMessageNode.get("Message")
							.textValue();

					@SuppressWarnings("deprecation")
					JsonParser jpDesc = factory.createJsonParser(jobMessage);
					JsonNode jobDescNode = mapper.readTree(jpDesc);
					String retrievedJobId = jobDescNode.get("JobId")
							.textValue();
					String statusCode = jobDescNode.get("StatusCode")
							.textValue();
					if (retrievedJobId.equals(jobId)) {
						messageFound = true;
						if (statusCode.equals("Succeeded")) {
							jobSuccessful = true;
						}
					}
				}

			} else {
				Time.sleep(sleepTimeMsec);
			}
		}
		return (messageFound && jobSuccessful);
	}

	/**
	 * For retrieving vault inventory. 
	 * 
	 * @param vaultName
	 * @param jobId
	 * @param outputFileName
	 * @throws IOException
	 */
	private void downloadJobOutput(String vaultName, String jobId,
			String outputFileName) throws IOException {
		logger.info("Downloading job output from vaultName={} into "
				+ "outputFileName={} for jobId={}", vaultName, outputFileName,
				jobId);
		
		GetJobOutputRequest getJobOutputRequest = new GetJobOutputRequest()
				.withVaultName(vaultName).withJobId(jobId);
		GetJobOutputResult getJobOutputResult = 
				glacierClient.getJobOutput(getJobOutputRequest);

		FileWriter fstream = new FileWriter(outputFileName);
		BufferedWriter out = new BufferedWriter(fstream);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				getJobOutputResult.getBody()));
		String inputLine;
		try {
			while ((inputLine = in.readLine()) != null) {
				out.write(inputLine);
			}
		} catch (IOException e) {
			throw new AmazonClientException("Unable to save archive for "
					+ "vaultName=" + vaultName, e);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
			try {
				out.close();
			} catch (Exception e) {
			}
		}
		
		logger.info("Retrieved inventory for vaultName={} to file={}", 
				vaultName, outputFileName);
	}

	/**
	 * To be called when done with this object. Cleans up notifications for when
	 * jobs complete.
	 */
    private void cleanUp() {
        snsClient.unsubscribe(new UnsubscribeRequest(snsSubscriptionARN));
        snsClient.deleteTopic(new DeleteTopicRequest(snsTopicARN));
        sqsClient.deleteQueue(new DeleteQueueRequest(sqsQueueURL));
    }

	/**
	 * Retrieves vault inventory and puts it into a file.
	 * 
	 * @param vaultName
	 *            Name of the AWS vault
	 * @param outputFileName
	 *            Name of inventory json file to store data in
	 */
	public void getVaultInventory(String vaultName, String outputFileName) {
		logger.info("Getting vault inventory for vaultName={} and storing "
				+ "it into file {}", vaultName, outputFileName);
		IntervalTimer timer = new IntervalTimer();
		
		String jobId = initiateVaultInventoryJobRequest(vaultName);

		try {
			Boolean success = waitForJobToComplete(jobId, sqsQueueURL);
			if (!success) {
				logger.error("Job did not complete successfully for "
						+ "vaultName={} jobId=", vaultName, jobId);
				return;
			}

			downloadJobOutput(vaultName, jobId, outputFileName);
			cleanUp();
			
			logger.info("Successfully downloaded vault inventory for "
					+ "vaultName={} and stored it into file {} . It took {} "
					+ "msec", vaultName, outputFileName, timer.elapsedMsec());
		} catch (Exception e) {
			logger.error("Exception getting vault inventory for vaultName={}"
					+ "jobId=",	vaultName, jobId);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AwsGlacierInventoryRetriever inventoryRetriever = 
				new AwsGlacierInventoryRetriever(AwsGlacier.OREGON_REGION);
		String vaultName = "mbta-core";		
		String inventoryOutputFileName = "D:/Logs/mbta/mbta-core_inventory.json";
		inventoryRetriever.getVaultInventory(vaultName, inventoryOutputFileName);
	}


}
