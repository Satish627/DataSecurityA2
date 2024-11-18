package PrintServer;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import PrintServer.models.Printer;
import database.Database;
import database.User;
import org.interfaces.IPrintServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.models.Empty;
import org.models.ServerResponse;
import org.models.TokenResponse;

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

    private final Map<String, List<RolePermissions.PrintServerMethods>> ROLE_PERMISSIONS =
            RolePermissions.getRolePermissions("server/src/main/resources/permissions.json");

    private static boolean hasStarted = true;
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
    public TokenResponse login(String email, String password) throws RemoteException {
        if (!hasStarted) {
            logger.warn("Server not started");
            return new TokenResponse(null, false);
        }
        Optional<User> loginResponse = Database.verifyUser(email, password);
        if (loginResponse.isEmpty()) {
            logger.warn("Invalid login attempt : " + email);
            return new TokenResponse(null, false);
        }
        String token = generateToken(loginResponse.get());
        logger.info("Token generated successfully");
        return new TokenResponse(token, true);
    }

    @Override
    public TokenResponse signUp(String email, String password) throws RemoteException {
        if (!hasStarted) {
            logger.warn("Server not started");
            return new TokenResponse(null, false);
        }
        Optional<User> addResponse = Database.addUser(email, password);
        if (addResponse.isEmpty()) {
            logger.warn("User already exists: " + email);
            return new TokenResponse(null, false);
        }
        logger.info("User added successfully: " + email);
        String token = generateToken(addResponse.get());
        return new TokenResponse(token, true);
    }

    @Override
    public ServerResponse<Empty> print(String filename, String printer, String token) throws RemoteException {
        if (!hasStarted) {
            logger.warn("Server not started");
            return ServerResponse.NOT_ALLOWED();
        }
        if (isRestricted(token, RolePermissions.PrintServerMethods.PRINT)) {
            return ServerResponse.NOT_ALLOWED();
        }
        Optional<String> decoded = validateToken(token);
        if (decoded.isEmpty()) {
            logger.warn("Invalid token");
            return ServerResponse.NOT_ALLOWED();
        }
        String email = decoded.get();

        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return ServerResponse.NOT_ALLOWED();
        }
        result.get().print(filename, email);
        return ServerResponse.SUCCESS();
    }

    @Override
    public ServerResponse<String> queue(String printer, String token) throws RemoteException {
        if (!hasStarted) {
            logger.warn("Server not started");
            return ServerResponse.NOT_ALLOWED("");
        }
        if (isRestricted(token, RolePermissions.PrintServerMethods.QUEUE)) {
            return ServerResponse.NOT_ALLOWED("");
        }
        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return ServerResponse.SUCCESS("Printer not found");
        }
        return ServerResponse.SUCCESS(result.get().getjobsQueueAsString());
    }

    @Override
    public ServerResponse<Empty> topQueue(String printer, int job, String token) throws RemoteException {
        if (!hasStarted) {
            logger.warn("Server not started");
            return ServerResponse.NOT_ALLOWED();
        }
        if (isRestricted(token, RolePermissions.PrintServerMethods.TOP_QUEUE)) {
            return ServerResponse.NOT_ALLOWED();
        }
        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return ServerResponse.NOT_ALLOWED();
        }
        result.get().topQueue(job);
        return ServerResponse.SUCCESS();
    }

    @Override
    public ServerResponse<Empty> start(String token) throws RemoteException {
        if (hasStarted) {
            logger.warn("Server already started");
            return ServerResponse.NOT_ALLOWED();
        }
        if (isRestricted(token, RolePermissions.PrintServerMethods.START)) {
            return ServerResponse.NOT_ALLOWED();
        }
        hasStarted = true;
        return ServerResponse.SUCCESS();
    }

    @Override
    public ServerResponse<Empty> stop(String token) throws RemoteException {
        if (!hasStarted) {
            logger.warn("Server not started");
            return ServerResponse.NOT_ALLOWED();
        }
        if (isRestricted(token, RolePermissions.PrintServerMethods.STOP)) {
            return ServerResponse.NOT_ALLOWED();
        }
        hasStarted = false;
        return ServerResponse.SUCCESS();
    }

    @Override
    public ServerResponse<Empty> restart(String token) throws RemoteException {
        if (isRestricted(token, RolePermissions.PrintServerMethods.RESTART)) {
            return ServerResponse.NOT_ALLOWED();
        }
        logger.info("Restarting print server...");
        this.stop(token);
        this.start(token);
        return ServerResponse.SUCCESS();
    }

    @Override
    public ServerResponse<String> status(String printer, String token) throws RemoteException {
        if (!hasStarted) {
            logger.warn("Server not started");
            return ServerResponse.NOT_ALLOWED("");
        }

        if (isRestricted(token, RolePermissions.PrintServerMethods.STATUS)) {
            return ServerResponse.NOT_ALLOWED("");
        }
        Optional<Printer> result = findPrinter(printer);
        if (result.isEmpty()) {
            logger.warn("Printer not found: " + printer);
            return ServerResponse.SUCCESS("Printer not found");
        }
        return ServerResponse.SUCCESS(result.get().getStatus());
    }

    @Override
    public ServerResponse<String> readConfig(String parameter, String token) throws RemoteException {
        if (!hasStarted) {
            logger.warn("Server not started");
            return ServerResponse.NOT_ALLOWED("");
        }
        if (isRestricted(token, RolePermissions.PrintServerMethods.READ_CONFIG)) {
            return ServerResponse.NOT_ALLOWED("");
        }
        String value = configs.get(parameter);

        if (value == null) {
            logger.warn("Config read: parameter '" + parameter + "' does not exist.");
            return ServerResponse.SUCCESS("Parameter not found");
        }

        logger.debug("Config read: " + parameter + " = " + value);
        return ServerResponse.SUCCESS(value);
    }

    @Override
    public ServerResponse<Empty> setConfig(String parameter, String value, String token) throws RemoteException {
        if (!hasStarted) {
            logger.warn("Server not started");
            return ServerResponse.NOT_ALLOWED();
        }

        if (isRestricted(token, RolePermissions.PrintServerMethods.SET_CONFIG)) {
            return ServerResponse.NOT_ALLOWED();
        }
        configs.put(parameter, value);
        logger.info("Config set: " + parameter + " = " + value);
        return ServerResponse.SUCCESS();
    }

    private Optional<Printer> findPrinter(String printerName) {
        return printers.stream()
                .filter(printer -> printer.getName().equalsIgnoreCase(printerName))
                .findFirst();
    }

    private String generateToken(User user) {
        String secret = ConfigLoader.getProperty("jwt.secret");
        String issuer = ConfigLoader.getProperty("jwt.issuer");
        long expiration = Long.parseLong(ConfigLoader.getProperty("jwt.expiration"));

        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create().withIssuer(issuer)
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getRole().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(algorithm);
    }

    private Optional<String> validateToken(String token) {
        try {
            Optional<DecodedJWT> decodedJWT = decodedJWT(token);
            return decodedJWT.map(jwt -> jwt.getClaim("email").asString());
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            return Optional.empty();
        }
    }

    private Optional<DecodedJWT> decodedJWT(String token){
        try {
            String secret = ConfigLoader.getProperty("jwt.secret");
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ConfigLoader.getProperty("jwt.issuer"))
                    .build();

            return Optional.of(verifier.verify(token));
        } catch (IllegalArgumentException | JWTVerificationException e) {
            return Optional.empty();
        }
    }

    private boolean isRestricted(String token, RolePermissions.PrintServerMethods method) {
        Optional<DecodedJWT> decodedJWT = decodedJWT(token);
        if (decodedJWT.isEmpty()) {
            return true;
        }
        String role = decodedJWT.get().getClaim("role").asString();

        return ! (ROLE_PERMISSIONS.containsKey(role) &&
                ROLE_PERMISSIONS.get(role).contains(method));
    }
}
