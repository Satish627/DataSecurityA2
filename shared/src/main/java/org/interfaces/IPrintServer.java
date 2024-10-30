package org.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPrintServer extends Remote{
    void  print(String filename, String printer) throws RemoteException;
    String queue(String printer) throws RemoteException;
    void topQueue(String printer, int job) throws RemoteException;
    void start() throws RemoteException;
    void stop() throws RemoteException;
    void restart() throws RemoteException;
    String status(String printer) throws RemoteException;
    String readConfig(String parameter) throws RemoteException;
    void setConfig(String parameter, String value) throws RemoteException;

    void startServer() throws RemoteException;
}
