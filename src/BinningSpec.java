import java.io.Serializable;

/**
 * Information about a given binning setting.  The binning value, and how or if it is available
 */
public class BinningSpec implements Serializable {
    private Integer binningValue;

    private BinningAvailability availability;

    public BinningSpec() { }

    public BinningSpec(int binningValue, BinningAvailability availability) {
        this.binningValue = binningValue;
        this.availability = availability;
    }

    public Integer getBinningValue() { return binningValue; }
    public void setBinningValue(Integer binningValue) { this.binningValue = binningValue; }

    public BinningAvailability getAvailability() { return availability; }
    public void setAvailability(BinningAvailability availability) { this.availability = availability; }

}
