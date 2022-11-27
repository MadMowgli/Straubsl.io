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

    public Matrix normalizeVectors(Matrix matrix) {

        Matrix normalizedMatrix = (Matrix) matrix.clone();

        // Normalize each column-vector of given matrix
        // Normalization of element e = e / vector magnitude
        // Vector magnitude = sqrt((e_i)^2 + , ..., + (e_n)^2)
        int rowNum = normalizedMatrix.getRowDimension();
        int colNum = normalizedMatrix.getColumnDimension();

        // matrix.get(row, col);
        // matrix.get(m, n);
        // Calc magnitude of each column vector
        double squareSum = 0;
        for(int col = 0; col < colNum; col++) {

            // Calculate square sum
            for(int row = 0; row < rowNum; row++) {
                squareSum += Math.pow(normalizedMatrix.get(row, col), 2);
            }

            // Normalize vector
            double vectorMagnitude = Math.sqrt(squareSum);
            for(int row = 0; row < rowNum; row++) {
                double normalizedElement =
                        Double.isNaN(normalizedMatrix.get(row, col) / vectorMagnitude)
                                ? 0 : normalizedMatrix.get(row, col) / vectorMagnitude; // Check for NaN
                normalizedMatrix.set(row, col, normalizedElement);
            }

            squareSum = 0;

        }
        return normalizedMatrix;
    }

}
