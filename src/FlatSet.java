/**
 * Description of one set of flat frames (a number of frames with identical specifications)
 */
public class FlatSet {
    private int         numberOfFrames;
    private int         numberDone;
    private FilterSpec  filterSpec;
    private int         binning;
    private AppPreferences preferences;

    /**
     * Constructor for a set with given parameters
     * @param numberWanted      Number of frames to collect
     * @param filter            Filter to be used for collection
     * @param binning           Binning level to be used for collection
     * @param preferences       Application preferences (for saveing exposure estimates)
     */
    public FlatSet(int numberWanted, FilterSpec filter, int binning, AppPreferences preferences) {
        this.numberDone = 0;
        this.numberOfFrames = numberWanted;
        this.filterSpec = filter;
        this.binning = binning;
        this.preferences = preferences;
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
     * @return (String)     Description of this set suitable for console log message
     */
    public String describe() {
        return String.format("%d %s flats binned %d x %d",
                this.numberOfFrames, this.filterSpec.getName(), this.binning, this.binning);
    }

    /**
     * Get a good guess at an appropriate exposure for this frame set by looking in the saved
     * preferences for what we used the last time we exposed frames with these specifications.
     * (Let the preferences provide a default value if we have never done this before.)
     * @return (double)     Exposure estimate in seconds
     */
    public double getEstimatedExposure() {
        System.out.println("getEstimatedExposure");
        double exposure = this.preferences.getInitialExposure(this.filterSpec.getSlotNumber(), this.binning);
        System.out.println("  Returns " + exposure);
        return exposure;
    }

    /**
     * A frame has been acquired with an acceptable ADU level.  Remember the exposure that did that,
     * in the app preferences, for using as an initial estimate next time
     * @param exposureSeconds
     */
    public void rememberSuccessfulExposure(double exposureSeconds) {
        System.out.println(String.format("rememberSuccessfulExposure(%f)", exposureSeconds));
        this.preferences.setInitialExposure(this.filterSpec.getSlotNumber(), this.binning, exposureSeconds);
    }
}
