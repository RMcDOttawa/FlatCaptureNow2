/**
 * Description of one set of flat frames (a number of frames with identical specifications)
 */
public class FlatSet {
    int         numberOfFrames = 0;
    int         numberDone = 0;
    FilterSpec  filterSpec = null;
    int         binning = 0;

    public FlatSet(int numberWanted, FilterSpec filter, int binning) {
        this.numberDone = 0;
        this.numberOfFrames = numberWanted;
        this.filterSpec = filter;
        this.binning = binning;
    }

    public int getNumberOfFrames() { return numberOfFrames; }
    public void setNumberOfFrames(int numberOfFrames) { this.numberOfFrames = numberOfFrames; }

    public int getNumberDone() { return numberDone; }
    public void setNumberDone(int numberDone) { this.numberDone = numberDone; }

    public FilterSpec getFilterSpec() { return filterSpec; }
    public void setFilterSpec(FilterSpec filterSpec) { this.filterSpec = filterSpec; }

    public int getBinning() { return binning; }
    public void setBinning(int binning) { this.binning = binning; }

}
