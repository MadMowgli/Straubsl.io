package SearchEngine.Models;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Models.TermSet;
import Util.JamaUtils;
import Util.LogFormatter;
import Util.MatrixManager;
import Util.PerformanceTimer;

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
    private Matrix matrix;
    private Matrix normalizedMatrix;

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

        // Read unique terms from preprocessed file
        performanceTimer.start("loadTermSet");
        termSet.readGlobaltermSet(configurationManager.privateProperties.getProperty("Files.Path.LastTermSet"));
        performanceTimer.stop("loadTermSet");

        // Load Matrix
        performanceTimer.start("loadMatrix");
        this.matrix = matrixManager.loadMatrix();
        performanceTimer.stop("loadMatrix");

        // Normalize matrix
        this.performanceTimer.start("normalizeMatrix");
        this.normalizedMatrix = matrixManager.normalizeVectors(this.matrix);
        this.performanceTimer.start("normalizeMatrix");

        // Check initialization
        this.logger.info("SearchEngine initialized - " + matrix.getRowDimension() + "x" + matrix.getColumnDimension());

    }

    // ------------------------------------------------------------------------------------------------- METHODS
    public void search(SearchQuery query) {

        this.performanceTimer.start("transformQuery");
        double[] frequencyVector = new double[this.matrix.getRowDimension()];
        ArrayList<String> queryArray = new ArrayList<>(Arrays.asList(query.getQueryString().split(" ")));
        String[] termSetArray = this.termSet.getUniqueTermsAsArray();

        for(int i = 0; i < this.matrix.getRowDimension(); i++) {
            frequencyVector[i] = Collections.frequency(queryArray, termSetArray[i]);
        }
        double[] searchVector = matrixManager.normalizeVector(frequencyVector);
        this.performanceTimer.stop("transformQuery");

        // Perform vector search
        this.performanceTimer.start("vectorSearch");
        Matrix searchMatrix = new Matrix(searchVector, 1);
        Matrix resultMatrix = searchMatrix.times(this.normalizedMatrix);
        this.performanceTimer.stop("vectorSearch");

        // Get indices (corresponds to documents) with highest value
        // https://stackoverflow.com/a/39819177/10765169
        // TODO: We only get highest index atm
        double[] resultVector = resultMatrix.getColumnPackedCopy();
        double max = Arrays.stream(resultVector).max().orElse(-1);
        int index = 0;
        for(int i = 0; i < resultVector.length; i++) {
            if(resultVector[i] == max) {
                index = i;
                break;
            }
        }

        // TODO: We need to load in all documents so we can match the index with the documents, lol

        System.out.println(index);

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
