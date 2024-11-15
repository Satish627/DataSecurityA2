package org.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPrintServer extends Remote{
    String login(String email, String password) throws RemoteException;

    void  print(String token,String filename, String printer) throws RemoteException;
    String queue(String token,String printer) throws RemoteException;
    void topQueue(String token,String printer, int job) throws RemoteException;
    void start() throws RemoteException;
    void stop() throws RemoteException;
    void restart() throws RemoteException;
    String status(String token,String printer) throws RemoteException;
    String readConfig(String token,String parameter) throws RemoteException;
    void setConfig(String token,String parameter, String value) throws RemoteException;

    boolean hasPermission(String token, String permission) throws  RemoteException;
}
