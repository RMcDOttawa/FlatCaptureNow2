import java.io.Serializable;

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
    private Boolean     controlMount = false;
    private Boolean     homeMount = false;
    private Boolean     trackingOff = false;
    private Boolean     slewToLight = false;
    private Boolean     parkWhenDone = false;

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
}
