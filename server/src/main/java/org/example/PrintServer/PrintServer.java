package org.example.PrintServer;

import org.interfaces.IPrintServer;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PrintServer implements IPrintServer, Serializable {
    public PrintServer() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public void startServer() throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(1099);
        try {
            registry.bind("server", this);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server started.....");
    }

    @Override
    public void print(String filename, String printer) throws RemoteException {
        System.out.println(filename + " " + printer);

    }

    @Override
    public String queue(String printer) throws RemoteException {
        return null;
    }

    @Override
    public void topQueue(String printer, int job) throws RemoteException {

    }

    @Override
    public void start() throws RemoteException {

    }

    @Override
    public void stop() throws RemoteException {

    }

    @Override
    public void restart() throws RemoteException {

    }

    @Override
    public String status(String printer) throws RemoteException {
        return null;
    }

    @Override
    public String readConfig(String parameter) throws RemoteException {
        return null;
    }

    @Override
    public void setConfig(String parameter, String value) throws RemoteException {

    }
}
