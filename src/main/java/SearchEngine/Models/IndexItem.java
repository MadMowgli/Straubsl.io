package SearchEngine.Models;

public class IndexItem {

    // Fields
    private int index;
    private double score;

    // Constructor
    public IndexItem(int index, double score) {
        this.index = index;
        this.score = score;
    }

    // Getter & Setter
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
}
