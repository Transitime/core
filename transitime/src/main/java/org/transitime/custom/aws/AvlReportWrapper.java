package org.transitime.custom.aws;

import org.transitime.db.structs.AvlReport;

public class AvlReportWrapper {
  private AvlReport _report;
  private Long _queueLatency = null;
  
  public AvlReportWrapper(AvlReport report, long queueLatency) {
    _report = report;
    _queueLatency = queueLatency;
  }
  
  public AvlReport getReport() {
    return _report;
  }
  
  public Long getQueueLatency() {
    return _queueLatency;
  }
  
}
