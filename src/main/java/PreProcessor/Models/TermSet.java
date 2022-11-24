package PreProcessor.Models;

import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Driver;
import PreProcessor.Runnables.FilterChunkRunnable;
import Util.PerformanceTimer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class TermSet {

    private final ConfigurationManager configManager;
    // Fields
    private volatile HashMap<Character, ArrayList<String>> contentDict;
    private volatile ArrayList<String> termSet;
    private volatile ArrayList<String> uniqueTerms;
    private volatile int last;
    private volatile int counter;
    private Logger logger;
    private PerformanceTimer timer;

    // Constructor
    public TermSet(ConfigurationManager configManager) {
        this.termSet = new ArrayList<>();
        this.contentDict = new HashMap<>();
        this.uniqueTerms = new ArrayList<>();
        this.logger = Logger.getLogger(Driver.LOGGER_NAME);
        this.timer = new PerformanceTimer();
        this.configManager = configManager;

        // Create dict containing alphabet letters as keys
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        for(char c : alphabet) { contentDict.put(c, new ArrayList<>()); }

        this.counter = 100;

    }

    // Methods
    public String[] getTermSetAsArray() {
        return this.termSet.toArray(new String[this.termSet.size()]);
    }

    public synchronized void addTerms(String[] terms) {
        this.termSet.addAll(Arrays.asList(terms));
    }

    public synchronized void addChunkUniqueTerms(String[] terms) {



        // Check each sublist if term is already contained instead of looping over the whole array
        // NOTE: this also gets rid of words that don't begin with a character from the alphabet, e.g: |.- etc.

        for(String term : terms) {

            // Sort out term
            char index = term.charAt(0);
            try {
                if(!this.containsTerm(index, term)) {
                    this.addTerm(index, term);
                    counter++;
                }
            } catch (Exception ignore) {
                // first character not from ABC
            }

            // Periodically write content, we don't want to lose everything if this operation needs to be stopped
            if(counter % 10000 == 0) {
                this.writeGlobalTermSet(contentDict.values());
            }


        }

    }

    private void mergeContentDict() {

    }

    private synchronized void addTerm(char index, String term) {
        this.contentDict.get(index).add(term);
    }
    private synchronized boolean containsTerm(char index, String term) {
        return this.contentDict.get(index).contains(term);
    }
    public void sortTermSet() {

        // Split this.termSet into chunks
        ArrayList<List<String>> temp = new ArrayList<>();
        int stepSize = 100000;
        int i = 0;
        while (i < this.termSet.size()) {

            if ((i + stepSize) > termSet.size()) {
                temp.add(this.termSet.subList(i, this.termSet.size() - 1));
            } else {
                temp.add(this.termSet.subList(i, i + stepSize));
            }
            i = i + stepSize;

        }

        // Sort each chunk
        this.timer.start("sortChunks");
        int n = Integer.parseInt((String) this.configManager.properties.get("MaxThreads.GlobalUniques"));
        try(ExecutorService executorService = Executors.newFixedThreadPool(n)) {
            for(List<String> chunk : temp) {
                executorService.submit(new FilterChunkRunnable(chunk.toArray(new String[0]), this));
            }

        }catch (Exception e) { this.logger.severe(e.getMessage()); }
        this.timer.stop("sortChunks");

        // Merge everything & put it into variable
        this.timer.start("mergeContentDict");
        for(ArrayList<String> arrayList : contentDict.values()) { this.uniqueTerms.addAll(arrayList); }
        this.timer.stop("mergeContentDict");

    }

    public void writeGlobalTermSet() {

        // Construct filename
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy_hh-mm-ss");
        String dateString = dateFormat.format(date);
        String dirName = System.getProperty("user.dir") + this.configManager.properties.getProperty("Files.Path.TermSet");
        String fileName = dirName + dateString + ".txt";

        // Create logging directory if not exists
        try {
            Files.createDirectories(Paths.get(dirName));
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }

        this.timer.start("WriteOutput");
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, false))) {

            for(String term : this.uniqueTerms) {
                bufferedWriter.write(term + "\n");
            }

        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        this.timer.stop("WriteOutput");

    }

    // Method overload to periodically write content while filtering
    public synchronized void writeGlobalTermSet(Collection<ArrayList<String>> content) {

        // Construct filename
        String dirName = System.getProperty("user.dir") + this.configManager.properties.getProperty("Files.Path.TermSet");
        String fileName = dirName + "save" + ".txt";

        // Create logging directory if not exists
        try {
            Files.createDirectories(Paths.get(dirName));
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }

        this.timer.start("WriteOutput");
        try( BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, false))) {

            for(ArrayList<String> arrayList : content) {
                for(String line : arrayList) {
                    bufferedWriter.write(line + "\n");
                }
            }

        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        this.timer.stop("WriteOutput");

    }

    public void readGlobaltermSet(String filePath) {

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            this.uniqueTerms.addAll(bufferedReader.lines().toList());
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }

    }

    // Getters
    public ArrayList<String> getTermSet() {
        return termSet;
    }

    public ArrayList<String> getUniqueTerms() {
        return uniqueTerms;
    }
}
