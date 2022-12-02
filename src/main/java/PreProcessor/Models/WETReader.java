package PreProcessor.Models;

import PreProcessor.Driver;
import Util.StringCleaner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WETReader {

    private volatile ArrayList<String> termSet;
    private Logger logger;
    private Set<Integer> allowedASciiRange;
    private Set<String> disallowedLanguages;

    // Constructor
    public WETReader() {
        this.termSet = new ArrayList<>();
        this.logger = Logger.getLogger(Driver.LOGGER_NAME);
        this.allowedASciiRange = IntStream.rangeClosed(0, 166).boxed().collect(Collectors.toSet());
        this.disallowedLanguages = Arrays.stream(new String[] {"zho", "kor", "jpn"}).collect(Collectors.toSet());
    }

    /**
     * Takes a string-path to a file as input, reads the file contents, puts each line in an array and returns this array.
     * @param filePath Path to the file which should be read
     * @return An array, containing the line sof the read document
     */
    // Methods
    public String[] readLines(String filePath) {

        ArrayList<String> lines = new ArrayList<>();

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line = "";
            while((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            return null;
        }

        return lines.toArray(new String[lines.size()]);

    }

    /**
     * Takes a string-path to a file as input, reads the file contents, checks whether each line is free of non-european characters. If so, the line is contained in the returned array.
     * NOTE: This doesn't work properly, though. Needs adjustment of the ASCII-range.
     * @param filePath Path to the file which should be read
     * @return An array, containing the line sof the read document
     */
    public String[] readEurLines(String filePath) {
        ArrayList<String> lines = new ArrayList<>();

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line = "";
            boolean europeanLine = true;
            while((line = bufferedReader.readLine()) != null) {

                // Go over each character in the line & check whether it's free of non-european characters
                char[] charArray = line.toCharArray();
                for(char c : charArray) {
                    if (!this.allowedASciiRange.contains((int) c)) {
                        europeanLine = false;
                        break;
                    }
                }

                if(europeanLine) { lines.add(line); }

            }
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            return null;
        }

        return lines.toArray(new String[lines.size()]);
    }

    /**
     * Transforms the content of a WARC-file (read into a String[]) to an array of WARC-objects. Arrays are used to boost performance.
     * @param lines An array of strings
     * @return An array of WARCModels
     */
    public WARCModel[] toModelArray(String[] lines) {
        ArrayList<WARCModel> warcModels = new ArrayList<>();
        ArrayList<String> contentArray = new ArrayList<>();
        boolean addLines = false;
        boolean createObject = false;
        String targetUri = "";
        String date = "";
        String language_line = "";
        ArrayList<String> languages = new ArrayList<>();

        // Loop over each line & grab data to instantiate a model
        for(String line : lines) {
            // If line contains required information, save it to variables
            if(line.contains("Target-URI")) {
                contentArray = new ArrayList<>();
                languages = new ArrayList<>();
                targetUri = line.split("WARC-Target-URI: ")[1];
            } else if (line.contains("WARC-Date")) {
                date = line.split("WARC-Date: ")[1];
            } else if (line.contains("WARC-Identified-Content-Language")){
                language_line = line.split("WARC-Identified-Content-Language: ")[1];
                try{
                    languages.addAll(Arrays.asList(language_line.split(",")));

                } catch (Exception e) {
                    // Can't split, only one value contained
                    languages.add(language_line);
                }
            } else if (line.contains("Content-Length: ")) {
                // If we reached a "Content-Length" line, actual content follows until the next "WARC/1.0" line
                addLines = true;
            }

            if(addLines && !line.equals("")) {
                if(line.equals("WARC/1.0")) {
                    createObject = true;
                    addLines = false;
                } else {

                    // Basic noise filtering
                    if(StringCleaner.shouldAddString(line) &&
                            !StringCleaner.cleanString(line).equalsIgnoreCase("contentlength ")) {

                        // Clear line from non-alphabetic character / make lowercase
                        String cleanedLine = StringCleaner.cleanString(line);
                        contentArray.add(cleanedLine);
                    }
                }
            }

            if(createObject) {
                createObject = false;
                if(contentArray.size() != 0) {
                    warcModels.add(new WARCModel(targetUri, date, contentArray, languages));
                }

                targetUri = "";
                date = "";
            }

        }

        // Remove the first element, this is some commoncrawl-element
        warcModels.remove(0);
        return warcModels.toArray(new WARCModel[0]);
    }

    public WARCModel[] toEurModelArray(String[] lines) {

        WARCModel[] temp = this.toModelArray(lines);

        // Sort out foreign languages
        ArrayList<WARCModel> warcModels = new ArrayList<>();
        boolean shouldAdd = true;
        for(WARCModel model : temp) {
            for(String language : model.getLanguages()) {
                if (this.disallowedLanguages.contains(language)) {
                    shouldAdd = false;
                    break;
                }
            }
            if(shouldAdd) { warcModels.add(model); }
            shouldAdd = true;
        }


        return warcModels.toArray(new WARCModel[0]);
    }

}
