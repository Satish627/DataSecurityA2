package org.example;


import org.interfaces.IPrintServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class GetServer {
    public static IPrintServer getServerFromRmi(){
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            IPrintServer server = (IPrintServer) registry.lookup("server");
            return server;

        } catch (RemoteException | NotBoundException e) {
           e.printStackTrace();
        }
        return null;
    }
}