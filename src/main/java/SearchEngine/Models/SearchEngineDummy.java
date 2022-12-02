package SearchEngine.Models;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Models.TermSet;
import PreProcessor.Models.WARCModel;
import PreProcessor.Models.WETReader;
import Util.MatrixManager;
import Util.PerformanceTimer;
import Util.StringCleaner;
import Util.WARCModelManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

public class SearchEngineDummy {


    private final ConfigurationManager configurationManager;
    private final TermSet termSet;
    private final WETReader wetReader;
    private final WARCModel[] models;

    // Constructor
    public SearchEngineDummy() {
        // Initialize stuff
        this.configurationManager = new ConfigurationManager();
        this.termSet = new TermSet(configurationManager);
        this.wetReader = new WETReader();
        WARCModelManager modelManager = new WARCModelManager(configurationManager, Logger.getLogger(""));
        this.models = modelManager.loadModels("models");
    }

    public ArrayList<WARCModel> search(SearchQuery query) {
        // Dummy object
        return new ArrayList<WARCModel>(Arrays.asList(this.models).subList(0, 10));

    }

}
