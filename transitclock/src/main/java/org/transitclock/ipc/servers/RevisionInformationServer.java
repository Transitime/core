package org.transitclock.ipc.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.db.structs.ConfigRevision;
import org.transitclock.ipc.data.IpcRevisionInformation;
import org.transitclock.ipc.interfaces.RevisionInformationInterface;
import org.transitclock.ipc.rmi.AbstractServer;

import java.rmi.RemoteException;

/**
 * RMI server for exposing internal schedule metadata.
 */
public class RevisionInformationServer extends AbstractServer implements RevisionInformationInterface {

    private static RevisionInformationServer singleton;

    private static final Logger logger =
            LoggerFactory.getLogger(RevisionInformationServer.class);

    public static RevisionInformationServer start(String agencyId) {
        if (singleton == null) {
            singleton = new RevisionInformationServer(agencyId);
        }
        if (!singleton.getAgencyId().equals(agencyId)) {
            logger.error("Tried calling ServerStatusServer.start() for " +
                            "agencyId={} but the singleton was created for projectId={}",
                    agencyId, singleton.getAgencyId());
            return null;
        }
        return singleton;
    }

    private RevisionInformationServer(String projectId) {
        super(projectId, RevisionInformationInterface.class.getSimpleName());
    }

    @Override
    public IpcRevisionInformation get() throws RemoteException {
        int activeRev = Core.getInstance().getDbConfig().getConfigRev();
        int lastIndex = Core.getInstance().getDbConfig().getConfigRevisions().size()-1;
        ConfigRevision lastConfigRevision = Core.getInstance().getDbConfig().getConfigRevisions().get(lastIndex);
        IpcRevisionInformation ipcRevisionInformation = new IpcRevisionInformation();
        ipcRevisionInformation.setActiveRev(activeRev);
        ipcRevisionInformation.setLastConfigRev(lastConfigRevision.getConfigRev());
        ipcRevisionInformation.setLastProcessedTime(lastConfigRevision.getProcessedTime());
        ipcRevisionInformation.setNote(lastConfigRevision.getNotes());
        ipcRevisionInformation.setZipFileLastModifiedTime(lastConfigRevision.getZipFileLastModifiedTime());
        return ipcRevisionInformation;
    }
}
