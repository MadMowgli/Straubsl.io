package PreProcessor.Runnables;

import PreProcessor.Models.TermSet;
import PreProcessor.Models.WARCModel;

public class LocalUniquesRunnable implements Runnable{
    // Fields
    private WARCModel warcModel;
    private TermSet termSet;

    // Constructor
    public LocalUniquesRunnable(WARCModel model, TermSet termSet) {
        this.warcModel = model;
        this.termSet = termSet;
    }

    @Override
    public void run() {
        String[] uniqueTerms = warcModel.getUniqueTerms();
        this.termSet.addTerms(uniqueTerms);
    }
}
