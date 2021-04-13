package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcStop;

import javax.xml.bind.annotation.XmlAttribute;

public class ApiStopSimple {
    @XmlAttribute
    private String id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private Integer code;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
    protected ApiStopSimple() {
    }

    public ApiStopSimple(IpcStop stop) {
        this.id = stop.getId();
        this.name = stop.getName();
        this.code = stop.getCode();
    }


}

