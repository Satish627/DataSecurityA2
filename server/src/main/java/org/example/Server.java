package org.example;

import org.example.PrintServer.PrintServer;
import org.interfaces.IPrintServer;

import java.rmi.RemoteException;

public class Server {
    public static void main(String[] args) {
        try {// Bind server to name
            IPrintServer printServer = new PrintServer();
            printServer.startServer();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}