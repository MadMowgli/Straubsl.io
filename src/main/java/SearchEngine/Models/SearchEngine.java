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

public class SearchEngine {

    // ------------------------------------------------------------------------------------------------- CONSTANTS
    public static final String LOGGER_NAME = "SearchEngine";
    public static final String LOGGER_PATH = System.getProperty("user.dir") + "/Logs/";
    private final StringCleaner stringCleaner;

    // ------------------------------------------------------------------------------------------------- FIELDS
    private Logger logger;
    private ConfigurationManager configurationManager;
    private PerformanceTimer performanceTimer;
    private TermSet termSet;
    private MatrixManager matrixManager;

    private Matrix documentTermMatrix;
    private Matrix termConceptMatrix;
    private Matrix documentConceptMatrix;

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

        this.stringCleaner = new StringCleaner(configurationManager);
        int splitter = Integer.parseInt((String) configurationManager.properties.get("Data.Splitter"));

        // Read unique terms from preprocessed file
        performanceTimer.start("loadTermSet");
        termSet.readGlobaltermSet("termset_eur_https_" + splitter);
        performanceTimer.stop("loadTermSet");

        // Load normalized Matrix
        performanceTimer.start("loadMatrix");
//        this.documentTermMatrix = matrixManager.loadMatrix("matrix_eur_https" + splitter);
//        this.documentTermMatrix = matrixManager.loadMatrix("LSA_" + splitter);

        // Load SVD matrices
        this.termConceptMatrix = matrixManager.loadMatrix("svd_u_" + splitter);
        this.documentConceptMatrix = matrixManager.loadMatrix("svd_v_" + splitter);
        performanceTimer.stop("loadMatrix");

        // Read input & convert each WARC-section to an object
        performanceTimer.start("loadWarcModels");
        WARCModelManager modelManager = new WARCModelManager(configurationManager, logger);
        this.models = modelManager.loadModels("models_eur_https_" + splitter);
        performanceTimer.stop("loadWarcModels");
        logger.info("Number of total Models: " + this.models.length);

        // Check initialization
        this.logger.info("SearchEngine initialized");

    }

    // ------------------------------------------------------------------------------------------------- METHODS
    public ArrayList<WARCModel> search(SearchQuery query) {

        // Transform the search query into a "document" by counting how many times each word of the term set appears in the query
        this.performanceTimer.start("transformQuery");
        double[] frequencyVector = this.transformQueryToVector(query);
        this.performanceTimer.stop("transformQuery");

        // Perform the cosine-similarity operation between the query-vector and each document in the matrix to obtain the documents which are the closest to the query
        this.performanceTimer.start("cosineSearch");
        double[] cosineResult = matrixManager.getCosineSimilarity(frequencyVector, documentTermMatrix);
        this.performanceTimer.stop("cosineSearch");

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

        return returnList;

    }

    public ArrayList<WARCModel> performLSASearch(SearchQuery query) {

        // Step 0: Create a query-vector by counting how many times each word of the term set appears in the query
        this.performanceTimer.start("transformQuery");
        double[] frequencyVector = this.transformQueryToVector(query);
        this.performanceTimer.stop("transformQuery");

        // Step 1: Find the concepts of the term-concept matrix that correspond with the query by multiplying the query vector with the term-concept matrix
        Matrix queryMatrix = new Matrix(frequencyVector, 1);
        Matrix queryConceptMatrix = queryMatrix.times(this.termConceptMatrix);

        // Step 2: Find the documents of the document-concept matrix that correspond with the concepts of step 1 by using cosine similarity
        double[] cosineSimilarities = matrixManager.getCosineSimilarity(queryConceptMatrix.getArray()[0], documentConceptMatrix);

        // Step 3: Sort these documents so that the documents with the strongest concept-match is on top
        Index index = new Index(cosineSimilarities);

        // Step 4: Form ArrayList of WARCModels according to the topTenMap
        ArrayList<WARCModel> returnList = new ArrayList<WARCModel>();
        for(int i = 1; i < 11; i++) {
            returnList.add(models[index.getItems().get(i).getIndex()]);
        }

        return returnList;

    }

    public double[] transformQueryToVector(SearchQuery query) {
        this.performanceTimer.start("transformQuery");
        // Clean query
        query.setQueryString(stringCleaner.cleanString(query.getQueryString()));

        // Transform the search query into a "document" by counting how many times each word of the term set appears in the query
        double[] frequencyVector = new double[this.termConceptMatrix.getRowDimension()];
        ArrayList<String> queryArray = new ArrayList<>(Arrays.asList(query.getQueryString().split(" ")));
        String[] termSetArray = this.termSet.getUniqueTermsAsArray();

        for(int i = 0; i < this.termConceptMatrix.getRowDimension(); i++) {
            frequencyVector[i] = Collections.frequency(queryArray, termSetArray[i]);
        }

        this.performanceTimer.stop("transformQuery");
        return frequencyVector;
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
