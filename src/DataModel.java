import org.xml.sax.InputSource;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Model for a flats capture plan.
 */
public class DataModel  implements Serializable {

    private Integer     modelVersion = 1;

    //  Instance variables that are initialized from Preferences
    private Boolean     useFilterWheel = true;
    private Integer     defaultFrameCount = 0;
    private Integer     targetADUs = 0;
    private Double      aduTolerance = 0.0;
    private String      serverAddress = "";
    private Integer     portNumber = 0;
    private Double      lightSourceAlt = 0.0;
    private Double      lightSourceAz = 0.0;
    private Boolean     ditherFlats = false;
    private Double      ditherRadius = 00.0;
    private Double      ditherMaximum = 00.0;


    //  Instance variables with simple default, not from preferences
    private Boolean     warmUpWhenDone = false;
    private Boolean     useTheSkyAutosave = true;
    private String      localPath = null;
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

    public Integer getModelVersion() { return modelVersion; }
    public void setModelVersion(Integer modelVersion) { this.modelVersion = modelVersion; }

    public ArrayList<FilterSpec> getFiltersInUse() { return filtersInUse; }
    public void setFiltersInUse(ArrayList<FilterSpec> filtersInUse) { this.filtersInUse = filtersInUse; }

    public ArrayList<BinningSpec> getBinningsInUse() { return binningsInUse; }
    public void setBinningsInUse(ArrayList<BinningSpec> binningsInUse) { this.binningsInUse = binningsInUse; }

    public ArrayList<ArrayList<Integer>> getFrameTableData() { return frameTableData; }
    public void setFrameTableData(ArrayList<ArrayList<Integer>> frameTableData) { this.frameTableData = frameTableData; }

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


        return newModel;
    }

    /**
     * De-serializer
     *
     * Create a new instance of data model by decoding the provided xml string
     * * @param serialized      String containing xml-encoded data model
     * @return
     */
    public static DataModel newFromXml(String serialized) {
        DataModel newModel = null;
        Object decodedObject = null;
        InputSource inputSource = new InputSource(new StringReader(serialized));
        XMLDecoder decoder;
        try {
            decoder = new XMLDecoder(inputSource);
            decodedObject = decoder.readObject();
        } catch (Exception e) {
            // An exception means the input string was corrupted.
            // Just drop out and leave the model as null
        }
        if (decodedObject instanceof DataModel) {
            newModel = (DataModel) decodedObject;
        }
        return newModel;
    }

    //  Generate the tables of filters, binning, and main table.  Do this outside the initialization
    //  method so it shows up as a difference from a just-initialized table for serialization

    public void generateDataTables(AppPreferences preferences) {
        //  Get and store the filters in us
        //  These will be used for the left-margin "row headers" in the main table
        this.filtersInUse = new ArrayList<FilterSpec>();
        List<Integer> filterSlotNumbers = preferences.getFilterSlotNumbers();
        for (int slotNumber : filterSlotNumbers) {
            if (preferences.getFilterUse(slotNumber)) {
                String name = preferences.getFilterName(slotNumber);
                FilterSpec newFilter = new FilterSpec(slotNumber, name);
                this.filtersInUse.add(newFilter);
            }
        }

        //  Get and store a list of binning values that are set to "default" or "available"
        //  These will become the column headings of the main table
        this.binningsInUse = new ArrayList<BinningSpec>();
        List<Integer> binningNumbers = preferences.getBinningNumbers();
        for (int binning : binningNumbers) {
            BinningAvailability binningAvailability = preferences.getBinningAvailability(binning);
            if (binningAvailability != BinningAvailability.OFF) {
                this.binningsInUse.add(new BinningSpec(binning, binningAvailability));
            }
        }

        //  Create 2-dimensional table of filters-in-use vs binnings-in-use
        int numTableRows = this.filtersInUse.size();
        int numTableColumns = this.binningsInUse.size();
        this.frameTableData = new ArrayList<>(numTableRows);
        for (int rowIndex = 0; rowIndex < numTableRows; rowIndex++) {
            // Create a set of columns for this row
            ArrayList<Integer> thisRowOfColumns = new ArrayList<Integer>(Collections.nCopies(numTableColumns,Integer.valueOf(0)));
            this.frameTableData.add(thisRowOfColumns);
        }

        //  Set the frames table to the default values
        setDefaultFrameCounts();

    }
    /**
     * Set the cells in the given frame data table to the default values.  I.e. set them to the
     * given default frame count where ever the cell's binning availability is "Default".  We treat
     * all the rows in the given table since it already has been set up with only filters (rows) needed
      */
    private  void setDefaultFrameCounts() {
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

    public String serialize() {

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        BufferedOutputStream outputStream = new BufferedOutputStream(byteStream);
        XMLEncoder encoder = new XMLEncoder(outputStream);
        encoder.writeObject(this);
        encoder.close();
        try {
            outputStream.close();
            byteStream.close();
        } catch (IOException e) {
            System.out.println("Exception serializing data model: " + e.getMessage());
            e.printStackTrace();
        }
        String resultString = byteStream.toString();

        return resultString;
    }

    public ArrayList<FlatSet> getFlatSetsToAcquire() {
        // todo getFlatSetsToAcquire
        System.out.println("getFlatSetsToAcquire");
        // As a stub, manually create a list of 3 sets
        FlatSet set1 = new FlatSet(11, new FilterSpec(1, "Red"), 1);
        FlatSet set2 = new FlatSet(12, new FilterSpec(1, "Green"), 2);
        FlatSet set3 = new FlatSet(13, new FilterSpec(1, "Blue"), 3);
        ArrayList<FlatSet> result = new ArrayList<FlatSet>(3);
        result.add(set1);
        result.add(set2);
        result.add(set3);
        return result;
    }
}
