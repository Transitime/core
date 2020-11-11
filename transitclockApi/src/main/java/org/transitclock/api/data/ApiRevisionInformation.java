package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcRevisionInformation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Exposes metadata about the current dataset loaded.
 */
@XmlRootElement(name = "revisionInformation")
public class ApiRevisionInformation {
    @XmlAttribute
    private String agencyId;

    @XmlElement(name = "lastRevision")
    private IpcRevisionInformation lastRevision;

    protected ApiRevisionInformation() {

    }

    public ApiRevisionInformation(String agencyId, IpcRevisionInformation ipcRevisionInformation) {
        this.agencyId = agencyId;
        this.lastRevision = ipcRevisionInformation;
    }
}
