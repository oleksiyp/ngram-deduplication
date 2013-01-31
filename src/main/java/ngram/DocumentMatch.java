package ngram;

/**
* User: Oleksiy Pylypenko
* At: 1/3/13  2:55 PM
*/
class DocumentMatch implements Comparable<DocumentMatch> {
    private int doc;
    private double howMuch;

    public DocumentMatch(int doc, double howMuch) {
        this.doc = doc;
        this.howMuch = howMuch;
    }

    @Override
    public String toString() {
        return String.format("%d(%.1f%%)", doc, howMuch * 100);
    }

    @Override
    public int compareTo(DocumentMatch o) {
        return -Double.valueOf(howMuch).compareTo(o.howMuch);
    }
}
