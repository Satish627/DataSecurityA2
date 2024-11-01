import PrintServer.PrintServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.interfaces.IPrintServer;

import java.rmi.RemoteException;

public class Server {
    public static void main(String[] args) throws RemoteException {
        IPrintServer printServer = new PrintServer();
        printServer.start();
    }
}