package PrintServer;

import models.Printer;
import org.interfaces.IPrintServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.html.Option;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class PrintServer implements IPrintServer {
    private static final Logger logger = LogManager.getLogger(PrintServer.class);
    private final Map<String, String> configs;
    private final List<Printer> printers;

    private static final List<Printer> initDummyPrinters = List.of(
        new Printer("Printer1"),
        new Printer("Printer2"),
        new Printer("Printer3")
    );

    public PrintServer() throws RemoteException {
        logger.info("Initializing PrintServer...");
        UnicastRemoteObject.exportObject(this, 0);
        printers = initDummyPrinters;
        configs = new HashMap<>();
    }

    @Override
    public void print(String filename, String printer) throws RemoteException {
        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return;
        }
        result.get().print(filename);
    }

    @Override
    public String queue(String printer) throws RemoteException {
        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return "Printer not found";
        }
        return result.get().getjobsQueueAsString();
    }

    @Override
    public void topQueue(String printer, int job) throws RemoteException {
        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return;
        }
        result.get().topQueue(job);
    }

    @Override
    public void start() throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(1099);
        try {
            registry.bind("server", this);
            logger.info("Server has started....");
        } catch (AlreadyBoundException e) {
            logger.error("Server binding failed: already bound", e);
            throw new RuntimeException(e);
        }
        logger.info("Server started.....");
    }

    @Override
    public void stop() throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.unbind("server");
            logger.info("Print server has stopped...");
        } catch (RemoteException e) {
            logger.error("Failed to stop the print server due to a remote exception", e);
        } catch (NotBoundException e) {
            logger.error("Print server was not bound when trying to stop");
        }
    }


    @Override
    public void restart() throws RemoteException {
        logger.info("Restarting print server...");
        this.stop();
        this.start();

    }

    @Override
    public String status(String printer) throws RemoteException {
        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return "Printer not found";
        }
        return result.get().getStatus();
    }

    @Override
    public String readConfig(String parameter) throws RemoteException {
        String value = configs.get(parameter);

        if (value == null) {
            logger.warn("Config read: parameter '" + parameter + "' does not exist.");
            return "Parameter not found";
        }

        logger.debug("Config read: " + parameter + " = " + value);
        return value;
    }


    @Override
    public void setConfig(String parameter, String value) throws RemoteException {
        configs.put(parameter, value);
        logger.info("Config set: " + parameter + " = " + value);

    }

    private Optional<Printer> findPrinter(String printerName) {
        return printers.stream()
                .filter(printer -> printer.getName().equalsIgnoreCase(printerName))
                .findFirst();
    }

}
