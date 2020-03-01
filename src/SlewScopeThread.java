import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Thread to slew the scope.  Slewing takes time, so we run it asynchronously as a thread, to
 * allow the main user interface to remain responsive - most importantly so that the "Cancel Slew"
 * button can be clicked and executed.
 */
public class SlewScopeThread implements  Runnable {
    MainWindow parent;
    String serverAddress;
    int portNumber;
    double targetAltitude;
    double targetAzimuth;
    TheSkyXServer server;
    boolean oldTrackingState = true;

    public SlewScopeThread(MainWindow parent,
                           String serverAddress,
                           int portNumber,
                           double targetAltitude,
                           double targetAzimuth
    ) {
        this.parent = parent;
        this.serverAddress = serverAddress;
        this.portNumber = portNumber;
        this.targetAltitude = targetAltitude;
        this.targetAzimuth = targetAzimuth;
    }

    @Override
    public void run() {

        String finishedMessage = null;
        try {
            this.server = new TheSkyXServer(this.serverAddress, this.portNumber);
            //  Slewing turns tracking on.  We'll remember the state to restore later.
            this.oldTrackingState = this.server.getScopeTracking();

            //  Start the slew, asynchronously
            this.server.slewToAltAz(this.targetAltitude, this.targetAzimuth, true);

            //  Poll scope until slewing is finished
            this.pollSlewingUntilDone();

            //  Restore the previous tracking state
            this.server.setScopeTracking(this.oldTrackingState);
        } catch (IOException e) {
            finishedMessage = "I/O Error";
        } catch (InterruptedException e) {
            // User clicked "Cancel". This is not an error, consider normal return.
            //  First, however, we'll tell the scope to stop any slew that might be in progress
            try {
                this.server.abortSlew();
            } catch (IOException ex) {
                // Ignore it if it fails.  We did the best we could.
            }
            finishedMessage = "Cancelled";
        } catch (TimeoutException e) {
            finishedMessage = "Timed Out";
        }


        parent.slewThreadFinished(finishedMessage);
    }

    /**
     * An asynchronous slew has begun.  Poll the scope at regular intervals
     * until it reports the slew is done.  Time out after a longish time.
     */
    private void pollSlewingUntilDone() throws InterruptedException, TimeoutException, IOException {

        double totalSecondsElapsed = 0.0;
        while (totalSecondsElapsed < Common.SCOPE_SLEW_TIMEOUT_SECONDS) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Cancelled");
            }
            if (this.server.scopeSlewComplete()) {
                return;
            } else {
                Thread.sleep((long)(Common.SCOPE_SLEW_POLLING_SECONDS * 1000));
                totalSecondsElapsed += Common.SCOPE_SLEW_POLLING_SECONDS;
            }
        }
        throw new TimeoutException("Slew timed out");
    }
}
