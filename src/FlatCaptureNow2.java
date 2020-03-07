import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.swing.*;

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
            ImmutablePair<DataModel, String> modelInfo = makeDataModel(args, prefs);
            DataModel dataModel = modelInfo.left;
            String windowTitle = modelInfo.right;
            MainWindow mainWindow = new MainWindow(prefs);
            mainWindow.setUiFromDataModel(dataModel, windowTitle);
            mainWindow.setVisible(true);
        } catch (Exception e) {
            System.out.println("Uncaught exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a suitable data model.  If a file name argument is provided, try to load it.
     * Otherwise create a new default model.  Provide a suitable file name in either case.
     * @param args              Command-line arguments from application invocation
     * @return  (Pair)          Data model and window title string
     */
    private static ImmutablePair<DataModel, String> makeDataModel(String[] args, AppPreferences prefs) {
        DataModel resultModel;
        String resultName;
        if (args.length == 0) {
            // No command arguments, make a fresh data model
            resultModel = DataModel.newInstance(prefs);
            resultModel.generateDataTables(prefs, prefs.getUseFilterWheel());
            resultName = Common.UNSAVED_FILE_TITLE;
        } else {
            //  Try to make a data model from this file
            resultModel = DataModel.tryLoadFromFile(args[0]);
            if (resultModel == null) {
                JOptionPane.showMessageDialog(null,
                        "File provided does not exist or is not a valid\ndata file. Creating an empty file instead.");
                resultModel = DataModel.newInstance(prefs);
                resultModel.generateDataTables(prefs, prefs.getUseFilterWheel());
                resultName = Common.UNSAVED_FILE_TITLE;
            } else {
                resultName = Common.simpleFileNameFromPath(args[0]);
            }
        }
        return ImmutablePair.of(resultModel, resultName);
    }
}


