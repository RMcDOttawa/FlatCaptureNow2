import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Model for a flats capture plan.
 */
public class DataModel  implements Serializable {
    @SuppressWarnings("unused")
    private Integer     modelVersion = 1;

    //  Instance variables that are initialized from Preferences
    private Boolean     useFilterWheel;
    private Integer     defaultFrameCount;
    private Integer     targetADUs;
    private Double      aduTolerance;
    private String      serverAddress;
    private Integer     portNumber;
    private Double      lightSourceAlt;
    private Double      lightSourceAz;
    private Boolean     ditherFlats;
    private Double      ditherRadius;
    private Double      ditherMaximum;


    //  Instance variables with simple default, not from preferences
    private Boolean     warmUpWhenDone = false;
    private Boolean     useTheSkyAutosave = true;
    private String     localPath = null;
    private Boolean     controlMount = false;
    private Boolean     homeMount = false;
    private Boolean     trackingOff = false;
    private Boolean     slewToLight = false;
    private Boolean     parkWhenDone = false;

    //  Filters that are in use for this session
    private ArrayList<FilterSpec> filtersInUse = null;

    //  Binnings that are in use or available for this session
    private ArrayList<BinningSpec> binningsInUse = null;

    //  Table to contain the data for the frame table on the main window.  Two-dimensional, where
    //  a row is all the frames for a given filter, and the columns in that row are the frames for the
    //  various binnings.  Only filters and binnings that are in use (i.e. the same ones in the above two
    //  tables) have corresponding rows and columns in this table

    private ArrayList<ArrayList<Integer>> frameTableData = null;

    // Special indexing getters for the filter and binning arrays

    /**
     * Get the filter at the given index in the "Filters In Use" list.
     * This becomes the basis for the row header of a table row.
     * @param filterIndex       Index in list
     * @return (FilterSpec)
     */
    public FilterSpec getFilterInUse(int filterIndex) {
        return this.filtersInUse.get(filterIndex);
    }

    public BinningSpec getBinningInUse(int binningIndex) { return this.binningsInUse.get(binningIndex);}

    public Integer getFrameCountAt(int rowIndex, int columnIndex) {
        ArrayList<Integer> entireRow = this.frameTableData.get(rowIndex);
        return entireRow.get(columnIndex);
    }


    public void setFrameCountAt(int rowIndex, int columnIndex, int frameCount) {
        ArrayList<Integer> entireRow = this.frameTableData.get(rowIndex);
        entireRow.set(columnIndex, Integer.valueOf(frameCount));
    }
    //  Getters and Setters

    public Boolean getUseFilterWheel() { return useFilterWheel; }
    public void setUseFilterWheel(Boolean useFilterWheel) { this.useFilterWheel = useFilterWheel; }

    public Integer getDefaultFrameCount() { return defaultFrameCount; }
    public void setDefaultFrameCount(Integer defaultFrameCount) { this.defaultFrameCount = defaultFrameCount; }

    public Integer getTargetADUs() { return targetADUs; }
    public void setTargetADUs(Integer targetADUs) { this.targetADUs = targetADUs; }

    public Double getAduTolerance() { return aduTolerance; }
    public void setAduTolerance(Double aduTolerance) { this.aduTolerance = aduTolerance; }

    public String getServerAddress() { return serverAddress; }
    public void setServerAddress(String serverAddress) { this.serverAddress = serverAddress; }

    public Integer getPortNumber() { return portNumber; }
    public void setPortNumber(Integer portNumber) { this.portNumber = portNumber; }

    public Double getLightSourceAlt() { return lightSourceAlt; }
    public void setLightSourceAlt(Double lightSourceAlt) { this.lightSourceAlt = lightSourceAlt; }

    public Double getLightSourceAz() { return lightSourceAz; }
    public void setLightSourceAz(Double lightSourceAz) { this.lightSourceAz = lightSourceAz; }

    public Boolean getDitherFlats() { return ditherFlats; }
    public void setDitherFlats(Boolean ditherFlats) { this.ditherFlats = ditherFlats; }

    public Double getDitherRadius() { return ditherRadius; }
    public void setDitherRadius(Double ditherRadius) { this.ditherRadius = ditherRadius; }

    public Double getDitherMaximum() { return ditherMaximum; }
    public void setDitherMaximum(Double ditherMaximum) { this.ditherMaximum = ditherMaximum; }

    public Boolean getWarmUpWhenDone() { return warmUpWhenDone; }
    public void setWarmUpWhenDone(Boolean warmUpWhenDone) { this.warmUpWhenDone = warmUpWhenDone; }

    public Boolean getUseTheSkyAutosave() { return useTheSkyAutosave; }
    public void setUseTheSkyAutosave(Boolean useTheSkyAutosave) { this.useTheSkyAutosave = useTheSkyAutosave; }

    public Boolean getControlMount() { return controlMount; }
    public void setControlMount(Boolean controlMount) { this.controlMount = controlMount; }

    public Boolean getHomeMount() { return homeMount; }
    public void setHomeMount(Boolean homeMount) { this.homeMount = homeMount; }

    public Boolean getTrackingOff() { return trackingOff; }
    public void setTrackingOff(Boolean trackingOff) { this.trackingOff = trackingOff; }

    public Boolean getSlewToLight() { return slewToLight; }
    public void setSlewToLight(Boolean slewToLight) { this.slewToLight = slewToLight; }

    public Boolean getParkWhenDone() { return parkWhenDone; }
    public void setParkWhenDone(Boolean parkWhenDone) { this.parkWhenDone = parkWhenDone; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    /**
     * Static constructor for data model.  Create a new data model with default values.
     */
    public static DataModel newInstance(AppPreferences preferences) {
        DataModel newModel = new DataModel();
        newModel.setUseFilterWheel(preferences.getUseFilterWheel());
        newModel.setDefaultFrameCount(preferences.getDefaultFrameCount());
        newModel.setTargetADUs(preferences.getTargetADUs());
        newModel.setAduTolerance(preferences.getAduTolerance());
        newModel.setServerAddress(preferences.getServerAddress());
        newModel.setPortNumber(preferences.getPortNumber());
        newModel.setLightSourceAlt(preferences.getLightSourceAlt());
        newModel.setLightSourceAz(preferences.getLightSourceAz());
        newModel.setDitherFlats(preferences.getDitherFlats());
        newModel.setDitherRadius(preferences.getDitherRadius());
        newModel.setDitherMaximum(preferences.getMaximumDither());

        //  Get and store the filters in us
        //  These will be used for the left-margin "row headers" in the main table
        newModel.filtersInUse = new ArrayList<FilterSpec>();
        List<Integer> filterSlotNumbers = preferences.getFilterSlotNumbers();
        for (int slotNumber : filterSlotNumbers) {
            if (preferences.getFilterUse(slotNumber)) {
                String name = preferences.getFilterName(slotNumber);
                FilterSpec newFilter = new FilterSpec(slotNumber, name);
                newModel.filtersInUse.add(newFilter);
            }
        }

        //  Get and store a list of binning values that are set to "default" or "available"
        //  These will become the column headings of the main table
        newModel.binningsInUse = new ArrayList<BinningSpec>();
        List<Integer> binningNumbers = preferences.getBinningNumbers();
        for (int binning : binningNumbers) {
            BinningAvailability binningAvailability = preferences.getBinningAvailability(binning);
            if (binningAvailability != BinningAvailability.OFF) {
                newModel.binningsInUse.add(new BinningSpec(binning, binningAvailability));
            }
        }

        //  Create 2-dimensional table of filters-in-use vs binnings-in-use
        int numTableRows = newModel.filtersInUse.size();
        int numTableColumns = newModel.binningsInUse.size();
        newModel.frameTableData = new ArrayList<>(numTableRows);
        for (int rowIndex = 0; rowIndex < numTableRows; rowIndex++) {
            // Create a set of columns for this row
            ArrayList<Integer> thisRowOfColumns = new ArrayList<Integer>(Collections.nCopies(numTableColumns,Integer.valueOf(0)));
            newModel.frameTableData.add(thisRowOfColumns);
        }

        //  Set the frames table to the default values
        setDefaultFrameCounts(newModel.frameTableData, newModel.binningsInUse, newModel.defaultFrameCount);

        return newModel;
    }

    /**
     * Set the cells in the given frame data table to the default values.  I.e. set them to the
     * given default frame count where ever the cell's binning availability is "Default".  We treat
     * all the rows in the given table since it already has been set up with only filters (rows) needed
     * @param frameTableData        The frame table to be updated
     * @param binningsInUse         The binning descriptions for the columns
     * @param defaultFrameCount     The default frame count
     */
    private static void setDefaultFrameCounts(ArrayList<ArrayList<Integer>> frameTableData, ArrayList<BinningSpec> binningsInUse, Integer defaultFrameCount) {
        for (int rowIndex = 0; rowIndex < frameTableData.size(); rowIndex++) {
            ArrayList<Integer> thisRow = frameTableData.get(rowIndex);
            for (int columnIndex = 0; columnIndex < binningsInUse.size(); columnIndex++) {
                BinningAvailability binningAvailability = binningsInUse.get(columnIndex).getAvailability();
                if (binningAvailability == BinningAvailability.DEFAULT) {
                    thisRow.set(columnIndex, defaultFrameCount);
                } else {
                    thisRow.set(columnIndex, Integer.valueOf(0));
                }
            }
        }
    }

    /**
     * Count the number of filters that are set to "Use"
     * @return (int)
     */
    public int countUsedFilters() {
        return this.filtersInUse.size();
    }

    /**
     * How many binning values are in use?
     * @return (int)
     */
    public int countUsedBinning() {
        return this.binningsInUse.size();
    }

    /**
     * Get the n'th (zero based) binning value that is in use
     * @param index     zero-based index of needed binning
     * @return
     */
    public int getBinning(int index) {
        return this.binningsInUse.get(index).getBinningValue();
    }

    /**
     * Determine if there are any non-zero frame counts in the frame plan
     * @return
     */
    public boolean atLeastOneFrameSetWanted() {
        for (ArrayList<Integer> thisRow : this.frameTableData) {
            for (Integer oneValue : thisRow) {
                if (oneValue > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
