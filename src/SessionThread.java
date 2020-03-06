import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

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
            DitherController dither = this.preSessionMountControl();
            this.measureDownloadTimes();
            this.setUpDithering(dither);
            this.processWorkList(dither);
            this.postSessionWarmUp();
            this.postSessionMountControl();
        } catch (IOException e) {
            this.console("I/O Error: " + e.getMessage(), 1);
            e.printStackTrace();
        } catch (InterruptedException e) {
            //  We come here if the thread was interrupted by the user clicking "Cancel"
            e.printStackTrace();
            this.cleanUpFromCancel();
            this.console("Session Cancelled", 1);
        } catch (ADUExposureException e) {
            e.printStackTrace();
            this.console("Too many failed exposures, aborting session.", 1);
        } catch (TimeoutException e) {
            e.printStackTrace();
            this.console(e.getMessage(), 1);
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
        } catch (IOException e) {
            // Ignore any errors, we're quitting anyway
        }
        try {
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
     * @param dither    Dither controller (null if no dithering)
     */
    private void processWorkList(DitherController dither) throws InterruptedException, IOException, ADUExposureException, TimeoutException {

        for (int itemIndex = 0; itemIndex < this.flatsToAcquire.size(); itemIndex++) {
            //  Tell the user interface to highlight this row in the table
            this.parent.highlightSessionTableRow(itemIndex);
            //  Tell the console what set we're beginning
            FlatSet thisSet = this.flatsToAcquire.get(itemIndex);
            console("Acquiring " + thisSet.describe() + ".", 1);
            //  Acquire all the flats in this set
            this.acquireOneFlatsSet(thisSet, dither);
            this.resetDithering(dither);
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
     * @param dither    Dithering controller (null if no dithering)
     */
    private void acquireOneFlatsSet(FlatSet thisSet, DitherController dither)
            throws InterruptedException, IOException, ADUExposureException, TimeoutException {
        if (thisSet.getNumberOfFrames() > thisSet.getNumberDone()) {
            // Connect camera
            this.server.connectToCamera();

            // Acquire the frames
            this.acquireFrames(thisSet, dither);
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
     * @param thisSet           Details of the frame set to be acquired
     * @param dither            Dithering controller (null if no dithering)
     */
    private void acquireFrames(FlatSet thisSet, DitherController dither) throws IOException, ADUExposureException, InterruptedException, TimeoutException {

        // Set up filter if in use. We only need do this once, since all the frames
        // we are about to take are identical.
        if (this.dataModel.getUseFilterWheel()) {
            this.server.selectFilter(thisSet.getFilterSpec().getSlotNumber());
        }

        // Get initial exposure estimate saved into preferences from last time we did this
        double exposureSeconds = thisSet.getEstimatedExposure();

        //  We'll run a progress bar measuring the number of frames to collect, not their exposure, because
        //  flat-frame exposures will likely be quite short and it's the total collection of frames that
        //  has a meaningful elapsed time.
        this.parent.startProgressBar(thisSet.getNumberOfFrames());

        // Loop until we have successfully saved the desired number of frames, or we fail because
        // of a number of exposure ADU out-of-spec failures in a row.
        int rejectedConsecutively = 0;
        int frameNumberTrying = 1;
        int lastDitheredFrameNumber = 0;

        while (thisSet.getNumberDone() < thisSet.getNumberOfFrames()) {
            // Don't dither if we're trying the same frame again, only new frames
            if (lastDitheredFrameNumber != frameNumberTrying) {
                lastDitheredFrameNumber = frameNumberTrying;
                this.ditherNextFrame(dither);
            }
            this.console(String.format("Frame %d of %d: %.3f seconds", frameNumberTrying,
                    thisSet.getNumberOfFrames(), exposureSeconds), 2);
            int frameAverageADUs = this.exposeFlatFrame(thisSet.getBinning(), exposureSeconds);
            if (this.ADUsInRange(frameAverageADUs, this.dataModel.getTargetADUs(), this.dataModel.getAduTolerance())) {
                this.parent.reportFrameADUs(frameAverageADUs, true);
                thisSet.setNumberDone(1 + thisSet.getNumberDone());
                thisSet.rememberSuccessfulExposure(exposureSeconds);
                this.saveAcquiredFrame(exposureSeconds, frameNumberTrying, thisSet);
                rejectedConsecutively = 0;
                frameNumberTrying++;
                this.parent.updateProgressBar(frameNumberTrying);
            } else{
                this.parent.reportFrameADUs(frameAverageADUs, false);
                rejectedConsecutively += 1;
                if (rejectedConsecutively > Common.ADU_FAILURE_RETRY_LIMIT) {
                    throw new ADUExposureException();
                }
            }
            exposureSeconds = this.refineExposure(exposureSeconds, frameAverageADUs, this.dataModel.getTargetADUs());
        }
        this.parent.stopProgressBar();

    }

    /**
     * Dither the next frame.  If dithering is on (non-null controller) calculate where the scope should
     * be pointed for the next frame and move it there.
     * @param dither    Dither controller
     */
    private void ditherNextFrame(DitherController dither) throws IOException {
        if (dither != null) {
            ImmutableTriple<Boolean, Double, Double> ditherResponse = dither.calculateNextFrame();
            boolean moveScope = ditherResponse.left;
            double moveToAlt = ditherResponse.middle;
            double moveToAz = ditherResponse.right;
            if (moveScope) {
//                System.out.println(String.format("Dither slew to: %f, %f", moveToAlt, moveToAz));
                this.server.slewToAltAz(moveToAlt, moveToAz, false);
                if (this.dataModel.getTrackingOff()) {
                    this.server.setScopeTracking(false);
                }
            }
        }
    }

    /**
     * Reset the dithering controller at the end of a set of frames, so the next set starts from
     * the target centre again.  Slew the scope back to the centre.
     * @param dither    Dither controller
     */
    private void resetDithering(DitherController dither) throws IOException {
        if (dither != null) {
            double originalAlt = dither.getStartAltDeg();
            double originalAz = dither.getStartAzDeg();
//            System.out.println(String.format("Dither reset slew to: %f, %f", originalAlt, originalAz));
            this.server.slewToAltAz(originalAlt, originalAz, false);
            if (this.dataModel.getTrackingOff()) {
                this.server.setScopeTracking(false);
            }
            dither.reset();
        }
    }

    /**
     * The last acquired frame has been measured and found acceptable.  Tell the server to save it to disk.
     * We'll either have it save to its defined "autosave directory", or we'll give it a path name
     * if the server is local to this computer and that option has been selected
     * @param exposureSeconds   Exposure to include in generated file name
     */
    private void saveAcquiredFrame(double exposureSeconds, int sequenceNumber, FlatSet thisSet) throws IOException {
        String fileName = this.makeLocalFileName(exposureSeconds, sequenceNumber, thisSet);
        if (this.dataModel.getUseTheSkyAutosave()) {
            this.server.saveImageToAutoSave(fileName);
        } else {
            this.server.saveImageToLocalPath(this.dataModel.getLocalPath() + "/" + fileName);
        }
    }

    /**
     * Make up a path and file name to receive the acquired image
     * File name format:   Flat-Filtername-binningxbinning-Sequence-Exposure
     * @param exposureSeconds   Exposure seconds
     * @param sequenceNumber    Frame sequence number
     * @param thisSet           Frame set descriptor for filter and binning
     * @return (String)         File name as absolute path
     */
    private String makeLocalFileName(double exposureSeconds, int sequenceNumber, FlatSet thisSet) {
        return String.format("Flat-%s-%dx%d-%d-%.2fs.fit",
                thisSet.getFilterSpec().getName(),
                thisSet.getBinning(), thisSet.getBinning(),
                sequenceNumber,
                exposureSeconds);
    }

    /**
     * Improve the estimated exposure for the next attempt.  We assume exposure-to-ADU response is linear, so
     * the amount by which the ADU value missed the target is the amount to adjust the exposure to improve it.
     * @param exposureSeconds       Exposure usd last time
     * @param frameAverageADUs      ADUs that resulted from that exposure
     * @param targetADUs            Desired ADU level
     * @return (double)             Improved exposure estimate
     */
    private double refineExposure(double exposureSeconds, int frameAverageADUs, Integer targetADUs) {
        double missFactor = ((double)frameAverageADUs) / ((double)targetADUs);
        double newExposure = exposureSeconds / missFactor;
        if (Common.FEEDBACK_EXPOSURE_ADJUSTMENT) {
            if (frameAverageADUs > targetADUs) {
                this.console(String.format("Reducing exposure to %f", newExposure), 4);
            } else {
                this.console(String.format("Increasing exposure to %f", newExposure), 4);
            }
        }
        return newExposure;
    }

    /**
     * Expose a single flat frame with given binning and exposure.  (Filter has already been set.)
     * Start the exposure asynchronously, then wait for it to complete.  This allows us to detect that
     * the thread has been interrrupted via the Cancel button and send an Abort to the camera.
     * Once the image has been acquired, ask TheSky to calculate the average ADUs and return that.
     * @param binning               Binning level for the frame
     * @param exposureSeconds       Exposure time for the frame
     * @return (int)                The average ADUs of the acquired frame.
     */
    private int exposeFlatFrame(int binning, double exposureSeconds) throws InterruptedException, IOException, TimeoutException {
        this.server.exposeFlatFrame(exposureSeconds, binning, true, false);
        this.waitForExposureCompletion(exposureSeconds, binning);
        return this.server.getLastImageADUs();
    }

    /**
     * Wait for the camera exposure, which is running ascynchronously, to complete.  We'll sleep for
     * the exposure time plus the measured download time, then start polling the camera at brief
     * intervals until it reports done.  Time out after a long wait.
     * @param exposureSeconds       How long was the actual exposure
     * @param binning               Binning in use (to look up download time)
     */
    private void waitForExposureCompletion(double exposureSeconds, int binning) throws InterruptedException, IOException, TimeoutException {

        //  Wait as long as it should take
        double downloadTime = this.downloadTimes.get(binning);
        double totalWaitSeconds = downloadTime + exposureSeconds;
        long waitMilliseconds = Math.round(totalWaitSeconds * 1000.0 );
        if (waitMilliseconds > 0) {
            Thread.sleep(waitMilliseconds);
        }

        double timeWaited = 0.0;
        //  Now poll the camera until complete or timeout
        while (!this.server.exposureIsComplete()) {
            // Exposure is not finished.  Have we waited long enough?
            if (timeWaited > Common.FRAME_COMPLETION_TIMEOUT_SECONDS) {
                throw new TimeoutException("Exposure timed out");
            } else {
                Thread.sleep(Math.round(1000.0 * Common.FRAME_COMPLETION_POLL_INTERVAL_SECONDS));
                timeWaited += Common.FRAME_COMPLETION_POLL_INTERVAL_SECONDS;
            }
        }
    }

    /**
     * Determine if the given ADU value is close enough to the target, within the given tolerance
     * @param frameAverageADUs      ADU value to test
     * @param targetADUs            Target ADU value
     * @param aduTolerance          Tolerance, as a percentage, as a value between 0 and 1
     * @return (boolean)            Value is within acceptable range
     */
    private boolean ADUsInRange(int frameAverageADUs, int targetADUs, double aduTolerance) {
        double difference = Math.abs(frameAverageADUs - targetADUs);
        double differenceRatio = difference / targetADUs;
        return differenceRatio <= aduTolerance;
    }

    /**
     * Do the various optional mount-control tasks that take place before the acquisition session.
     * These can include:
     * - Home the mount
     * - Slew to the location of a fixed light source
     * - Turn tracking off
     * @return (DitherController)   Dither controller if dithering in use, otherwise null
     */
    private DitherController preSessionMountControl() throws IOException {
        DitherController dither = null;
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
            if (this.dataModel.getDitherFlats()) {
                // We assume scope is now pointed at target, either manually or by the slew above
                ImmutablePair<Double,Double> scopeCoordinates = this.server.getScopeAltAz();
                dither = new DitherController(scopeCoordinates.left, scopeCoordinates.right,
                        this.dataModel.getDitherRadius(), this.dataModel.getDitherMaximum());
                this.console("Dithering flats: " + dither.description() + ".", 1);
            }
        }
        return dither;
    }

    /**
     * To asynchronously manage the camera, we need to know how long the downloads of finished images take.
     * These vary with the binning setting.  We'll time them here, by taking zero-length bias frames at each binning.
     * They are stored in a HashMap indexed by the binning number.  Use of a hashmap allows us to check if a given
     * binning has already been measured (key will exist) so we only measure each one once.
     */
    private void measureDownloadTimes() throws  IOException {
        this.console("Measuring download times for each binning level needed.", 1);
        //  Connect to camera so we're not measuring connect time with the first download test
        this.server.connectToCamera();
        // Create store for the measured times, and measure each binning in use
        this.downloadTimes = new HashMap<>(4);
        for (FlatSet flatSet : this.flatsToAcquire) {
            Integer binning = flatSet.getBinning();
            if (!this.downloadTimes.containsKey(binning)) {
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
     * @param dither    Dither controller (null if no dithering)
     */
    private void setUpDithering(DitherController dither) {
        if (dither != null) {
            System.out.println("setUpDithering");
            // Nothing to do; left here for possible future use
        }
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
