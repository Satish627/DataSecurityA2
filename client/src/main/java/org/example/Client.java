package org.example;


import org.interfaces.IPrintServer;
import java.rmi.RemoteException;

public class Client {
    public static void main(String[] args) {
        try {
          IPrintServer server = GetServer.getServerFromRmi();
          server.print("Hello", "World");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}