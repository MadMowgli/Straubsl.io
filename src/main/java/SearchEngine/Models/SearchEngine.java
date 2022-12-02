package SearchEngine.Models;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Models.TermSet;
import PreProcessor.Models.WARCModel;
import PreProcessor.Models.WETReader;
import Util.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchEngine {

    // ------------------------------------------------------------------------------------------------- CONSTANTS
    public static final String LOGGER_NAME = "SpringSearch";
    public static final String LOGGER_PATH = System.getProperty("user.dir") + "/Logs/";
    public static final String WET_FILE_PATH = "data/CC-MAIN-20220924151538-20220924181538-00000.warc.wet";

    // ------------------------------------------------------------------------------------------------- FIELDS
    private Logger logger;
    private ConfigurationManager configurationManager;
    private PerformanceTimer performanceTimer;
    private TermSet termSet;
    private MatrixManager matrixManager;
    private Matrix normalizedMatrix;
    private WETReader wetReader;
    private WARCModel[] models;

    // ------------------------------------------------------------------------------------------------- CONSTRUCTOR
    public SearchEngine() {

        // Configure logger
        this.configureLogger();

        // Initialize stuff
        this.logger = Logger.getLogger(LOGGER_NAME);
        this.configurationManager = new ConfigurationManager();
        this.performanceTimer = new PerformanceTimer();
        this.termSet = new TermSet(configurationManager);
        this.matrixManager = new MatrixManager(configurationManager);
        this.wetReader = new WETReader();

        // Read unique terms from preprocessed file
        performanceTimer.start("loadTermSet");
        termSet.readGlobaltermSet(configurationManager.privateProperties.getProperty("Files.Path.LastTermSet"));
        performanceTimer.stop("loadTermSet");

        // Load normalized Matrix
        performanceTimer.start("loadMatrix");
        this.normalizedMatrix = matrixManager.loadMatrix("normalizedMatrix");
        performanceTimer.stop("loadMatrix");

        // Normalize matrix - already done.
//        this.performanceTimer.start("normalizeMatrix");
//        this.normalizedMatrix = matrixManager.normalizeVectors(this.matrix);
//        this.matrixManager.writeMatrix(this.normalizedMatrix, "normalizedMatrix");
//        this.performanceTimer.start("normalizeMatrix");

        // Read input & convert each WARC-section to an object
        performanceTimer.start("loadWarcModels");
        WARCModelManager modelManager = new WARCModelManager(configurationManager, logger);
        int splitter = Integer.parseInt((String) configurationManager.properties.get("Data.Splitter"));
        this.models = modelManager.loadModels("models_" + splitter);
        performanceTimer.stop("loadWarcModels");
        logger.info("Number of total Models: " + this.models.length);

        // Check initialization
        this.logger.info("SearchEngine initialized - " + normalizedMatrix.getRowDimension() + "x" + normalizedMatrix.getColumnDimension());

    }

    // ------------------------------------------------------------------------------------------------- METHODS
    public ArrayList<WARCModel> search(SearchQuery query) {

        this.performanceTimer.start("transformQuery");
        // Clean query
        query.setQueryString(StringCleaner.cleanString(query.getQueryString()));

        double[] frequencyVector = new double[this.normalizedMatrix.getRowDimension()];
        ArrayList<String> queryArray = new ArrayList<>(Arrays.asList(query.getQueryString().split(" ")));
        String[] termSetArray = this.termSet.getUniqueTermsAsArray();

        for(int i = 0; i < this.normalizedMatrix.getRowDimension(); i++) {
            frequencyVector[i] = Collections.frequency(queryArray, termSetArray[i]);
        }
        double[] searchVector = matrixManager.normalizeVector(frequencyVector);
        this.performanceTimer.stop("transformQuery");

        // Perform vector search
        // TODO: I think we're messing up during the multiplication?
        this.performanceTimer.start("vectorSearch");
        Matrix searchMatrix = new Matrix(searchVector, 1);
        Matrix resultMatrix = searchMatrix.times(this.normalizedMatrix);
        this.performanceTimer.stop("vectorSearch");

        // Get indices (corresponds to documents) with highest value
        // https://stackoverflow.com/a/39819177/10765169
        // TODO: We only get highest index atm
        double[] resultVector = resultMatrix.getRowPackedCopy();
        double max = Arrays.stream(resultVector).max().orElse(-1);
        int index = 0;
        for(int i = 0; i < resultVector.length; i++) {
            if(resultVector[i] == max) {
                index = i;
                break;
            }
        }

        // Match index with models
        // return this.models[index];

        // Dummy object
        return new ArrayList<WARCModel>(Arrays.asList(this.models).subList(0, 10));

    }



    public void configureLogger() {

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
