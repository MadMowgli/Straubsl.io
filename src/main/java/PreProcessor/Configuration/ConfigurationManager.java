package PreProcessor.Configuration;

import PreProcessor.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigurationManager {

    private final Logger logger;

    // Fields
    private final String CONFIGURATION_PATH = System.getProperty("user.dir") + "/src/main/resources/preprocessor.cfg";
    private final String BACKUP_PATH = System.getProperty("user.dir") + "/../preprocessor.cfg";


    // private final String PRIVATE_CONFIGURATION_PATH = System.getProperty("user.dir") + "/src/main/Java/PreProcessor/Configuration/private.cfg";
    public Properties properties;


    // Constructor
    public ConfigurationManager(){
        this.logger = Logger.getLogger(Driver.LOGGER_NAME);

        // Instantiate properties
        this.properties = new Properties();

        try{
            this.properties.load(new FileInputStream(ResourceUtils.getFile("classpath:application.properties")));
//            this.properties.load(new FileInputStream(CONFIGURATION_PATH));
            // this.privateProperties.load(new FileInputStream(PRIVATE_CONFIGURATION_PATH));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            this.logger.severe(e.getMessage());

            try{
                this.properties.load(new FileInputStream(BACKUP_PATH));
            } catch (Exception e2) {
                System.out.println(e2.getMessage());
                this.logger.severe(e2.getMessage());
            }

        }
    }

    // Methods

}
