package PreProcessor.Runnables;

import PreProcessor.Models.TermSet;

import java.util.ArrayList;

public class FilterChunkRunnable implements Runnable {

    // Fields
    String[] stringArray;
    TermSet parent;

    // Constructor
    public FilterChunkRunnable(String[] stringArray, TermSet parent) {
        this.stringArray = stringArray;
        this.parent = parent;
    }

    @Override
    public void run() {

        // Sort out uniques
        ArrayList<String> returnList = new ArrayList<>();
        for(String term : stringArray) {
            if(!returnList.contains(term)) {
                returnList.add(term);
            }
        }

//        // Sort list alphabetically to enhance global unique finding
//        returnList.sort(String.CASE_INSENSITIVE_ORDER);

        // Pass them to parent
        parent.addChunkUniqueTerms(returnList.toArray(new String[0]));

    }
}

