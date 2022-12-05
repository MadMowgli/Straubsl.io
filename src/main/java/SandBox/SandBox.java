package SandBox;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Models.TermSet;
import PreProcessor.Models.WARCModel;
import PreProcessor.Models.WETReader;
import PreProcessor.Runnables.LocalUniquesRunnable;
import Util.LogFormatter;
import Util.MatrixManager;
import Util.PerformanceTimer;
import Util.WARCModelManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SandBox {

    // Fields
    public static final String LOGGER_NAME = "SandBox";
    private static final String LOGGER_PATH = System.getProperty("user.dir") + "/Logs/";
    // private static final String WET_FILE_PATH = "data/CC-MAIN-20220924151538-20220924181538-00000.warc.wet";
    private static final String WET_FILE_PATH = "data/TestData.wet";

    public static void main(String[] args) {

        // Setup
        configureLogger();
        Logger logger = Logger.getLogger(LOGGER_NAME);
        ConfigurationManager configurationManager = new ConfigurationManager();
        PerformanceTimer performanceTimer = new PerformanceTimer();
        TermSet termSet = new TermSet(configurationManager);
        WETReader wetReader = new WETReader(configurationManager);
        MatrixManager matrixManager = new MatrixManager(configurationManager);
        WARCModelManager modelManager = new WARCModelManager(configurationManager, logger);
        int splitter = Integer.parseInt((String) configurationManager.properties.get("Data.Splitter"));

        // Create document-term-matrix
        performanceTimer.start("loadBagOfWords");
        Matrix documentTermMatrix = matrixManager.loadMatrix("matrix_" + splitter);
        performanceTimer.stop("loadBagOfWords");

        performanceTimer.start("createSVD");
        SingularValueDecomposition svd = documentTermMatrix.svd();
        performanceTimer.stop("createSVD");

        performanceTimer.start("writeSVDComponents");
        matrixManager.writeMatrix(svd.getV(), "svd_v");
        matrixManager.writeMatrix(svd.getU(), "svd_u");
        matrixManager.writeMatrix(svd.getS(), "svd_s");
        performanceTimer.stop("writeSVDComponents");
        performanceTimer.logStatements();
        System.out.println("debug");
        System.exit(1);

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
