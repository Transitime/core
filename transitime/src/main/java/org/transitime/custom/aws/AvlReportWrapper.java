package org.transitime.custom.aws;

import org.transitime.db.structs.AvlReport;

public class AvlReportWrapper {
  private AvlReport _report;
  private Long _totalLatency = null;
  private Long _avlLatency = null;
  private Long _sqsLatency = null;
  private Long _forwarderProcessingLatency = null;
  private Long _forwarderSendLatency = null;
  
  public AvlReportWrapper(AvlReport report, Long avlLatency, Long forwarderProcessingLatency, Long sqsLatency, Long totalLatency) {
    _report = report;
    _avlLatency = avlLatency;
    _sqsLatency = sqsLatency;
    _totalLatency = totalLatency;
    _forwarderProcessingLatency = forwarderProcessingLatency;
    if (sqsLatency != null && forwarderProcessingLatency != null) {
      _forwarderSendLatency = sqsLatency - forwarderProcessingLatency;
    }
  }
  
  public AvlReport getReport() {
    return _report;
  }
  
  public Long getTotalLatency() {
    return _totalLatency;
  }
  
  public Long getAvlLatency() {
    return _avlLatency;
  }
  
  public Long getSqsLatency() {
    return _sqsLatency;
  }
  
  public Long getForwarderProcessingLatency() {
    return _forwarderProcessingLatency;
  }
  
  public Long getForwarderSendLatency() {
    return _forwarderSendLatency;
  }
}
