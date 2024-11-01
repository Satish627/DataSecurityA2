import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerTest {
    private static final Logger logger = LogManager.getLogger(LoggerTest.class);

    public static void main(String[] args) {
        logger.info("This is a test log message.");
        logger.warn("This is a warning message.");
        logger.error("This is an error message.");
    }
}
