package org.transitclock.ipc.clients;

import org.transitclock.ipc.interfaces.RevisionInformationInterface;
import org.transitclock.ipc.rmi.ClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for instantiating RevisionInformationInterface.
 */
public class RevisionInterfaceFactory {

    private static Map<String, RevisionInformationInterface> revisionInformationInterfaceMap =
            new HashMap();

    public static RevisionInformationInterface get(String agencyId) {
        RevisionInformationInterface revisionInformationInterface =
                revisionInformationInterfaceMap.get(agencyId);
        if (revisionInformationInterface == null) {
            revisionInformationInterface = ClientFactory.getInstance(agencyId, RevisionInformationInterface.class);
            revisionInformationInterfaceMap.put(agencyId, revisionInformationInterface);
        }
        return revisionInformationInterface;
    }
}
