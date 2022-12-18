package SearchEngine.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class Index {

    // Fields
    private HashMap<Integer, IndexItem> items;

    // Constructor
    public Index(double[] cosineSimilarities) {
        this.items = new HashMap<>();
        // Need to provide initial values so we can compare against something
        for(int i = 1; i < 11; i++) {
            this.items.put(i, new IndexItem(0, 0));
        }

        // Get top 10 from cosineSimilarities
        for(int i = 0; i < cosineSimilarities.length; i++) {
            double value = cosineSimilarities[i];
            for(int j = 1; j < 11; j++) {
                if(value > items.get(j).getScore()) {
                    shuffle(j);
                    items.put(j, new IndexItem(i, value));
                    break;
                }
            }

        }
    }

    // Custom Methods
    private void shuffle(int startingPosition) {
        for(int i = 10; i > startingPosition; i--) {
            items.put(i, items.get(i - 1));
        }
    }

    // Getter & Setter
    public HashMap<Integer, IndexItem> getItems() {
        return items;
    }
}
