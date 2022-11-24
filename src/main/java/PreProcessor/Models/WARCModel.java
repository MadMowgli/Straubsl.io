package PreProcessor.Models;

import PreProcessor.Driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class WARCModel {

    // Fields
    private String targetUri;
    private String date;
    private ArrayList<String> content;
    private ArrayList<String> languages;
    private Logger logger;


    // Constructor
    public WARCModel(String targetUri, String date, ArrayList<String> content, ArrayList<String> languages) {
        this.targetUri = targetUri;
        this.date = date;
        this.content = content;
        this.languages = languages;
        this.logger = Logger.getLogger(Driver.LOGGER_NAME);
    }

    // Custom methods
    public String[] getUniqueTerms() {
        ArrayList<String> terms = new ArrayList<>();
        ArrayList<String> uniques = new ArrayList<>();

        // First, split content up nto words
        for(String line : this.content) {
            terms.addAll(Arrays.asList(line.split(" ")));
        }

        // Then, create an array of unique terms
        for(String term : terms) {
            if(!uniques.contains(term) && !term.isBlank()) {
                uniques.add(term);
            }
        }

        return uniques.toArray(new String[uniques.size()]);
    }


    // Getter & Setter
    public String getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<String> getContent() {
        return content;
    }

    public void setContent(ArrayList<String> content) {
        this.content = content;
    }

    public ArrayList<String> getLanguages() {
        return languages;
    }
}
