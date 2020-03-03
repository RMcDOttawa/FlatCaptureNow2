import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class SessionThread implements Runnable {

    private final Session parent;
    private final DataModel dataModel;
    private final ArrayList<FlatSet> flatsToAcquire;
    private TheSkyXServer server;
    private HashMap<Integer, Double> downloadTimes;

    public SessionThread(Session parent, DataModel dataModel, ArrayList<FlatSet> flatsToAcquire) {
        this.parent = parent;
        this.dataModel = dataModel;
        this.flatsToAcquire = flatsToAcquire;
    }

    /**
     * Main routine of this thread, invoked automatically by starting the thread.
     * Set up, acquire frames, and close down the session.  There is no "user interface"
     * for this thread - any messages to the user are passed back to the Session window controller.
     */
    @Override
    public void run() {
        try {
            this.server = new TheSkyXServer(this.dataModel.getServerAddress(), this.dataModel.getPortNumber());
            this.console("Session Started", 1);
            this.preSessionMountControl();
            this.measureDownloadTimes();
            this.setUpDithering();
            this.processWorkList();
            this.postSessionWarmUp();
            this.postSessionMountControl();
        } catch (IOException e) {
            this.console("I/O Error: " + e.getMessage(), 1);
        } catch (InterruptedException e) {
            //  We come here if the thread was interrupted by the user clicking "Cancel"
            this.console("Session Cancelled", 1);
            this.cleanUpFromCancel();
        }
        this.console("Session Ended", 1);
        this.parent.acquisitionThreadEnded();
    }

    /**
     * User clicked "Cancel".  Do any clean-up needed before ending the task.  Be quick.
     * Chores done here:
     */
    private void cleanUpFromCancel() {
        try {
            this.server.abortSlew();
            this.server.abortImageInProgress();
        } catch (IOException e) {
            // Ignore any errors, we're quitting anyway
        }
    }

    /**
     * Process the "work list" - the list of flat frame sets to be acquired. We will loop through
     * the complete list, acquire each set.  Optionally, if Dithering selected, we move the scope
     * slightly before each acquired frame.  Acquired frames are not saved immediately upon acquisition -
     * instead we will inspect the frame to see if the ADU average is in range, and save only if it is.
     */
    private void processWorkList() {
        // todo processWorkList
        System.out.println("processWorkList");
    }

    /**
     * Do the various optional mount-control tasks that take place before the acquisition session.
     * These can include:
     * - Home the mount
     * - Slew to the location of a fixed light source
     * - Turn tracking off
     */
    private void preSessionMountControl() throws IOException {
        if (this.dataModel.getControlMount()) {
            if (this.dataModel.getHomeMount()) {
                this.server.homeMount();
            }
            if (this.dataModel.getSlewToLight()) {
                this.server.slewToAltAz(this.dataModel.getLightSourceAlt(), this.dataModel.getLightSourceAz(), false);
            }
            if (this.dataModel.getTrackingOff()) {
                this.server.setScopeTracking(false);
            }
        }
    }

    /**
     * To ascynchronously manage the camera, we need to know how long the downloads of finished images take.
     * These vary with the binning setting.  We'll time them here, by taking zero-length bias frames at each binning.
     * They are stored in a HashMap indexed by the binning number.  Use of a hashmap allows us to check if a given
     * binning has already been measured (key will exist) so we only measure each one once.
     */
    private void measureDownloadTimes() throws InterruptedException, IOException {
        this.console("Measuring download times for each binning level needed.", 1);
        this.downloadTimes = new HashMap<Integer, Double>(4);
        for (FlatSet flatSet : this.flatsToAcquire) {
            Integer binning = flatSet.getBinning();
            if (this.downloadTimes.containsKey(binning)) {
                // We already have a time for this binning, don't need to do another
                ;
            } else {
                Double downloadTime = this.timeDownloadFor(binning);
                this.downloadTimes.put(binning, downloadTime);
            }
        }
    }

    /**
     * Measure the dowload time for the given binning value by taking, and timing, a
     * zero-second bias frame.  Since bias frames are zero length, the time elapsed will be
     * just the download time for an image binned to that size.
     * @param binning       Binning value to take and time
     * @return (Double)     Elapsed time in seconds
     */
    private Double timeDownloadFor(Integer binning) throws IOException {
        LocalDateTime timeBefore = LocalDateTime.now();
        this.exposeBiasFrame(binning, false, false);
        LocalDateTime timeAfter = LocalDateTime.now();
        Duration timeTaken = Duration.between(timeAfter, timeBefore).abs();
        double downloadSeconds = timeTaken.getSeconds();
        this.console(String.format("Binned %d x %d: %.02f seconds.", binning, binning, downloadSeconds), 2);
        return downloadSeconds;
    }

    private void exposeBiasFrame(Integer binning, boolean asynchronous, boolean autosave) throws IOException {
        this.server.exposeBiasFrame(binning, asynchronous, autosave);
    }

    /**
     * If optional dithering is in use, set up the data structure used to manage it
     */
    private void setUpDithering() {
        // todo setUpDithering
        System.out.println("setUpDithering");
    }

    /**
     * Optionally turn off the camera's cooler at the end of the session so the CCD can warm up gradually.
     */
    private void postSessionWarmUp() {
        // todo postSessionWarmUp
        System.out.println("postSessionWarmUp");
    }

    /**
     * Optionally park the mount after the session
     */
    private void postSessionMountControl() {
        // todo postSessionMountControl
        System.out.println("postSessionMountControl");
    }

    /**
     * Send the given message info to the Session Window for display on the console frame there.
     * Note: a time stamp is automatically added to the displayed line.
     * @param messageText           Text to display
     * @param indentationLevel      Indentation level.  1 = leftmost, 2+ are indented
     */
    private void console(String messageText, int indentationLevel) {
        this.parent.console(messageText, indentationLevel);
    }
}
