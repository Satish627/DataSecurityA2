import org.interfaces.IPrintServer;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;


public class GetServer {
    public static IPrintServer getServerFromRmi(){
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            return (IPrintServer) registry.lookup("server");

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}