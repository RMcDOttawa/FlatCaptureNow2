import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilities and constants common to the entire application
 */

public class Common {

    //  Slewing the scope.

    //  How long do we wait for the slew to complete before timing out?
    public static final double SCOPE_SLEW_TIMEOUT_SECONDS = 3.0 * 60;
    //  How often do we ask the mount if slewing is still underway?
    public static final double SCOPE_SLEW_POLLING_SECONDS = 0.5;
    //  How often do we pulse the "Slewing" message on the user interface?
    public static final long SLEWING_FEEDBACK_INTERVAL_MILLISECONDS = 500;

    //  When taking flats, how many failed exposures in a row causes an abort?
    public static final int ADU_FAILURE_RETRY_LIMIT = 10;

    //  When exposing a flat, how long after the exposure should be finished do we consider
    //  continued non-completion a timeout error?
    public static final double FRAME_COMPLETION_POLL_INTERVAL_SECONDS = 0.5;
    public static final double FRAME_COMPLETION_TIMEOUT_SECONDS = 60.0;

    //  Are we simulating ADU measurement rather than using the server?
    public static final boolean SIMULATE_ADU_MEASUREMENT = true;
    public static final double SIMULATION_NOISE_FRACTION = 0.05;  // 5% noise

    //  Information about files saved from the application

    public static final String DATA_FILE_SUFFIX = "fcn2";
    public static final String UNSAVED_FILE_TITLE = "(Unsaved File)";
    public static final boolean FEEDBACK_EXPOSURE_ADJUSTMENT = false;

    /**
     * Given a full path, get just the file name, without the extension.
     * Funny, I thought there was a built-in function with exactly this function somewhere
     * in a Java library, but I couldn't find it after looking for as long as i cared to.
     * @param fullPath      Absolute path to file whose name is to be extracted
     * @return (String)     Just the file name
     */
    public static String simpleFileNameFromPath(String fullPath) {
        Path path = Paths.get(fullPath);
        Path fileNamePath = path.getFileName();
        String fileNameString = fileNamePath.toString();
        if (fileNameString.endsWith("." + Common.DATA_FILE_SUFFIX)) {
            fileNameString = fileNameString.substring(0,
                    fileNameString.length() - (1 + Common.DATA_FILE_SUFFIX.length()));
        }
        return fileNameString;
    }
}
