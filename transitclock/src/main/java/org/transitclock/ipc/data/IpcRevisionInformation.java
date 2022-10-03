package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Expose schedule metadata to API tier.
 */
public class IpcRevisionInformation implements Serializable {

    private int activeRev;
    private int configRev;
    private Date processedTime;
    private String note;
    private Date zipFileTime;

    public int getActiveRev() {
        return activeRev;
    }
    public void setActiveRev(int activeRev) {
        this.activeRev = activeRev;
    }

    public int getLastConfigRev() {
        return configRev;
    }
    public void setLastConfigRev(int configRev) {
        this.configRev = configRev;
    }

    public Date getLastProcessedTime() {
        return processedTime;
    }
    public void setLastProcessedTime(Date processedTime) {
        this.processedTime = processedTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getZipFileLastModifiedTime() {
        return zipFileTime;
    }
    public void setZipFileLastModifiedTime(Date zipFileLastModifiedTime) {
        this.zipFileTime = zipFileLastModifiedTime;
    }
}
