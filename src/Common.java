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

    //  Information about files saved from the application

    public static final String DATA_FILE_SUFFIX = "fcn2";
    public static final String UNSAVED_FILE_TITLE = "(Unsaved File)";
}
