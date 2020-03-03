import java.io.Serializable;

/**
 * Information about a filter in use and to be used for flat-acquisition
 */
public class FilterSpec implements Serializable {

    private Integer slotNumber;
    private String name;

    public FilterSpec() {}

    /**
     * Constructor for a spec with given settings
     * @param slotNumber        1-based filter wheel slot number
     * @param name              Name of filter at this location
     */
    public FilterSpec(Integer slotNumber, String name) {
        this.slotNumber = slotNumber;
        this.name = name;
    }

    //  Getters and setters
    public Integer getSlotNumber() { return slotNumber; }
    public void setSlotNumber(Integer slotNumber) { this.slotNumber = slotNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
