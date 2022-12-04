package PreProcessor.Models;

import PreProcessor.Driver;
import Util.StringCleaner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

public class WARCModel implements Serializable {

    // Fields
    private String targetUri;
    private String date;
    private ArrayList<String> content;
    private final ArrayList<String> languages;



    // Constructor
    public WARCModel(String targetUri, String date, ArrayList<String> content, ArrayList<String> languages) {
        this.targetUri = targetUri;
        this.date = date;
        this.content = content;
        this.languages = languages;
    }

    // Custom methods
    public String[] getUniqueTerms() {
        ArrayList<String> terms = new ArrayList<>();
        ArrayList<String> uniques = new ArrayList<>();

        // First, split content up nto words
        terms = this.getTokens();

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

    public ArrayList<String> getTokens() {

        ArrayList<String> returnList = new ArrayList<>();

        for (String contentString : this.content) {
            String[] contentArray = contentString.split(" ");
            for (String in : contentArray) {
                returnList.add(StringCleaner.cleanString(in));
            }
        }

        return returnList;
    }
}
