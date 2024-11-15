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
        String role = getUserRole(email);
        if (role != null && verifyPassword(email, password)) {
            logger.info("Login successful for user: {}", email);
            return generateToken(email, role);
        }
        logger.warn("Login failed for user: {}", email);
        return null;
    }

    private String getUserRole(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader("server/src/main/resources/userCredentials.txt"))) {
            String role;
            while ((role = reader.readLine()) != null) {
                String[] parts = role.split(":");
                if (parts[0].equalsIgnoreCase(email)) {
                    return parts[2];
                }
            }
        } catch (IOException e) {
            logger.error("Error reading user credentials file", e);
        }
        return null;
    }

    @Override
    public void print(String token, String filename, String printer) throws RemoteException {
        checkAccess(token, "Print");

        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            throw new RemoteException("Printer not found.");
        }
        result.get().print(filename);
        logger.info("Print request sent to {} by user with token: {}", printer, token);
    }

    @Override
    public String queue(String token, String printer) throws RemoteException {
        checkAccess(token, "Queue");

        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            throw new RemoteException("Printer not found.");
        }
        logger.info("Queue request for {} by user with token: {}", printer, token);
        return result.get().getjobsQueueAsString();
    }

    @Override
    public void topQueue(String token, String printer, int job) throws RemoteException {
        checkAccess(token, "TopQueue");

        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            throw new RemoteException("Printer not found.");
        }
        result.get().topQueue(job);
        logger.info("Job {} moved to top of queue on {} by user with token: {}", job, printer, token);
    }

    @Override
    public void start() throws RemoteException {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("server", this);
            logger.info("Server has started...");
        } catch (AlreadyBoundException e) {
            logger.error("Server binding failed: already bound", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.unbind("server");
            logger.info("Print server has stopped...");
        } catch (Exception e) {
            logger.error("Error stopping the print server.", e);
        }
    }

    @Override
    public void restart() throws RemoteException {
        logger.info("Restarting print server...");
        this.stop();
        this.start();
    }

    @Override
    public String status(String token, String printer) throws RemoteException {
        checkAccess(token, "Status");

        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            throw new RemoteException("Printer not found.");
        }
        logger.info("Status request for {} by user with token: {}", printer, token);
        return result.get().getStatus();
    }

    @Override
    public String readConfig(String token, String parameter) throws RemoteException {
        checkAccess(token, "ReadConfig");

        String value = configs.get(parameter);
        if (value == null) {
            logger.warn("Config read: parameter '{}' does not exist.", parameter);
            return "Parameter not found.";
        }

        logger.info("Read config '{}' by user with token: {}", parameter, token);
        return value;
    }


    @Override
    public void setConfig(String token, String parameter, String value) throws RemoteException {
        checkAccess(token, "SetConfig");

        configs.put(parameter, value);
        logger.info("Set config '{}' = '{}' by user with token: {}", parameter, value, token);
    }

    @Override
    public boolean hasPermission(String token, String permission) throws RemoteException {
        String role = getRoleFromToken(token);
        if (role == null) {
            logger.warn("No user role found for token: {}", token);
            return false;
        }

        Map<String, List<String>> rolePermissions = Map.of(
                "Admin", List.of("Print", "Queue", "TopQueue", "Status", "ReadConfig", "SetConfig"),
                "User", List.of("Print", "Queue", "Status")
        );

        List<String> permissions = rolePermissions.get(role);
        if (permissions != null && permissions.contains(permission)) {
            return true;
        } else {
            logger.warn("Role '{}' does not have permission for '{}'", role, permission);
            return false;
        }
    }

    private Optional<Printer> findPrinter(String printerName) {
        return printers.stream()
                .filter(printer -> printer.getName().equalsIgnoreCase(printerName))
                .findFirst();
    }

    private boolean validateToken(String token) throws RemoteException {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token);
            Date expiration = claims.getBody().getExpiration();
            if (expiration.before(new Date())) {
                throw new RemoteException("Token expired. Please log in again.");
            }
            return true;
        } catch (ExpiredJwtException e) {
            throw new RemoteException("Token expired. Please log in again.");
        } catch (Exception e) {
            logger.warn("Token validation error.", e);
            throw new RemoteException("Authentication failed. Invalid token.");
        }
    }

    private String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    private String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            logger.warn("Error extracting role from token.", e);
            return null;
        }
    }

    private void checkAccess(String token, String permission) throws RemoteException {
        if (!validateToken(token)) {
            throw new RemoteException("Authentication failed. Invalid or expired token.");
        }
        if (!hasPermission(token, permission)) {
            throw new RemoteException("You do not have permission to perform this action.");
        }
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
}
