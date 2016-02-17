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

import java.io.File;
import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.ResourceNotFoundException;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

/**
 * For transferring files to and from AWS Glacier storage.
 *
 * @author SkiBu Smith
 *
 */
public class AwsGlacier {

	public static final String OREGON_REGION = "us-west-2";

	// For uploading and downloading files
	private final ArchiveTransferManager atm;
	private final AWSCredentials credentials;
	private final AmazonGlacierClient glacierClient;
    public final AmazonSQSClient sqsClient;
    public final AmazonSNSClient snsClient;
	
	private static final Logger logger = LoggerFactory
			.getLogger(AwsGlacier.class);

	/********************** Member Functions **************************/

	public AwsGlacier(String region) {
		// Get credentials from credentials file, environment variable, or 
		// Java property. 
		// See http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html
		AWSCredentialsProviderChain credentialsProvider = 
				new DefaultAWSCredentialsProviderChain();
		credentials = credentialsProvider.getCredentials();
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

        // Create the ArchiveTransferManager used for uploading and 
        // downloading files. Need to use ArchiveTransferManager constructor 
        // that allows one to specify sqsClient & snsClient so that they have
        // the proper region. If use ArchiveTransferManager without specifying
        // sqs and sns clients then default ones are constructed, but these
        // use the default Virginia region, which is wrong.
		atm = new ArchiveTransferManager(glacierClient, sqsClient, snsClient);
}
	

	/**
	 * Uploads file to vault on AWS Glacier.
	 * 
	 * @param vaultName
	 * @param fileName
	 * @param archiveDescription
	 * @return The archiveId if successful, otherwise null
	 */
	public String upload(String vaultName, String fileName, 
			String archiveDescription) {
		File file = new File(fileName);
		try {
			UploadResult uploadResult = 
					atm.upload(vaultName, archiveDescription, file);
			return uploadResult.getArchiveId();
		} catch (ResourceNotFoundException e1) { 
			// The vault doesn't exist so try creating it
			CreateVaultRequest request = 
					new CreateVaultRequest().withVaultName(vaultName);
			glacierClient.createVault(request);
			
			try {
				UploadResult uploadResult = 
						atm.upload(vaultName, archiveDescription, file);
				return uploadResult.getArchiveId();
			} catch (AmazonClientException | FileNotFoundException e2) {
				logger.error("Exception occurred when trying to create "
						+ "vaultName=\"{}\"", vaultName, e2);				
				return null;
			}
		} catch (AmazonClientException | FileNotFoundException e) {
			logger.error("Exception occurred when uploading file \"{}\" to "
					+ "AWS Glacier. {}", fileName, e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * So can log download events to see the progress of the download.
	 */
	private static class ProgressListenerImpl implements ProgressListener {

		/**
		 * Called when there is a ProgressEvent during a download.
		 *
		 * @see com.amazonaws.event.ProgressListener#progressChanged(com.amazonaws.event.ProgressEvent)
		 */
		@Override
		public void progressChanged(ProgressEvent progressEvent) {
			logger.info("Download progress event: {}", progressEvent);
			
		}
		
	}

	/**
	 * Downloads the file with the specified archiveId from the specified
	 * vaultName. Note that this can take several hours!
	 * 
	 * @param vaultName
	 * @param archiveId
	 * @param outputFileName
	 */
	public void download(String vaultName, String archiveId,
			String outputFileName) {
		logger.info("Downloading to file=\"{}\" from vaultName=\"{}\" "
				+ "archiveId=\"{}\"", 
				outputFileName, vaultName, archiveId);
		
		// So can log how long request took
		IntervalTimer timer = new IntervalTimer();
		
		String accountId = "-"; // Use default account
		ProgressListenerImpl progressListener = new ProgressListenerImpl();
		File outputFile = new File(outputFileName);
		try {
			atm.download(accountId, vaultName, archiveId, outputFile,
					progressListener);
			
			logger.info("Successfully downloaded in {} minutes from "
					+ "vaultName=\"{}\" archiveId=\"{}\"", 
					timer.elapsedMsec()/Time.MS_PER_MIN, vaultName, archiveId);
		} catch (AmazonClientException e) {
			logger.error("Exception when downloading from vaultName=\"{}\" "
					+ "archiveId=\"{}\". {}", 
					vaultName, archiveId, e.getMessage());
		}
	}
	
	/**
	 * For testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		AwsGlacier glacier = new AwsGlacier(OREGON_REGION);
//		String uploadId = glacier.upload("testVault-WithSlash",
//				"C:/Users/Mike/tmp/json.txt", "Initial test2");
//		System.err.println("uploadId=" + uploadId);
		
		String vaultName = "mbta-core";		
		String archiveId = "KcIOX6mmr0A1h4WQpeu7Vq4EbmO1Xp7vZJZZ5iAEEcYNY2KYxdZH3mIHATKIbdiVg0Nu6Od9FLScOpgdjrMUsaXe7pyI77MNBz5wd_hqaYhQ81DG_f5v5n6FSSFF4JJg90YT5khmrQ";
		String outputFileName = "D:/Logs/mbta/archive_2014-08-30.zip";
		glacier.download(vaultName, archiveId, outputFileName);
	}

}
