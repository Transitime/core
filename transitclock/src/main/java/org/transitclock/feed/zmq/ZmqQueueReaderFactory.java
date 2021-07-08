package org.transitclock.feed.zmq;

import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

public class ZmqQueueReaderFactory {
    private static StringConfigValue className =
            new StringConfigValue("transitclock.avl.feed.zmq.reader",
                    "org.transitclock.feed.zmq.oba.NycQueueInferredLocationBeanReader",
                    "Specifies the class used to process queue inferred location beans.");

    public static ZmqQueueBeanReader singleton = null;

    public static ZmqQueueBeanReader getInstance() {

        if (singleton == null) {
            singleton = ClassInstantiator.instantiate(className.getValue(),
                    ZmqQueueBeanReader.class);
        }

        return singleton;
    }
}
