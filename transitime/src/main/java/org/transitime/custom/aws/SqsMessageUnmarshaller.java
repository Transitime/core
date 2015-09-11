package org.transitime.custom.aws;

import org.transitime.db.structs.AvlReport;

import com.amazonaws.services.sqs.model.Message;

/**
 * Interface for deserializing an AWS SQS Message into an
 * AVLReport. 
 *
 */
public interface SqsMessageUnmarshaller {
  
  AvlReport deserialize(Message message) throws Exception;

}
