package org.transitclock.ipc.interfaces;

import org.transitclock.ipc.data.IpcRevisionInformation;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for exposing schedule metadata to API tier.
 */
public interface RevisionInformationInterface extends Remote {

    IpcRevisionInformation get() throws RemoteException;


}
