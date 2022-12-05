package Util;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Driver;
import PreProcessor.Models.WARCModel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

public class MatrixManager {

    // Fields
    private ConfigurationManager configManager;
    private Logger logger;
    private PerformanceTimer performanceTimer;

    // Constructor
    public MatrixManager(ConfigurationManager configurationManager) {
        this.configManager = configurationManager;
        this.logger = Logger.getLogger(Driver.LOGGER_NAME);
        this.performanceTimer = new PerformanceTimer();
    }

    // Methods
    // ----------------------------------------------------------------------------------------------- I/O
    public void writeMatrix(Matrix matrix, String name) {

        // Create Directory if not exists
        try {
            String dirPath = System.getProperty("user.dir") + (String) configManager.properties.getProperty("Files.Path.Matrix");
            Files.createDirectories(Paths.get(dirPath));
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }

        // Write object to file
        String dirPath = System.getProperty("user.dir") + (String) configManager.properties.getProperty("Files.Path.Matrix");
        String filePath = dirPath + name;
        try(ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filePath))) {
            outputStream.writeObject(matrix);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }


    }

    public Matrix loadMatrix(String name) {

        // Load matrix from file
        String dirPath = System.getProperty("user.dir") + (String) configManager.properties.getProperty("Files.Path.Matrix");
        String filePath = dirPath + name;
        // String filePath = dirPath + "Matrix.txt";

        try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Matrix) objectInputStream.readObject();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        return null;

    }

    // ----------------------------------------------------------------------------------------------- Maths
    public Matrix createDocumentTermMatrix(WARCModel[] documents, ArrayList<String> uniqueTerms) {
        performanceTimer.start("createMatrixArray");

        /* Create document-term-matrix
                X-Axis (n = col): models_eur.length
                Y-Axis (m = row): termSet.size()
          */
        double[][] values = new double[uniqueTerms.size()][documents.length];
        int col = 0;
        for(WARCModel model : documents) {
            int row = 0;
            for(String term : uniqueTerms) {
                values[row][col] = Collections.frequency(model.getContent(), term);
                row++;
            }
            col++;
        }

        performanceTimer.stop("createMatrixArray");

        // Form it into JAMA matrix
        performanceTimer.start("createMatrix");
        Matrix documentTermMatrix = new Matrix(values);
        performanceTimer.stop("createMatrix");

        return documentTermMatrix;
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

    public double[] normalizeVector(double[] vector) {
        double[] returnVector = new double[vector.length];
        double squaredSum = 0;

        // Calculate magnitude of vector
        for(int i = 0; i < vector.length; i++) {
            squaredSum += Math.pow(vector[i], 2);
        }
        double vectorMagnitude = Math.sqrt(squaredSum);

        // Normalize vector
        for(int i = 0; i < vector.length; i++) {
            double normalizedElement = Double.isNaN( vector[i] / vectorMagnitude) ? 0 :  vector[i] / vectorMagnitude;
            returnVector[i] = normalizedElement;
        }

        return returnVector;

    }

    public double getCosineSimilarity(double[] vector_a, double[] vector_b) {

        // Step 0: Both vectors must have the same number of elements
        if(vector_a.length != vector_b.length) {
            logger.severe("Could not calculate cosine similarity due to non-identical number of elements in each vector.");
            throw new ArithmeticException("Vector A and vector B must have the same number of elements!");
        }

        // Step 1: Calculate dot-product of each vector
        double dotProduct = 0;
        for(int i = 0; i < vector_a.length; i++) {
            dotProduct += Double.isNaN(vector_a[i] * vector_b[i]) ? 0 : vector_a[i] * vector_b[i];
        }

        // Step 2: Calculate dot-product of each normalized vector
        double[] normalized_a = this.normalizeVector(vector_a);
        double[] normalized_b = this.normalizeVector(vector_b);
        double normalizedDotProduct = 0;
        for(int i = 0; i < normalized_a.length; i++) {
            normalizedDotProduct += Double.isNaN(normalized_a[i] * normalized_b[i]) ? 0 : normalized_a[i] * normalized_b[i];
        }

        // Step 3: Divide dot-product of each vector by dot-product of each normalized vector
        return dotProduct / normalizedDotProduct;

    }

    public double[] getCosineSimilarity(double[] vector, Matrix matrix) {

        // Step 0: Vector must have the same number of elements as either matrix_m or matrix_n
        if(vector.length != matrix.getColumnDimension() && vector.length != matrix.getRowDimension()) {
            logger.severe("Could not calculate cosine similarity due to non-identical number of elements in each vector.");
            throw new ArithmeticException("Vector A must have the same number of elements as matrix.getRowDimension() or matrix.getColDimension()!");
        }

        double[] returnVector = new double[matrix.getColumnDimension()];

        try {
            // Step 1: Calculate cosine-similarity of each col/row (depending on size)
            if(vector.length == matrix.getColumnDimension()) {
                // In this case, we can work with row-vectors -> no need to transpose the matrix first
                for(int i = 0; i < matrix.getColumnDimension(); i ++) {
                    returnVector[i] = this.getCosineSimilarity(vector, matrix.getArray()[i]);
                }
            }

            if(vector.length == matrix.getRowDimension()) {
                // Here we first need to transpose the matrix, since we can't access the column vectors directly via array
                Matrix transpose = matrix.transpose();
                for(int i = 0; i < transpose.getColumnDimension() - 1; i++) {
                    returnVector[i] = this.getCosineSimilarity(vector, transpose.getArray()[i]);
                }
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }


        // The highest number in the returned vector is the index of the document the query corresponds the most with
        return returnVector;

    }

    // ----------------------------------------------------------------------------------------------- Testing
    public double getMaxValue(Matrix matrix) {
        double[][] values = matrix.getArray();
        double max = 0;
        for(int row = 0; row < matrix.getRowDimension(); row++) {
            for(int col = 0; col < matrix.getColumnDimension(); col++) {
                max = Math.max(values[row][col], max);
            }
        }
        return max;
    }

}
