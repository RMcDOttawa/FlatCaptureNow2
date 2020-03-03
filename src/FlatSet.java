/**
 * Description of one set of flat frames (a number of frames with identical specifications)
 */
public class FlatSet {
    int         numberOfFrames = 0;
    int         numberDone = 0;
    FilterSpec  filterSpec = null;
    int         binning = 0;

    /**
     * Constructor for a set with given parameters
     * @param numberWanted      Number of frames to collect
     * @param filter            Filter to be used for collection
     * @param binning           Binning level to be used for collection
     */
    public FlatSet(int numberWanted, FilterSpec filter, int binning) {
        this.numberDone = 0;
        this.numberOfFrames = numberWanted;
        this.filterSpec = filter;
        this.binning = binning;
    }

    //  Getters and setters

    public int getNumberOfFrames() { return numberOfFrames; }
    public void setNumberOfFrames(int numberOfFrames) { this.numberOfFrames = numberOfFrames; }

    public int getNumberDone() { return numberDone; }
    public void setNumberDone(int numberDone) { this.numberDone = numberDone; }

    public FilterSpec getFilterSpec() { return filterSpec; }
    public void setFilterSpec(FilterSpec filterSpec) { this.filterSpec = filterSpec; }

    public int getBinning() { return binning; }
    public void setBinning(int binning) { this.binning = binning; }

    /**
     * Brief string description of this set suitable for displaying in session console
     * @return
     */
    public String describe() {
        return String.format("%d %s flats binned %d x %d",
                this.numberOfFrames, this.filterSpec.getName(), this.binning, this.binning);
    }
}
