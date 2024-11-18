package org.interfaces;

import org.models.Empty;
import org.models.ServerResponse;
import org.models.TokenResponse;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Optional;

public interface IPrintServer extends Remote{
    TokenResponse login(String email, String password) throws RemoteException;

    TokenResponse signUp(String email, String password) throws RemoteException;
    ServerResponse<Empty> print(String filename, String printer, String token) throws RemoteException;
    ServerResponse<String> queue(String printer, String token) throws RemoteException;
    ServerResponse<Empty>  topQueue(String printer, int job, String token) throws RemoteException;
    ServerResponse<Empty>  start(String token) throws RemoteException;
    ServerResponse<Empty>  stop(String token) throws RemoteException;
    ServerResponse<Empty>  restart(String token) throws RemoteException;
    ServerResponse<String> status(String printer, String token) throws RemoteException;
    ServerResponse<String> readConfig(String parameter, String token) throws RemoteException;
    ServerResponse<Empty> setConfig(String parameter, String value, String token) throws RemoteException;
}
