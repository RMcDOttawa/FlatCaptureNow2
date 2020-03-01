/**
 * Information about a filter in use and to be used for flat-acquisition
 */
public class FilterSpec {
    private Integer slotNumber;
    private String name;

    public FilterSpec(Integer slotNumber, String name) {
        this.slotNumber = slotNumber;
        this.name = name;
    }

    public Integer getSlotNumber() { return slotNumber; }
    public void setSlotNumber(Integer slotNumber) { this.slotNumber = slotNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
