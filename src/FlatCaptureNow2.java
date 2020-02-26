import java.util.List;

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
            //  If we were given command arguments, assume that was a data file and
            //  try to open it as a data model.  Otherwise create a new data model
            AppPreferences prefs = AppPreferences.createPreferences();
            DataModel resultModel = DataModel.newInstance(prefs);
            MainWindow mainWindow = new MainWindow(prefs);
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
