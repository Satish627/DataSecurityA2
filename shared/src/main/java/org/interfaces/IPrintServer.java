package org.interfaces;

import org.models.TokenResponse;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Optional;

public interface IPrintServer extends Remote{
    TokenResponse login(String email, String password) throws RemoteException;

    TokenResponse signUp(String email, String password) throws RemoteException;
    void  print(String filename, String printer, String token) throws RemoteException;
    String queue(String printer, String token) throws RemoteException;
    void topQueue(String printer, int job, String token) throws RemoteException;
    void start() throws RemoteException;
    void stop() throws RemoteException;
    void restart() throws RemoteException;
    String status(String printer, String token) throws RemoteException;
    String readConfig(String parameter, String token) throws RemoteException;
    void setConfig(String parameter, String value, String token) throws RemoteException;
}
