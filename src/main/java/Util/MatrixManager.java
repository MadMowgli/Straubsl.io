package Util;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Driver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class MatrixManager {

    // Fields
    private ConfigurationManager configManager;
    private Logger logger;

    // Constructor
    public MatrixManager(ConfigurationManager configurationManager) {
        this.configManager = configurationManager;
        this.logger = Logger.getLogger(Driver.LOGGER_NAME);
    }

    // Methods
    public void writeMatrix(Matrix matrix) {

        // Create Directory if not exists
        try {
            String dirPath = System.getProperty("user.dir") + (String) configManager.properties.getProperty("Files.Path.Matrix");
            Files.createDirectories(Paths.get(dirPath));
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }

        // Write object to file
        String dirPath = System.getProperty("user.dir") + (String) configManager.properties.getProperty("Files.Path.Matrix");
        String filePath = dirPath + "Matrix.txt";
        try(ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filePath))) {
            outputStream.writeObject(matrix);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }


    }

    public Matrix loadMatrix() {

        // Load matrix from file
        String dirPath = System.getProperty("user.dir") + (String) configManager.properties.getProperty("Files.Path.Matrix");
        String filePath = dirPath + "Matrix.txt";

        try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Matrix) objectInputStream.readObject();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        return null;

    }

}
