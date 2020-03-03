import java.util.List;

/**
 * Program to control TheSkyX, over its TCP server connection, and have it capture
 * a large number of Flat calibration frames - sets of frames for different filter
 * and binning combinations.  Exposure times are automatically calculated to achieve
 * a requested average exposure level of the frames.
 */
public class FlatCaptureNow2 {

    /**
     * Main program called from operating system
     * @param args      Array of string arguments to the program.  Arg[0] is the program name.
     */
    public static void main(String[] args) {
        //  If we are running on a Mac, use the system menu bar instead of windows-style window menu
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("mac os x")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        //  Create and open the main window
        try {
            AppPreferences prefs = AppPreferences.createPreferences();
            DataModel dataModel = DataModel.newInstance(prefs);
            dataModel.generateDataTables(prefs);
            MainWindow mainWindow = new MainWindow(prefs);
            mainWindow.setUiFromDataModel(dataModel, Common.UNSAVED_FILE_TITLE);

//            mainWindow.loadDataModel(loadedDataModel, windowTitle);
//            mainWindow.setFilePath(makeFilePath(windowTitle, args));
//            mainWindow.makeNotDirty();
            mainWindow.setVisible(true);
        } catch (Exception e) {
            System.out.println("Uncaught exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

