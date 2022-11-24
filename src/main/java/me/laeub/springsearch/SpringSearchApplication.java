package me.laeub.springsearch;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Models.TermSet;
import PreProcessor.Models.WETReader;
import Util.LogFormatter;
import Util.MatrixManager;
import Util.PerformanceTimer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class SpringSearchApplication {

    public static final String LOGGER_NAME = "SpringSearch";
    private static final String LOGGER_PATH = System.getProperty("user.dir") + "/Logs/";
    private static final String WET_FILE_PATH = "data/CC-MAIN-20220924151538-20220924181538-00000.warc.wet";

    public static void main(String[] args) {

        // Setup
        configureLogger();
        Logger logger = Logger.getLogger(LOGGER_NAME);
        ConfigurationManager configurationManager = new ConfigurationManager();
        PerformanceTimer performanceTimer = new PerformanceTimer();
        TermSet termSet = new TermSet(configurationManager);
        MatrixManager matrixManager = new MatrixManager(configurationManager);

        // Read unique terms from preprocessed file
        performanceTimer.start("loadTermSet");
        termSet.readGlobaltermSet(configurationManager.privateProperties.getProperty("Files.Path.LastTermSet"));
        performanceTimer.stop("loadTermSet");

        // Load Matrix
        performanceTimer.start("loadMatrix");
        Matrix matrix = matrixManager.loadMatrix();
        performanceTimer.stop("loadMatrix");


        SpringApplication.run(SpringSearchApplication.class, args);
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
