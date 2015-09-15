package org.transitime.custom.aws;

import org.transitime.db.structs.AvlReport;

import com.amazonaws.services.sqs.model.Message;

/**
 * Interface for deserializing an AWS SQS Message into an
 * AVLReport. 
 *
 */
public interface SqsMessageUnmarshaller {
  
  AvlReport toAvlReport(Message message) throws Exception;
  String toString(Message message) throws Exception;

}
