import java.io.Serializable;

/**
 * Information about a given binning setting.  The binning value, and how or if it is available
 */
public class BinningSpec implements Serializable {
    private Integer binningValue;

    private BinningAvailability availability;

    public BinningSpec() { }

    /**
     * Constructor for a spec with given values
     * @param binningValue          Binning value (1, 2, 3, or 4)
     * @param availability          Code for whether this binning value is available, default, or hidden
     */
    public BinningSpec(int binningValue, BinningAvailability availability) {
        assert (binningValue >= 1) && (binningValue <= 4);
        this.binningValue = binningValue;
        this.availability = availability;
    }

    //  Getters and setters

    public Integer getBinningValue() { return binningValue; }
    public void setBinningValue(Integer binningValue) { this.binningValue = binningValue; }

    public BinningAvailability getAvailability() { return availability; }
    public void setAvailability(BinningAvailability availability) { this.availability = availability; }

}
