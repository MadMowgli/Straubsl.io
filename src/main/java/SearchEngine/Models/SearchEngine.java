package SearchEngine.Models;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Models.TermSet;
import PreProcessor.Models.WARCModel;
import PreProcessor.Models.WETReader;
import Util.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchEngine {

    // ------------------------------------------------------------------------------------------------- CONSTANTS
    public static final String LOGGER_NAME = "SearchEngine";
    public static final String LOGGER_PATH = System.getProperty("user.dir") + "/Logs/";
    public static final String WET_FILE_PATH = "data/CC-MAIN-20220924151538-20220924181538-00000.warc.wet";
    private final StringCleaner stringCleaner;

    // ------------------------------------------------------------------------------------------------- FIELDS
    private Logger logger;
    private ConfigurationManager configurationManager;
    private PerformanceTimer performanceTimer;
    private TermSet termSet;
    private MatrixManager matrixManager;

    private Matrix documentTermMatrix;
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
        this.wetReader = new WETReader(configurationManager);
        this.stringCleaner = new StringCleaner(configurationManager);
        int splitter = Integer.parseInt((String) configurationManager.properties.get("Data.Splitter"));

        // Read unique terms from preprocessed file
        performanceTimer.start("loadTermSet");
        termSet.readGlobaltermSet("termset_" + splitter);
        performanceTimer.stop("loadTermSet");

        // Load normalized Matrix
        performanceTimer.start("loadMatrix");
//        this.documentTermMatrix = matrixManager.loadMatrix("matrix_" + splitter);
        this.documentTermMatrix = matrixManager.loadMatrix("LSA_" + splitter);
        performanceTimer.stop("loadMatrix");

        // Normalize matrix - we don't need this since we normalize the vectors on the fly
        // TODO: Maybe normalize it beforehand? Gives a performance boost...
//        this.performanceTimer.start("normalizeMatrix");
//        this.normalizedMatrix = matrixManager.normalizeVectors(this.matrix);
//        this.matrixManager.writeMatrix(this.normalizedMatrix, "normalizedMatrix");
//        this.performanceTimer.start("normalizeMatrix");

        // Read input & convert each WARC-section to an object
        performanceTimer.start("loadWarcModels");
        WARCModelManager modelManager = new WARCModelManager(configurationManager, logger);
        this.models = modelManager.loadModels("models_" + splitter);
        performanceTimer.stop("loadWarcModels");
        logger.info("Number of total Models: " + this.models.length);

        // Check initialization
        this.logger.info("SearchEngine initialized - " + documentTermMatrix.getRowDimension() + "x" + documentTermMatrix.getColumnDimension());

    }

    // ------------------------------------------------------------------------------------------------- METHODS
    public ArrayList<WARCModel> search(SearchQuery query) {

        this.performanceTimer.start("transformQuery");
        // Clean query
        query.setQueryString(stringCleaner.cleanString(query.getQueryString()));

        // Transform the search query into a "document" by counting how many times each word of the term set appears in the query
        double[] frequencyVector = new double[this.documentTermMatrix.getRowDimension()];
        ArrayList<String> queryArray = new ArrayList<>(Arrays.asList(query.getQueryString().split(" ")));
        String[] termSetArray = this.termSet.getUniqueTermsAsArray();

        for(int i = 0; i < this.documentTermMatrix.getRowDimension(); i++) {
            frequencyVector[i] = Collections.frequency(queryArray, termSetArray[i]);
        }

        this.performanceTimer.stop("transformQuery");

        // Perform the cosine-similarity operation between the query-vector and each document in the matrix to obtain the documents which are the closest to the query
        this.performanceTimer.start("cosineSearch");
        double[] cosineResult = matrixManager.getCosineSimilarity(frequencyVector, documentTermMatrix);
        this.performanceTimer.stop("cosineSearch");

        // Get indices (corresponds to documents) with highest value
        // https://stackoverflow.com/a/39819177/10765169
        // TODO: We only get highest index atm

        // Get all indexes higher than 0
        HashMap<Integer, Double> indexes = new HashMap<>();
        for(int i = 0; i < cosineResult.length; i++) {
            if(cosineResult[i] > 0) {
                indexes.put(i, cosineResult[i]);
            }
        }

        // Sort them, highest on top
        LinkedHashMap<Integer, Double> linkedHashMap = new LinkedHashMap<>();
        indexes.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> linkedHashMap.put(x.getKey(), x.getValue()));

        // Get highest 10 indexes
        ArrayList<WARCModel> returnList = new ArrayList<>();
        int count = 0;
        for(Integer index : linkedHashMap.keySet()) {
            count ++;
            returnList.add(models[index]);
            if(count == 10) {
                break;
            }
        }


        // Match index with models

        return returnList;

        // Dummy object
        // return new ArrayList<WARCModel>(Arrays.asList(this.models).subList(0, 10));

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
