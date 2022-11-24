package PreProcessor;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Models.TermSet;
import PreProcessor.Models.WARCModel;
import PreProcessor.Models.WETReader;
import PreProcessor.Runnables.LocalUniquesRunnable;
import Util.LogFormatter;
import Util.MatrixManager;
import Util.PerformanceTimer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Driver {

    // Fields
    public static final String LOGGER_NAME = "PreProcessor";
    private static final String LOGGER_PATH = System.getProperty("user.dir") + "/Logs/";
    private static final String WET_FILE_PATH = "data/CC-MAIN-20220924151538-20220924181538-00000.warc.wet";

    // Main method
    public static void main(String[] args) {

        // Setup
        configureLogger();
        Logger logger = Logger.getLogger(LOGGER_NAME);
        ConfigurationManager configurationManager = new ConfigurationManager();
        PerformanceTimer performanceTimer = new PerformanceTimer();
        TermSet termSet = new TermSet(configurationManager);
        WETReader wetReader = new WETReader();
        MatrixManager matrixManager = new MatrixManager(configurationManager);

        // Read unique terms from file to save time
        // termSet.readGlobaltermSet(configurationManager.privateProperties.getProperty("Files.Path.LastTermSet"));


        // Read input & convert each WARC-section to an object CHINESE
        performanceTimer.start("loadWarcModels");
        String[] cont = wetReader.readLines(WET_FILE_PATH);
        WARCModel[] models_eur = wetReader.toEurModelArray(cont);
        performanceTimer.stop("loadWarcModels");
        logger.info("Number of total models: " + models_eur.length);

        // We have to split our model-array to avoid OutOfMemoryError when creating Matrix
        int splitter = Integer.parseInt((String) configurationManager.properties.get("Data.Splitter"));
        WARCModel[] models_chunk = Arrays.copyOfRange(models_eur, 0, models_eur.length/splitter);

        // Unload content from RAM
        cont = null;
        models_eur = null;

        // Step 1: Get all the unique terms from the models_eur ("local" uniques)
        performanceTimer.start("getLocalUniques");
        try(ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt( (String) configurationManager.properties.get("MaxThreads.LocalUniques")))) {
            for(WARCModel model : models_chunk) {
                executorService.submit(new LocalUniquesRunnable(model, termSet));
            }
        } catch (Exception e) { logger.severe(e.getMessage()); }
        performanceTimer.stop("getLocalUniques");

        // Step 2: Get uniques from total of terms  ("global" uniques)
        performanceTimer.start("getGlobalUniques");
        termSet.sortTermSet();
        performanceTimer.stop("getGlobalUniques");
        logger.info("getGlobalUniques: " + performanceTimer.getRecords().get("getGlobalUniques"));

        // Save the global TermSet
        termSet.writeGlobalTermSet();

        System.out.println(termSet.getTermSetAsArray().length);

         /* Create document-term-matrix
                X-Axis (n): models_eur.length
                Y-Axis (m): termSet.size()
          */
        performanceTimer.start("createMatrixArray");
        double[][] values = new double[termSet.getUniqueTerms().size()][models_chunk.length];
        int m = 0;
        int n = 0;
        for(WARCModel model : models_chunk) {
            m = 0;
            for(String term : termSet.getUniqueTerms()) {
                values[m][n] = Collections.frequency(model.getContent(), term);
                m++;
            }
            n++;
        }
        performanceTimer.stop("createMatrixArray");

        // Form it into JAMA matrix
        performanceTimer.start("createMatrix");
        Matrix documentTermMatrix = new Matrix(values);
        performanceTimer.stop("createMatrix");

        // Write content of matrix down
        performanceTimer.start("writeMatrix");
        matrixManager.writeMatrix(documentTermMatrix);  // Matrix with data.splitter=4 is 11Gb big...
        performanceTimer.stop("writeMatrix");

        // Load Matrix
        Matrix matrix = matrixManager.loadMatrix();

        System.out.println("debug");

    }

    public static void configureLogger() {

        // Create logging dir if not exists
        try {
            Files.createDirectories(Paths.get(LOGGER_PATH));
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize logging: " + e.getMessage());
        }

        Logger logger = Logger.getLogger(LOGGER_NAME);
        logger.setLevel(Level.INFO);
        try{
            Handler logHandler = new FileHandler(LOGGER_PATH + LOGGER_NAME + "_%u" + "_%g" + ".log", 1000000, 3, true);
            LogFormatter logFormatter = new LogFormatter();
            logHandler.setLevel(Level.FINE);
            logHandler.setFormatter(logFormatter);
            logger.addHandler(logHandler);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize logging: " + e.getMessage());
        }
    }

}
