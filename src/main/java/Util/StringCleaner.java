package Util;

import PreProcessor.Configuration.ConfigurationManager;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class StringCleaner {

    // Fields
    private ArrayList<String> stopWords;


    // Constructor
    public StringCleaner(ConfigurationManager configurationManager) {

        // Read stop-words from file
        this.stopWords = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(
//                new FileReader(configurationManager.properties.getProperty("Files.Path.StopWords")))) {
                new FileReader(ResourceUtils.getFile("classpath:stopwords.txt")))) {
            String line = "";
            while((line = bufferedReader.readLine()) != null) {
                this.stopWords.add(line);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This function provides some basic data pre-processing (such as removing non-alphabetic characters from the string) to remove noise and duplicates in the unique term set.
     * @param input The string which needs to be cleaned.
     * @return The cleaned string.
     */
    // Methods
    public String cleanString(String input) {
        // Remove all non-alphabetic characters & make lowercase
        return input.replaceAll("[^a-zA-Z|^\s|äöüÄÖÜèéà]", "").toLowerCase();
    }

    /**
     * This function serves as a single collection of "filtering" mechanisms applied to each term to filter out noisy terms.
     * It was mainly applied to create a single point of where these filtering rules are defined rather than hardcoding across the code.
     * @param input The string which needs to be checked.
     * @return A boolean which indicates if the string meets all criteria.
     */
    public boolean shouldAddString(String input) {
        return input.length() <= 35
                && !input.isBlank()
                && !input.isEmpty()
                && !input.equalsIgnoreCase("\n")
                && !input.equalsIgnoreCase("contentlength")
                && !this.stopWords.contains(input);
    }


}
