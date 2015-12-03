package org.transitime.custom.aws;

import org.transitime.db.structs.AvlReport;

public class AvlReportWrapper {
  private AvlReport _report;
  private Long _totalLatency = null;
  private Long _avlLatency = null;
  private Long _sqsLatency = null;
  
  public AvlReportWrapper(AvlReport report, long avlLatency, long sqsLatency, long totalLatency) {
    _report = report;
    _avlLatency = avlLatency;
    _sqsLatency = sqsLatency;
    _totalLatency = totalLatency;
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
  
}
