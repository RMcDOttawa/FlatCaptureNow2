import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class AppPreferences {
    private Preferences preferences;

    private static final String USE_FILTER_WHEEL = "use_filter_wheel";
    private static final String FLAT_FRAME_DEFAULT_COUNT_SETTING = "flat_frame_default_count";
    private static final String FILTER_SLOT_NUMBERS = "filter_slot_numbers";
    private static final String BINNING_NUMBERS = "binning_numbers";
    private static final String BINNING_AVAILABILITY = "binning_availability";
    private static final String EXPOSURE_ESTIMATE = "exposure_estimate";
    private static final String TARGET_ADU_SETTING = "target_adus";
    private static final String TARGET_ADU_TOLERANCE = "target_adu_tolerance";
    private static final String SERVER_ADDRESS_SETTING = "server_address";
    private static final String PORT_NUMBER_SETTING = "port_number";
    private static final String SOURCE_ALT = "source_alt";
    private static final String SOURCE_AZ = "source_az";
    private static final String DITHER_FLATS = "dither_flats";
    private static final String DITHER_RADIUS = "dither_radius";
    private static final String DITHER_MAX_RADIUS = "dither_max_radius";

    /**
     * Static constructor for preferences manager
     */
    public static AppPreferences createPreferences() {
        AppPreferences thePrefsObject = new AppPreferences();
        thePrefsObject.preferences = Preferences.userRoot().node("FlatCaptureNow2");
        System.out.println("Preferences object path: " + thePrefsObject.preferences.absolutePath());
        return thePrefsObject;
    }

    // Getters and Setters for the preference values

    // Does the camera have, and do we use, an automated filter wheel?
    public boolean getUseFilterWheel() {
        return this.preferences.getBoolean(USE_FILTER_WHEEL, true);
    }
    public void setUseFiterWheel(boolean flag) {
        this.preferences.putBoolean(USE_FILTER_WHEEL, flag);
    }

    //  Default number of frames to take for each filter/bin combination
    public int getDefaultFrameCount() {
        return this.preferences.getInt(FLAT_FRAME_DEFAULT_COUNT_SETTING, 16);
    }
    public void setDefaultFrameCount(int value) {
        this.preferences.putInt(FLAT_FRAME_DEFAULT_COUNT_SETTING, value);
    }

    //  Target ADU value for flat frame exposure (determines exposure length)
    public double getTargetADUs() {
        return this.preferences.getDouble(TARGET_ADU_SETTING, 25000.0);
    }
    public void setTargetADUs(double value) {
        this.preferences.putDouble(TARGET_ADU_SETTING, value);
    }

    //  Percentage tolerance for the target ADU (how close is close enough)
    //  Stored as a real value between zero and 1
    public double getAduTolerance() {
        return this.preferences.getDouble(TARGET_ADU_TOLERANCE, 0.1);
    }
    public void setAduTolerance(double value) {
        this.preferences.putDouble(TARGET_ADU_TOLERANCE, value);
    }

    //  IP address or host name of TSX server
    public String getServerAddress() {
        return this.preferences.get(SERVER_ADDRESS_SETTING, "localhost");
    }
    public void setServerAddress(String value) {
        this.preferences.put(SERVER_ADDRESS_SETTING, value);
    }

    //  Port number where TSX is listening
    public int getPortNumber() {
        return this.preferences.getInt(PORT_NUMBER_SETTING, 3040);
    }
    public void setPortNumber(int value) {
        this.preferences.putInt(PORT_NUMBER_SETTING, value);
    }

    //  Altitude of flat light source
    public double getLightSourceAlt() {
        return this.preferences.getDouble(SOURCE_ALT, 0.0);
    }
    public void setLightSourceAlt(double value) {
        this.preferences.putDouble(SOURCE_ALT, value);
    }

    //  Azimuth of flat light source
    public double getLightSourceAz() {
        return this.preferences.getDouble(SOURCE_AZ, 0.0);
    }
    public void setLightSourceAz(double value) {
        this.preferences.putDouble(SOURCE_AZ, value);
    }

    //  Should the scope be moved a small amount between flats to dither them?
    public boolean getDitherFlats() {
        return this.preferences.getBoolean(DITHER_FLATS, false);
    }
    public void setDitherFlats(boolean flag) {
        this.preferences.putBoolean(DITHER_FLATS, flag);
    }

    //  If dithering, distance from centre of first concentric displacement circle
    public double getDitherRadius() {
        return this.preferences.getDouble(DITHER_RADIUS, 10.0);
    }
    public void setDitherRadius(double value) {
        this.preferences.putDouble(DITHER_RADIUS, value);
    }

    //  If dithering, maximum distance from centre of before restarting
    public double getMaximumDither() {
        return this.preferences.getDouble(DITHER_MAX_RADIUS, 100.0);
    }
    public void setMaximumDither(double value) {
        this.preferences.putDouble(DITHER_MAX_RADIUS, value);
    }

    //  What slot numbers exist in the filter wheel?
    //  Note: no setter.  This is hard-coded into the UI.
    public List<Integer> getFilterSlotNumbers() {
        String slotNumbersEncoded = this.preferences.get(FILTER_SLOT_NUMBERS, "1,2,3,4,5,6,7,8");
        List<String> slotNumberStringsList = Arrays.asList(slotNumbersEncoded.split(","));
        List<Integer> slotNumbers = slotNumberStringsList.stream()
                                    .map(Integer::valueOf)
                                    .collect(Collectors.toList());
        return slotNumbers;
    }

    //  For every filter slot number we store a name.  Get and Set these with single methods
    public String getFilterName(int slotNumber) {
        System.out.println("Preferences/getFilterName stub " + slotNumber);
        return "STUB-" + slotNumber;
    }
    public void setFilterName(int slotNumber, String name) {
        System.out.println("Preferences/setFilterName stub " + slotNumber + ", " + name);
    }

    //  For every filter slot number we store a "use" flag.  Get and Set these with single methods
    public boolean getFilterUse(int slotNumber) {
        System.out.println("Preferences/getFilterUse stub " + slotNumber);
        return false;
    }
    public void setFilterUse(int slotNumber, boolean useFilter) {
        System.out.println("Preferences/setFilterUse stub " + slotNumber + ", " + useFilter);
    }

    //  What binning numbers do we support?
    //  Note: no setter.  This is hard-coded into the UI.
    public List<Integer> getBinningNumbers() {
        String binningNumbersEncoded = this.preferences.get(BINNING_NUMBERS, "1,2,3,4");
        List<String> binningNumberStringsList = Arrays.asList(binningNumbersEncoded.split(","));
        List<Integer> binningNumbers = binningNumberStringsList.stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        return binningNumbers;
    }

    //  Each binning number has an Availability value stored for it.  In the preferences we store
    //  these as integers as follows:
    //    public enum BinningAvailability {
    //        OFF,              Store as 0
    //        AVAILABLE,        Store as 1
    //        DEFAULT           Store as 2
    //    }

    public BinningAvailability getBinningAvailability(int binningLevel) {
        String key = BINNING_AVAILABILITY + ":" + binningLevel;
        int codeNumber = this.preferences.getInt(key, 1);
        switch (codeNumber) {
            case 0:
                return BinningAvailability.OFF;
            case 1:
                return BinningAvailability.AVAILABLE;
            case 2:
                return BinningAvailability.DEFAULT;
        }
        assert false;
        return BinningAvailability.AVAILABLE;
    }

    public void setBinningAvailability(int binningLevel, BinningAvailability availability) {
        String key = BINNING_AVAILABILITY + ":" + binningLevel;
        int codeNumber = 1;
        switch (availability) {
            case OFF:
                codeNumber = 0;
                break;
            case AVAILABLE:
                codeNumber = 1;
                break;
            case DEFAULT:
                codeNumber = 2;
                break;
        }
        this.preferences.putInt(key, codeNumber);
    }

    //  For every combination of filter and binning, we store an estimated exposure time
    //  These are encoded as separate items with a name, then the filter slot number, then the binning level

    public double getInitialExposure(int filterSlot, int binningLevel) {
        String key = EXPOSURE_ESTIMATE + ":" + filterSlot + ":" + binningLevel;
        return this.preferences.getDouble(key, 10.0);
    }

    public void setInitialExposure(int filterSlot, int binningLevel, double exposure) {
        String key = EXPOSURE_ESTIMATE + ":" + filterSlot + ":" + binningLevel;
        this.preferences.putDouble(key, exposure);
    }

}
