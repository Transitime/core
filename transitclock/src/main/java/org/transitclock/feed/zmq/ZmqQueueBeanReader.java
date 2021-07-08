package org.transitclock.feed.zmq;

import org.transitclock.db.structs.AvlReport;

public interface ZmqQueueBeanReader {
    AvlReport getAvlReport(String topic, String contents) throws Exception;
}
