package Util;

public class StringCleaner {

    // Fields

    // Constructor

    /**
     * This function provides some basic data pre-processing (such as removing non-alphabetic characters from the string) to remove noise and duplicates in the unique term set.
     * @param input The string which needs to be cleaned.
     * @return The cleaned string.
     */
    // Methods
    public static String cleanString(String input) {
        // Remove all non-alphabetic characters & make lowercase
        return input.replaceAll("[^a-zA-Z|^\s|äöüÄÖÜèéà]", "").toLowerCase();
    }

    /**
     * This function serves as a single collection of "filtering" mechanisms applied to each term to filter out noisy terms.
     * It was mainly applied to create a single point of where these filtering rules are defined rather than hardcoding across the code.
     * @param input The string which needs to be checked.
     * @return A boolean which indicates if the string meets all criteria.
     */
    public static boolean shouldAddString(String input) {
        return input.length() <= 300
                && !input.isBlank()
                && !input.equalsIgnoreCase("contentlength ");
    }

    public static boolean shouldAddTestString(String input) {
        return !input.isBlank()
                && !input.equalsIgnoreCase("contentlength ");
    }

}
