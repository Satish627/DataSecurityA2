package PrintServer;

import io.jsonwebtoken.*;
import models.Printer;
import org.interfaces.IPrintServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

    private static final String key = "secret";

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
    public String login(String email, String password) throws RemoteException {
        if (verifyPassword(email, password)) {
            return generateToken(email);
        }
        return null;
    }
    @Override
    public void print(String token, String filename, String printer) throws RemoteException {
        if (validateToken(token)) {
            Optional<Printer> result = findPrinter(printer);
            if (result.isEmpty()) {
                logger.warn("Printer not found: " + printer);
                return;
            }
            result.get().print(filename);
        } else {
            logger.warn("Authentication failed... invalid token");
        }
    }


    @Override
    public String queue(String token,String printer) throws RemoteException {
        if(!validateToken(token)){
            logger.warn("Authentication failed... invalid or expired token");
            return "Authentication failed. Please log in again.";
        }

        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return "Printer not found";
        }
        return result.get().getjobsQueueAsString();
    }

    @Override
    public void topQueue(String token,String printer, int job) throws RemoteException {
        if(!validateToken(token)){
            logger.warn("Authentication failed... invalid or expired token");
        }

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
    public String status(String token,String printer) throws RemoteException {
        if(!validateToken(token)){
            logger.warn("Authentication failed... invalid or expired token");
            return "Authentication failed. Please log in again.";
        }

        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return "Printer not found";
        }
        return result.get().getStatus();
    }

    @Override
    public String readConfig(String token,String parameter) throws RemoteException {
        if(!validateToken(token)){
            logger.warn("Authentication failed... invalid or expired token");
            return "Authentication failed. Please log in again.";
        }

        String value = configs.get(parameter);

        if (value == null) {
            logger.warn("Config read: parameter '" + parameter + "' does not exist.");
            return "Parameter not found";
        }

        logger.debug("Config read: " + parameter + " = " + value);
        return value;
    }


    @Override
    public void setConfig(String token,String parameter, String value) throws RemoteException {
        if(!validateToken(token)){
            logger.warn("Authentication failed... invalid or expired token");
        }

        configs.put(parameter, value);
        logger.info("Config set: " + parameter + " = " + value);

    }

    private Optional<Printer> findPrinter(String printerName) {
        return printers.stream()
                .filter(printer -> printer.getName().equalsIgnoreCase(printerName))
                .findFirst();
    }
    public boolean verifyPassword(String email, String enteredPassword) {
        try (BufferedReader reader = new BufferedReader(new FileReader("server/src/main/resources/userCredentials.txt"))) {
            String credentials;
            while ((credentials = reader.readLine()) != null) {
                String[] parts = credentials.split(":");
                String storedEmail = parts[0];
                String storedHashedPassword = parts[1];

                if (storedEmail.equalsIgnoreCase(email)) {
                    return BCrypt.checkpw(enteredPassword, storedHashedPassword);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading user credentials file", e);
        }
        return false;
    }
    private String generateToken(String email) {
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
        logger.info("Generated Token: " + token);
        return token;
    }

    private boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token);
            return claims.getBody().getExpiration().after(new Date());
        } catch (SignatureException e) {
            logger.warn("Invalid token signature.");
            return false;
        } catch (Exception e) {
            logger.warn("Token validation error.");
            return false;
        }
    }

}
