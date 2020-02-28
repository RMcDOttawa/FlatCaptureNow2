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
            AppPreferences prefs = AppPreferences.createPreferences();
            DataModel dataModel = DataModel.newInstance(prefs);
            MainWindow mainWindow = new MainWindow(prefs, dataModel);
            mainWindow.setUiFromDataModel();

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

// todo populate main window table headers
// todo Populate main window table data
// todo Accept main window data table cell edits
// todo Record main window changes in data model
// todo Read and write files
// todo Handle file dirtiness
// todo Open session console window on start of session