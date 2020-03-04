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
    private void processWorkList() throws InterruptedException, IOException {

        for (int itemIndex = 0; itemIndex < this.flatsToAcquire.size(); itemIndex++) {
            //  Tell the user interface to highlight this row in the table
            this.parent.highlightSessionTableRow(itemIndex);
            //  Tell the console what set we're beginning
            FlatSet thisSet = this.flatsToAcquire.get(itemIndex);
            console("Acquiring " + thisSet.describe() + ".", 1);
            //  Acquire all the flats in this set
            this.acquireOneFlatsSet(itemIndex, thisSet);
        }
        
    }

    /**
     * Acquire all the frames in the given flats set.
     * This involves the following steps:
     *      - Determine an exposure that results in ADU level in the specified target range
     *      - Acquire all the required flats, adjusting ADU after each, and discarding any
     *          that fall out of range.  So more than the needed number of exposures may be needed
     *      - Keep track of retries caused by flats being out of range, and give up after a limit is reached.
     *          (This might happen if the lighting conditions change dramatically, such as a light coming on in
     *          the observatory)
     *      - If dithering is in use, do a dither move after each successful frame
     * @param thisSet   Specifications for the Flats set wanted
     */
    private void acquireOneFlatsSet(int workItemIndex, FlatSet thisSet) throws InterruptedException, IOException {
        // todo acquireOneFlatsSet
        System.out.println("acquireOneFlatsSet");
        if (thisSet.getNumberOfFrames() > thisSet.getNumberDone()) {
            // Connect camera
            this.server.connectToCamera();

            // Acquire the frames
            this.acquireFrames(workItemIndex, thisSet);
        }
    }

    /**
     * Acquire the number of frames, of the specification, in the given work item.
     * We start with an estimate of the right exposure, based on what worked last time.
     * After each frame we measure the average ADUs, and keep the frame only if it is within
     * spec.  Then we refine the exposure.  This way the first one or two frames may be rejected
     * as we search for a good exposure, then the others will adjust as acquisition proceeds.  This
     * will allow for changes such as the sky (if sky flats) gradually brightening, or allows
     * the operator to adjust the brightness of a light panel.
     *
     * In case conditions become unworkable, we will keep track of how many frames IN A ROW have
     * been rejected, and fail if a threshold is exceeded.
     *
     * Because we don't want to save FITs files for frames that are rejected, we take frames with
     * autosave OFF, then manually save each frame after it is analyzed and once we know we like it.
     * @param workItemIndex     Index in work list (for updating UI)
     * @param thisSet           Details of the frame set to be acquired
     */
    private void acquireFrames(int workItemIndex, FlatSet thisSet) throws IOException, InterruptedException {
        // todo acquireFrames

        // Set up filter if in use. We only need do this once, since all the frames
        // we are about to take are identical.
        if (this.dataModel.getUseFilterWheel()) {
            this.server.selectFilter(thisSet.getFilterSpec().getSlotNumber());
        }

        // Get initial exposure estimate saved into preferences from last time we did this
        double exposureSeconds = thisSet.getEstimatedExposure();

        // Loop until we have successfully saved the desired number of frames, or we fail because
        // of a number of exposure ADU out-of-spec failures in a row.
        int rejectedConsecutively = 0;

        //  We'll run a progress bar measuring the number of frames to collect, not their exposure, because
        //  flat-frame exposures will likely be quite short and it's the total collection of frames that
        //  has a meaningful elapsed time.
        this.parent.startProgressBar(thisSet.getNumberOfFrames());
        // stub
        for (int i = 0; i < thisSet.getNumberOfFrames(); i++) {
            Thread.sleep(1000);
            this.parent.updateProgressBar(i);
        }
        this.parent.stopProgressBar();

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
