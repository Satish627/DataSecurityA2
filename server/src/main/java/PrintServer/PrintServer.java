package PrintServer;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import PrintServer.models.Printer;
import database.Database;
import org.interfaces.IPrintServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.models.TokenResponse;

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
    public TokenResponse login(String email, String password) throws RemoteException {
        boolean isCorrectCredentials = Database.verifyUser(email, password);
        if (!isCorrectCredentials) {
            logger.warn("Invalid login attempt : "+ email);
            return new TokenResponse(null, false);
        }
        String token = generateToken(email);
        logger.info("Token generated successfully");
        return new TokenResponse(token, true);

    }

    @Override
    public TokenResponse signUp(String email, String password) throws RemoteException {
        boolean isUserAdded = Database.addUser(email, password);
        if (!isUserAdded) {
            logger.warn("User already exists: "+ email);
            return new TokenResponse(null, false);
        }
        logger.info("User added successfully: "+ email);
        String token = generateToken(email);
        return new TokenResponse(token, true);
    }

    @Override
    public void print(String filename, String printer, String token) throws RemoteException {
        Optional<String> decoded = validateToken(token);
        if (decoded.isEmpty()){
            logger.warn("Invalid token");
            return;
        }
        String email = decoded.get();

        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return;
        }
        result.get().print(filename, email );
    }

    @Override
    public String queue(String printer, String token) throws RemoteException {
        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return "Printer not found";
        }
        return result.get().getjobsQueueAsString();
    }

    @Override
    public void topQueue(String printer, int job, String token) throws RemoteException {
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
    public String status(String printer, String token) throws RemoteException {
        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return "Printer not found";
        }
        return result.get().getStatus();
    }

    @Override
    public String readConfig(String parameter, String token) throws RemoteException {
        String value = configs.get(parameter);

        if (value == null) {
            logger.warn("Config read: parameter '" + parameter + "' does not exist.");
            return "Parameter not found";
        }

        logger.debug("Config read: " + parameter + " = " + value);
        return value;
    }


    @Override
    public void setConfig(String parameter, String value, String token) throws RemoteException {
        configs.put(parameter, value);
        logger.info("Config set: " + parameter + " = " + value);

    }

    private Optional<Printer> findPrinter(String printerName) {
        return printers.stream()
                .filter(printer -> printer.getName().equalsIgnoreCase(printerName))
                .findFirst();
    }

    private String generateToken(String email){
        String secret = ConfigLoader.getProperty("jwt.secret");
        String issuer = ConfigLoader.getProperty("jwt.issuer");
        long expiration = Long.parseLong(ConfigLoader.getProperty("jwt.expiration"));

        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create().withIssuer(issuer)
                .withClaim("email", email)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(algorithm);
    }

    private Optional<String> validateToken(String token){
        try{
            String secret = ConfigLoader.getProperty("jwt:secret");
            String issuer = ConfigLoader.getProperty("jwt:issuer");


            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return Optional.of(jwt.getClaim("email").asString());
        }catch (Exception e){
            logger.warn("Invalid token");
            return Optional.empty();
        }
    }

}
