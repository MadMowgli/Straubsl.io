package PreProcessor.Configuration;

import PreProcessor.Driver;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;


public class ConfigurationManager {

    private final Logger logger;
    // Fields
    private final String CONFIGURATION_PATH = System.getProperty("user.dir") + "/src/main/Java/PreProcessor/Configuration/preprocessor.cfg";
    private final String PRIVATE_CONFIGURATION_PATH = System.getProperty("user.dir") + "/src/main/Java/PreProcessor/Configuration/private.cfg";
    public Properties properties;
    public Properties privateProperties;

    // Constructor
    public ConfigurationManager(){
        this.logger = Logger.getLogger(Driver.LOGGER_NAME);

        // Instantiate properties
        this.properties = new Properties();
        this.privateProperties = new Properties();
        try{
            this.properties.load(new FileInputStream(CONFIGURATION_PATH));
            this.privateProperties.load(new FileInputStream(PRIVATE_CONFIGURATION_PATH));
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    // Methods

}
