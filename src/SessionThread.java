import java.util.ArrayList;

public class SessionThread implements Runnable {

    private final Session parent;
    private final DataModel dataModel;
    private final ArrayList<FlatSet> flatsToAcquire;

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
        System.out.println("SessionThread/run entered");
        // todo SessionThread/run
        try {
            this.console("Session Started", 1);
            this.preSessionMountControl();
            this.measureDownloadTimes();
            this.setUpDithering();
            this.processWorkList();
            this.postSessionWarmUp();
            this.postSessionMountControl();
        } catch ( InterruptedException e) {
            //  We come here if the thread was interrupted by the user clicking "Cancel"
            this.cleanUpFromCancel();
            e.printStackTrace();
        }
        System.out.println("SessionThread/run exits");
        this.console("Session Ended", 1);
        this.parent.acquisitionThreadEnded();
    }

    /**
     * User clicked "Cancel".  Do any clean-up needed before ending the task.  Be quick.
     */
    private void cleanUpFromCancel() {
        // todo cleanUpFromCancel
        System.out.println("cleanUpFromCancel");
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
    private void preSessionMountControl() {
        // todo     preSessionMountControl
        System.out.println("preSessionMountControl");
    }

    /**
     * To ascynchronously manage the camera, we need to know how long the downloads of finished images take.
     * These vary with the binning setting.  We'll time them here, by taking zero-length bias frames at each binning.
     */
    private void measureDownloadTimes() throws InterruptedException {
        // todo measureDownloadTimes
        System.out.println("measureDownloadTimes");
        Thread.sleep(10 * 1000);  // To force exception signature
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
        // todo console
        System.out.println("console");
    }
}
