package org.transitime.custom.aws;

import java.util.List;

import com.amazonaws.services.sqs.model.Message;

/**
 * Interface for deserializing an AWS SQS Message into an
 * AVLReport. 
 *
 */
public interface SqsMessageUnmarshaller {
  
  AvlReportWrapper toAvlReport(Message message) throws Exception;
  String toString(Message message) throws Exception;
  List<AvlReportWrapper> toAvlReports(Message message);

}
