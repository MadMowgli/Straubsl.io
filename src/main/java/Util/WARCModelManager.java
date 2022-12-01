package Util;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Models.WARCModel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class WARCModelManager {

    // Fields
    private ConfigurationManager configManager;
    private Logger logger;


    // Constructor
    public WARCModelManager(ConfigurationManager configManager, Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
    }


    // Methods

    // Serialize arraylist of models
    public void serializeModels(WARCModel[] models, String fileName) {
        // Create Directory if not exists
        try {
            String dirPath = System.getProperty("user.dir") + (String) configManager.properties.getProperty("Files.Path.Models");
            Files.createDirectories(Paths.get(dirPath));
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }

        // Write object to file
        String dirPath = System.getProperty("user.dir") + (String) configManager.properties.getProperty("Files.Path.Models");
        String filePath = dirPath + fileName;
        try(ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filePath))) {
            outputStream.writeObject(models);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // TODO: Implement method to load serialized models
    public WARCModel[] loadModels(String fileName) {

        // Load models from file
        String dirPath = System.getProperty("user.dir") + (String) configManager.properties.getProperty("Files.Path.Models");
        String filePath = dirPath + fileName;

        try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(filePath))) {
            return (WARCModel[]) objectInputStream.readObject();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        return null;

    }

}
