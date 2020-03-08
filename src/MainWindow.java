import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.awt.desktop.QuitStrategy;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
/*
 * Created by JFormDesigner on Tue Feb 25 15:18:41 EST 2020
 */



/**
 * Main window controller for the application
 * @author Richard McDonald
 */
@SuppressWarnings({"FieldCanBeLocal", "DuplicatedCode"})
public class MainWindow extends JFrame {

    private AppPreferences preferences;
    private DataModel dataModel = null;

    //  Map to record valid/invalid state of validated fields in the UI
    //  Fields not in the map are considered valid.  Fields in the map are valid or not
    //  depending on the boolean stored.
    private HashMap<JTextField, Boolean> fieldValidity = null;
    private SlewScopeThread slewScopeRunnable;
    private Thread slewScopeThread;
    private boolean rememberProceedEnabled;  // Remember proceed state during a slew
    private boolean documentIsDirty = false;

    private SlewingFeedbackTask slewingFeedbackTask = null;
    private Timer slewingFeedbackTimer = null;
    private boolean slewMessageIsPulsed;
    private FrameTableModel frameTableModel;

    private String filePath = "";
    private Session sessionWindow;

    /**
     * Main constructor for this class.  Provide it with a copy of the application
     * Preferences object since some initialization is drawn from the prefs.
     * @param preferences   Application preferences object
     */
    public MainWindow ( AppPreferences preferences) {
        this.preferences = preferences;
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //  Catch main Quit menu so we can check for unsaved data
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            String osName = System.getProperty("os.name").toLowerCase();
            // the following doesn't work on windows, even though the above "isDesktopSupported"
            // said it does.  So only set quit strategy on non-windows.
            if (!osName.toUpperCase().contains("WINDOWS")) {
                desktop.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
                desktop.setQuitHandler((QuitEvent evt, QuitResponse res) -> quitMenuItemActionPerformed());
            }
        }
        initComponents();
    }

    /**
     * Set up the visual UI from the given data model
     * @param dataModel     Data model to load to UI
     * @param windowTitle   Title to assign to window
     */
    public void setUiFromDataModel(DataModel dataModel, String windowTitle) {
        this.setTitle(windowTitle);
        this.dataModel = dataModel;
        this.fieldValidity = new HashMap<>();

        this.serverAddressField.setText(dataModel.getServerAddress());
        this.portNumberField.setText(String.valueOf(dataModel.getPortNumber()));

        this.targetADUfield.setText(String.valueOf(dataModel.getTargetADUs()));
        this.aduToleranceField.setText(String.valueOf(roundFloat(dataModel.getAduTolerance() * 100.0, 2)));

        this.useFilterWheelCheckbox.setSelected(dataModel.getUseFilterWheel());
        this.warmWhenDoneCheckbox.setSelected(dataModel.getWarmUpWhenDone());

        if (dataModel.getUseTheSkyAutosave()) {
            this.useAutosaveButton.setSelected(true);
            this.useLocalFolderButton.setSelected(false);
        } else {
            this.useLocalFolderButton.setSelected(true);
            this.useAutosaveButton.setSelected(false);
        }

        this.controlMountCheckbox.setSelected(dataModel.getControlMount());
        this.homeMountCheckbox.setSelected(dataModel.getHomeMount());
        this.trackingOffCheckbox.setSelected(dataModel.getTrackingOff());
        this.slewToLightCheckbox.setSelected(dataModel.getSlewToLight());
        this.parkWhenDoneCheckbox.setSelected(dataModel.getParkWhenDone());

        this.lightSourceAltField.setText(String.format("%.8f",dataModel.getLightSourceAlt()));
        this.lightSourceAzField.setText(String.format("%.8f",dataModel.getLightSourceAz()));

        this.ditherFlatsCheckbox.setSelected(dataModel.getDitherFlats());
        this.ditherRadiusField.setText(String.valueOf(dataModel.getDitherRadius()));
        this.ditherMaximumField.setText(String.valueOf(dataModel.getDitherMaximum()));

        //  Set up table model for the frames table
        this.frameTableModel = new FrameTableModel(this, dataModel);
        this.framesTable.setModel(frameTableModel);

        //  Set some appearance attributes for the frames table that can't be set in the JFormDesigner
        this.framesTable.setRowHeight(this.calcGoodFramesTableRowHeight());

        //  Set up table to accept edits on single clicks not double

        this.setUpFramesTableEditing();
        this.setLocalOrRemoteMessage();
        this.enableSlewControls();
        this.enableProceedButton();
        this.makeNotDirty();
    }


    @SuppressWarnings("SameParameterValue")
    private static double roundFloat(double inputValue, int numPlaces) {
        double multiplier = Math.pow(10.0, numPlaces);
        return Math.round(inputValue * multiplier) / multiplier;
    }

    /**
     * Set up table to accept edits on single clicks not double
     */
    private void setUpFramesTableEditing() {
        for (int columnIndex = 0; columnIndex < this.dataModel.countUsedBinning(); columnIndex++) {
            int adjustedColumn = columnIndex + 1;  // Move over to allow for row headings
            Class <?> columnClass = this.framesTable.getColumnClass(adjustedColumn);
            IntegerEditor singleClickEditor = new IntegerEditor(0, 32767);
            singleClickEditor.setClickCountToStart(1);
            this.framesTable.setDefaultEditor(columnClass, singleClickEditor);
        }
    }

    /**
     * Determine a good row height for the frames table.
     * We'll pull one of the cells, get its font size, and use a multiple of that.
     * @return  row height
     */
    private int calcGoodFramesTableRowHeight() {
        Font tableFont = this.framesTable.getFont();
        int fontSize = tableFont.getSize();
        return 3 * fontSize;
    }

    /**
     *  File/Preferences menu item responder.
     *  Open preferences window to display and record preferences
     */
    private void prefsMenuItemActionPerformed() {
        // Create dialog initialized with preferences
        PrefsWindow prefsWindow = new PrefsWindow(this);
        prefsWindow.setUpUI(this.preferences);
        //  Show dialog
        prefsWindow.setVisible(true);
    }

    /**
     * User has modified the Server Address field.  Validate and store in data model.
     */
    private void serverAddressFieldActionPerformed() {
        String proposedServerAddress = this.serverAddressField.getText().trim();
        boolean valid = false;
        if (RmNetUtils.validateIpAddress(proposedServerAddress)) {
            valid = true;
        } else if (RmNetUtils.validateHostName(proposedServerAddress)) {
            valid = true;
        }
        if (valid) {
            this.dataModel.setServerAddress(proposedServerAddress);
            this.setLocalOrRemoteMessage();
            this.makeDirty();
        }
        this.recordTextFieldValidity(this.serverAddressField, valid);
        this.enableProceedButton();
    }

    /**
     * User has modified the Port Number field.  Validate and store in data model.
     */
    private void portNumberFieldActionPerformed() {
        String proposedValue = this.portNumberField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(proposedValue, 0, 65535);
            valid = validation.left;
            if (valid) {
                this.dataModel.setPortNumber(validation.right);
                this.makeDirty();
            }
        }
        this.recordTextFieldValidity(this.portNumberField, valid);
        this.enableProceedButton();
    }

    /**
     * User has modified the Target ADUs field.  Validate and store in data model.
     */
    private void targetADUfieldActionPerformed() {
        String proposedValue = this.targetADUfield.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(proposedValue, 0, 65535);
            valid = validation.left;
            if (valid) {
                this.dataModel.setTargetADUs(validation.right);
                this.makeDirty();
            }
        }
        this.recordTextFieldValidity(this.targetADUfield, valid);
        this.enableProceedButton();
    }

    /**
     * User has modified the ADU Tolerance field.  Validate and store in data model.
     */
    private void aduToleranceFieldActionPerformed() {
        String proposedValue = this.aduToleranceField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, 0.0, 100.0);
            valid = validation.left;
            if (valid) {
                this.dataModel.setAduTolerance(validation.right / 100.0);
                this.makeDirty();
            }
        }
        this.recordTextFieldValidity(this.aduToleranceField, valid);
        this.enableProceedButton();
    }

    /**
     * User has modified the Light Source Altitude field.  Validate and store in data model.
     */
    private void lightSourceAltFieldActionPerformed() {
        String proposedValue = this.lightSourceAltField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, -90.0, +90.0);
            valid = validation.left;
            if (valid) {
                this.dataModel.setLightSourceAlt(validation.right );
                this.makeDirty();
            }
        }
        this.recordTextFieldValidity(this.lightSourceAltField, valid);
        this.enableProceedButton();
        this.enableSlewControls();
    }

    /**
     * User has modified the Light Source Azimuth field.  Validate and store in data model.
     */
    private void lightSourceAzFieldActionPerformed() {
        String proposedValue = this.lightSourceAzField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, -360.0, +360.0);
            valid = validation.left;
            if (valid) {
                this.dataModel.setLightSourceAz(validation.right );
                this.makeDirty();
            }
        }
        this.recordTextFieldValidity(this.lightSourceAzField, valid);
        this.enableProceedButton();
        this.enableSlewControls();
    }

    /**
     * User has modified the Dithering Radius field.  Validate and store in data model.
     */
    private void ditherRadiusFieldActionPerformed() {
        String proposedValue = this.ditherRadiusField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, .001, 360.0*60*60);
            valid = validation.left;
            if (valid) {
                this.dataModel.setDitherRadius(validation.right );
                this.makeDirty();
            }
        }
        this.recordTextFieldValidity(this.ditherRadiusField, valid);
        this.enableProceedButton();
    }

    /**
     * User has modified the Dithering Maximum Radius field.  Validate and store in data model.
     */
    private void ditherMaximumFieldActionPerformed() {
        String proposedValue = this.ditherMaximumField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, .001, 360.0*60*60);
            valid = validation.left;
            if (valid) {
                this.dataModel.setDitherMaximum(validation.right );
                this.makeDirty();
            }
        }
        this.recordTextFieldValidity(this.ditherMaximumField, valid);
        this.enableProceedButton();
    }

    /**
     * Focus lost on a text field is the same as its Action event
     */
    private void serverAddressFieldFocusLost() {
        this.serverAddressFieldActionPerformed();
    }

    private void portNumberFieldFocusLost() {
        this.portNumberFieldActionPerformed();
    }

    private void targetADUfieldFocusLost() {
        this.targetADUfieldActionPerformed();
    }

    private void aduToleranceFieldFocusLost() {
        this.aduToleranceFieldActionPerformed();
    }

    private void lightSourceAltFieldFocusLost() {
        this.lightSourceAltFieldActionPerformed();
    }

    private void lightSourceAzFieldFocusLost() {
        this.lightSourceAzFieldActionPerformed();
    }

    private void ditherRadiusFieldFocusLost() {
        this.ditherRadiusFieldActionPerformed();
    }

    private void ditherMaximumFieldFocusLost() {
        this.ditherMaximumFieldActionPerformed();
    }


    /**
     * Record the validity of the given text field.
     * In a dict indexed by the text field, record the validity state so we can, later, quickly check
     * if all the fields are valid.  Also colour the field red if it is not valid.
     * @param theField      The Swift JTextField being validated
     * @param isValid       Whether that field contains valid data
     */
    private void recordTextFieldValidity(JTextField theField, boolean isValid) {
        //  Record validity in map
        if (this.fieldValidity.containsKey(theField)) {
            this.fieldValidity.replace(theField, isValid);
        } else {
            this.fieldValidity.put(theField, isValid);
        }

        //  Set background colour
        Color backgroundColor = Color.RED;
        if (isValid) {
            backgroundColor = Color.WHITE;
        }
        theField.setBackground(backgroundColor);
    }

    /**
     * Determine if a given field is recorded as valid.
     * It's valid if it is not in the table at all (since it's only put there on first invalidation)
     * or if it's in the table with a true validity
     */

    private boolean fieldIsValid(JTextField fieldToCheck) {
        return this.fieldValidity.getOrDefault(fieldToCheck, true);
    }

    /**
     * Enable the "Proceed" button only if there are no outstanding invalid fields, at least one frame set
     * to be captured, and a valid destination. (Either autosave, or local folder which has been set).
     */
    public void enableProceedButton() {
        boolean destinationOk = this.dataModel.getUseTheSkyAutosave() || (this.dataModel.getLocalPath() != null);
        boolean allValid = this.allTextFieldsValid();
        boolean atLeastOne = this.dataModel.atLeastOneFrameSetWanted();
        String message = null;
        if (!destinationOk) {
            message = "you selected \"Use Local Folder\" but have not set a local destination";
        }
        if (!allValid) {
            message = "there are invalid text fields";
        }
        if (!atLeastOne) {
            message = "no frames are requested in the plan";
        }
        this.proceedButton.setEnabled(allValid && atLeastOne && destinationOk);
        if (this.proceedButton.isEnabled()) {
            this.proceedButton.setToolTipText("Begin the acquisition session");
        } else {
            this.proceedButton.setToolTipText("Disabled because " + message + ".");
        }
    }

    /**
     * Check all the fields recorded in the field validity map and report if they are all valid
     * @return (boolean)       Indication that all fields are valid
     */
    private boolean allTextFieldsValid() {
        for (HashMap.Entry<JTextField,Boolean> entry : this.fieldValidity.entrySet()) {
            boolean isValid = entry.getValue();
            if (!isValid) {
                return false;
            }
        }
        return true;
    }

    /**
     * Mark the file as having unsaved data so we'll prompt to save
     */
    public void makeDirty() {
        this.documentIsDirty = true;
    }

    private void makeNotDirty() {
        this.documentIsDirty = false;
    }

    private boolean isDirty() {
        return this.documentIsDirty;
    }

    /**
     * Store the "Use Filter Wheel" setting from the checkbox to the data model
     */
    private void useFilterWheelCheckboxActionPerformed() {
        this.dataModel.setUseFilterWheel(this.useFilterWheelCheckbox.isSelected());
        //  Re-do table to reflect change to filter wheel setting, since this has a large
        //  impact on the plan (removing all filters or adding them back)
        this.dataModel.generateDataTables(this.preferences, this.dataModel.getUseFilterWheel());
        this.frameTableModel = new FrameTableModel(this, dataModel);
        this.framesTable.setModel(frameTableModel);
        this.makeDirty();
    }

    /**
     * Store the "Warm Up When Done" setting from the checkbox to the data model
     */
    private void warmWhenDoneCheckboxActionPerformed() {
        this.dataModel.setWarmUpWhenDone(this.warmWhenDoneCheckbox.isSelected());
        this.makeDirty();
    }

    /**
     * Store the "Use TheSkyX Autosave Folder" setting from the checkbox to the data model
     */
    private void useAutosaveButtonActionPerformed() {
        this.dataModel.setUseTheSkyAutosave(this.useAutosaveButton.isSelected());
        this.enableProceedButton();
        this.makeDirty();
    }

    /**
     * Store the "Use Local Folder" setting from the checkbox to the data model.
     * There is no separate setting for this - it is the inverse of the Use TheSkyX Autosave setting
     */
    private void useLocalFolderButtonActionPerformed() {
        this.dataModel.setUseTheSkyAutosave(this.useAutosaveButton.isSelected());
        this.enableProceedButton();
        this.makeDirty();
    }

    /**
     * Respond to button that asks us to query the TheSkyX server for the Autosave path
     */
    private void queryAutosaveButtonActionPerformed() {
        String message;
        try {
            TheSkyXServer server = new TheSkyXServer(this.dataModel.getServerAddress(), this.dataModel.getPortNumber());
            message = server.getCameraAutosavePath();
        } catch (IOException e) {
            message = "I/O Error";
        }
        this.destinationPath.setText(message);
        this.useAutosaveButton.setSelected(true);
        this.useLocalFolderButton.setSelected(false);
        this.dataModel.setUseTheSkyAutosave(true);
    }

    /**
     * Respond to button that asks us to select a file folder local to this computer
     */
    private void setLocalFolderButtonActionPerformed() {
        //  Use a file dialog to get the folder to receive files.  Unfortunately we have to use JFileChooser
        //  since the AWT FileDialog can't do just directories.  I say Unfortunately because JFileChooser looks
        //  nothing like the standard system dialogs, while FDialog does.
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = directoryChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = directoryChooser.getSelectedFile();
            this.destinationPath.setText(file.getAbsolutePath());
            this.dataModel.setLocalPath(file.getAbsolutePath());
            this.makeDirty();
            this.enableProceedButton();
            this.useAutosaveButton.setSelected(false);
            this.useLocalFolderButton.setSelected(true);
            this.dataModel.setUseTheSkyAutosave(false);
        }
    }

    /**
     * Store the "Control Mount" setting from the window to the data model
     */
    private void controlMountCheckboxActionPerformed() {
        this.dataModel.setControlMount(this.controlMountCheckbox.isSelected());
        this.makeDirty();
        this.enableSlewControls();
    }

    /**
     * Store the "Home Mount" setting from the window to the data model
     */
    private void homeMountCheckboxActionPerformed() {
        this.dataModel.setHomeMount(this.homeMountCheckbox.isSelected());
        this.makeDirty();
    }

    /**
     * Store the "Slew to Light Source" setting from the window to the data model
     */
    private void slewToLightCheckboxActionPerformed() {
        this.dataModel.setSlewToLight(this.slewToLightCheckbox.isSelected());
        this.makeDirty();
    }

    /**
     * Store the "Tracking Off" setting from the window to the data model
     */
    private void trackingOffCheckboxActionPerformed() {
        this.dataModel.setTrackingOff(this.trackingOffCheckbox.isSelected());
        this.makeDirty();
    }

    /**
     * Store the "Park When Done" setting from the window to the data model
     */
    private void parkWhenDoneCheckboxActionPerformed() {
        this.dataModel.setParkWhenDone(this.parkWhenDoneCheckbox.isSelected());
        this.makeDirty();
    }

    /**
     * Store the "Dither Flats" setting from the window to the data model
     */
    private void ditherFlatsCheckboxActionPerformed() {
        this.dataModel.setDitherFlats(this.ditherFlatsCheckbox.isSelected());
        this.makeDirty();
    }

    /**
     * Respond to button asking us to read the current pointing location from the mount
     */
    private void readScopePositionButtonActionPerformed() {
        try {
            TheSkyXServer server = new TheSkyXServer(this.preferences.getServerAddress(),
                    this.preferences.getPortNumber());
            ImmutablePair<Double,Double> serverResponse = server.getScopeAltAz();
            double altitude = serverResponse.left;
            double azimuth = serverResponse.right;

            this.dataModel.setLightSourceAlt(altitude);
            this.dataModel.setLightSourceAz(azimuth);

            this.lightSourceAltField.setText(String.format("%.8f", altitude));
            this.lightSourceAzField.setText(String.format("%.8f",azimuth));

            this.makeDirty();

            this.readScopeMessage.setText("Read OK");
        } catch (IOException e) {
            this.readScopeMessage.setText("I/O Error");
        } catch (NumberFormatException e) {
            this.readScopeMessage.setText("Server Error");
        }
    }

    /**
     * Manually-manage the enablement of the slewing controls - the cancel and slew buttons and the alt/az fields
     * We do this manually so we can manually suspend them during slew.  Doing them with bindings, as most of the
     * other controls are handled, prevents manual adjustment.
     */
    private void enableSlewControls() {

        //  The Slew button is enabled only if "Control" scope is on and both Alt and Az fields are valid
        boolean slewEnabled = this.controlMountCheckbox.isSelected()
                && this.fieldIsValid(this.lightSourceAltField)
                && this.fieldIsValid(this.lightSourceAzField);
        this.slewScopeButton.setEnabled(slewEnabled);

        //  The Alt and Az fields are enabled if Control is selected
        this.lightSourceAltField.setEnabled(this.controlMountCheckbox.isSelected());
        this.lightSourceAzField.setEnabled(this.controlMountCheckbox.isSelected());
    }

    /**
     * Slew the mount to the given light source location.
     * Slew is asynchronous, and we need to wait for it.  While it is slewing we'd like the Cancel
     * button to be available, but not the Slew button or the coordinates - if those are changed or
     * clicked during the slew, it's difficult to think what might happen.
     *    Disable buttons that would cause problems
     *    Do the slew, as a sub-thread so the UI remains responsive
     *    Wait for the slew to finish or be cancelled
     *    Set everything's enablement back to what it was before the slew
     * Later: While the slew is running, we'll also display "Slewing" in the status message area and, with
     * a timer, we'll pulse it red to black.
     */
    private void slewScopeButtonActionPerformed() {

        //  Things we'd like enabled and disabled during slew
        this.rememberProceedEnabled = this.proceedButton.isEnabled();
        this.proceedButton.setEnabled(false);
        this.slewScopeButton.setEnabled(false);
        this.cancelSlewButton.setEnabled(true);
        this.lightSourceAltField.setEnabled(false);
        this.lightSourceAzField.setEnabled(false);

        //  Do the slew, wait for it to end or be cancelled
        this.slewScopeToLightSource();

    }

    /**
     * Spawn a sub-thread to slew the scope
     */
    private void slewScopeToLightSource()  {

        //  Start the slew thread
        this.slewScopeRunnable = new SlewScopeThread(this, this.dataModel.getServerAddress(),
                this.dataModel.getPortNumber(), this.dataModel.getLightSourceAlt(),
                this.dataModel.getLightSourceAz());
        this.slewScopeThread = new Thread(slewScopeRunnable);
        this.slewScopeThread.start();

        //  Start the slew timer to pulse the "slewing" message
        this.slewMessage.setText("Slewing");
        this.slewMessageIsPulsed = false;
        this.slewingFeedbackTask = new SlewingFeedbackTask(this);
        this.slewingFeedbackTimer = new Timer();
        this.slewingFeedbackTimer.scheduleAtFixedRate(this.slewingFeedbackTask,
                Common.SLEWING_FEEDBACK_INTERVAL_MILLISECONDS,
                Common.SLEWING_FEEDBACK_INTERVAL_MILLISECONDS);

    }

    /**
     * The timer that is running to "pulse" the slewing message has fired.
     * Alternate the message between two colours to provide a visual pulsing effect.
     */
    public void fireSlewFeedbackTimer() {
        Color messageColour = this.slewMessageIsPulsed ? Color.RED : Color.BLACK;
        this.slewMessageIsPulsed = !this.slewMessageIsPulsed;
        this.slewMessage.setForeground(messageColour);
    }

    /**
     * The Thread slewing the scope will tell us when it is done by messaging this method
      * @param finishedMessage      Null if normal completion, otherwise an error message
     */
    public void slewThreadFinished(String finishedMessage) {
        this.slewScopeButton.setEnabled(true);
        this.cancelSlewButton.setEnabled(false);
        this.lightSourceAltField.setEnabled(true);
        this.lightSourceAzField.setEnabled(true);
        this.proceedButton.setEnabled(this.rememberProceedEnabled);

        this.slewScopeThread = null;
        this.slewScopeRunnable = null;

        //  Cancel the feedback timer
        if (this.slewingFeedbackTimer != null) {
            this.slewingFeedbackTimer.cancel();
            this.slewingFeedbackTimer = null;
        }
        if (this.slewingFeedbackTask != null) {
            this.slewingFeedbackTask.cancel();
            this.slewingFeedbackTask = null;
        }

        this.slewMessage.setForeground(Color.BLACK);
        //noinspection ReplaceNullCheck
        if (finishedMessage == null) {
            this.slewMessage.setText(" ");
        } else {
            this.slewMessage.setText(finishedMessage);
        }

    }

    /**
     * Respond to button asking us to cancel the slew that is in progress
     */
    private void cancelSlewButtonActionPerformed() {
        //  Find and cancel the task handling the slew
        if (this.slewScopeThread != null) {
            this.slewScopeThread.interrupt();
        }
    }

    /**
     * Respond to button to turn on all filter/binning combinations in the table.
     * All values in the table are set to the default frame count.
     */
    private void allOnButtonActionPerformed() {
        //  Set all the table cells to the default value
        // Skip column 1, the pseudo-header column
        Integer cellValue = this.dataModel.getDefaultFrameCount();
        for (int rowIndex = 0; rowIndex < this.frameTableModel.getRowCount(); rowIndex++) {
            for (int columnIndex = 1; columnIndex < this.frameTableModel.getColumnCount(); columnIndex++) {
                this.frameTableModel.setValueAt(cellValue, rowIndex, columnIndex);
            }
        }
        this.makeDirty();
    }

    /**
     * Respond to button asking to set table to default filter/binning combinations
     */
    private void defaultsButtonActionPerformed() {
        //  Set all the table cells to the default value or zero depending on the binning availability
        // Skip column 0, the pseudo-header column
        Integer defaultValue = this.dataModel.getDefaultFrameCount();
        Integer zeroValue = 0;
        for (int rowIndex = 0; rowIndex < this.frameTableModel.getRowCount(); rowIndex++) {
            for (int columnIndex = 1; columnIndex < this.frameTableModel.getColumnCount(); columnIndex++) {
                BinningSpec binningSpec = this.dataModel.getBinningInUse(columnIndex - 1);
                if (binningSpec.getAvailability() == BinningAvailability.DEFAULT) {
                    this.frameTableModel.setValueAt(defaultValue, rowIndex, columnIndex);
                } else {
                    //  Has to be available. Can't be OFF or it wouldn't be in the table.
                    assert binningSpec.getAvailability() == BinningAvailability.AVAILABLE;
                    this.frameTableModel.setValueAt(zeroValue, rowIndex, columnIndex);
                }
            }
        }
        this.makeDirty();
    }

    /**
     * Respond to button asking to turn off all filter/binning combinations in the table
     */
    private void allOffButtonActionPerformed() {
        //  Set all the table cells to zero
        // Skip column 1, the pseudo-header column
        Integer cellValue = 0;
        for (int rowIndex = 0; rowIndex < this.frameTableModel.getRowCount(); rowIndex++) {
            for (int columnIndex = 1; columnIndex < this.frameTableModel.getColumnCount(); columnIndex++) {
                this.frameTableModel.setValueAt(cellValue, rowIndex, columnIndex);
            }
        }
        this.makeDirty();
    }

    /**
     * Set message field to indicate whether the defined TheSkyX server is running on this or
     * a remote computer (by analysing whether the server address is known to be local).
     * Enable the "Local" buttons only if running locally.
     */
    private void setLocalOrRemoteMessage() {
        String localityMessage;
        boolean localButtonsEnabled = false;
        if (RmNetUtils.addressIsLocal(this.dataModel.getServerAddress())) {
            localityMessage = "TheSkyX runs on this computer.";
            localButtonsEnabled = true;
        } else {
            localityMessage = "TheSkyX runs remotely.";
            this.useAutosaveButton.setSelected(true);
        }
        this.remoteOrLocalMessage.setText(localityMessage);
        this.useLocalFolderButton.setEnabled(localButtonsEnabled);
        this.setLocalFolderButton.setEnabled(localButtonsEnabled);
    }

    /**
     * Respond to Proceed button to start the acquisition session.  Open the dialog window
     * that is used for the session console, then spawn the thread that does the acquisition.
     */
    public void proceedButtonActionPerformed() {
        //  In case they have a table cell edit in progress and haven't hit Enter, absorb it
        this.endInProgressTableCellEdits();
        if (this.proceedButton.isEnabled()) {

            //  Session console window
            this.sessionWindow = new Session(this);
            ArrayList<FlatSet> flatsToAcquire = this.dataModel.getFlatSetsToAcquire(this.preferences);
            sessionWindow.setUpUI(this.dataModel, flatsToAcquire);
            sessionWindow.setVisible(true);

            //  Start the acquisition thread
            sessionWindow.spawnAcquisitionTask(sessionWindow, flatsToAcquire);
        }
    }

    /**
     * Swing has the unfortunate habit of not processing changes to text fields until their action is
     * triggered by hitting enter or changing focus.  So if the user starts to type something into a text
     * field and then clicks a button, the change they've typed will be lost.  We've handled that for all
     * the regular text fields by catching their "focus lost" actions.  Here we'll detect that a table cell
     * has an edit in progress, and close the edit.
     */
    private void endInProgressTableCellEdits() {
        CellEditor tableCellEditor = this.framesTable.getCellEditor();

        if (tableCellEditor != null) {
            // A cell edit is in progress.  Tell the editor to stop and save.
            assert(tableCellEditor instanceof IntegerEditor);
            IntegerEditor activeEditor = (IntegerEditor) tableCellEditor;
            activeEditor.stopCellEditing();
            this.makeDirty();
        }

        //  Finishing that edit might have changed enablement of certain controls
        this.enableProceedButton();
        this.enableSlewControls();
    }

    /**
     * Respond to "Save As" menu item.  Prompt user for a file name and location and,
     * if they don't cancel, write an XML-encoded version of the data model to the file.
     */
    private void saveAsMenuItemActionPerformed() {

         //  Get file to save to
        FileDialog fileDialog = new FileDialog(this, "Save Plan File", FileDialog.SAVE);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);
        String selectedFile = fileDialog.getFile();

        //  If not cancelled, serialize the data model and write to file
        if (selectedFile != null) {
            String selectedDirectory = fileDialog.getDirectory();
            String fullPath = selectedDirectory + selectedFile;
            if (!fullPath.endsWith(("." + Common.DATA_FILE_SUFFIX))) {
                fullPath += "." + Common.DATA_FILE_SUFFIX;
            }
            File newFile = new File(fullPath);
            this.writeToFile(newFile);
        }
    }

    /**
     * Write the current data model, serialized to XML, to the given file
     * Go through a temporary file so there is no data loss in the event of a crash
     * @param fileToSave        File object of file to be saved
     */
    private void writeToFile(File fileToSave) {
        // Absorb any table cell edit in process
        this.endInProgressTableCellEdits();

        // Write serialized data model to file
        String serialized = this.dataModel.serialize();

        // Write to temporary file then delete and rename old copy
        // This way, if system crashes, either old or new file will still exist - no data loss
        String fileNameWithExtension = fileToSave.getName();
        assert(fileNameWithExtension.endsWith("." + Common.DATA_FILE_SUFFIX));
        String justFileName = Common.simpleFileNameFromPath(fileToSave.getAbsolutePath());
        String directory = fileToSave.getParent();
        try {
            File tempFile = File.createTempFile(justFileName, Common.DATA_FILE_SUFFIX, new File(directory));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.getAbsolutePath()));
            writer.write(serialized);
            writer.close();

            //  Content is now in temporary file.   Delete original file name and rename temporary.
            @SuppressWarnings("unused") boolean ignored = fileToSave.delete();
            if (!tempFile.renameTo(fileToSave)) {
                JOptionPane.showMessageDialog(null,
                        "Unable to rename temporary file after writing.");
            }

            // Set title of main window
            this.setTitle(justFileName);
            //  un-dirty the document
            this.makeNotDirty();
            //  Remember the file path for future saves
            this.filePath = fileToSave.getAbsolutePath();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFound Exception. Not sure how this can happen, probably can't.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Unable to write to file.");
            JOptionPane.showMessageDialog(null, "IO error, unable to save file");
        }

    }


    /**
     * User selected the Open menu.
     * Discard the current data model (checking for a needed save) and prompt the user
     * for a file to load.
     */
    private void openMenuItemActionPerformed() {
        if (protectedSaveProceed(true)) {
            FileDialog fileDialog = new FileDialog(this, "Plan File", FileDialog.LOAD);
            fileDialog.setMultipleMode(false);
            fileDialog.setFilenameFilter((File dir, String name) -> name.endsWith("." + Common.DATA_FILE_SUFFIX));
            fileDialog.setVisible(true);
            String selectedFile = fileDialog.getFile();
            String selectedDirectory = fileDialog.getDirectory();
            String fullPath = selectedDirectory + selectedFile;
            if (selectedFile != null) {
                this.readFromFile(fullPath);
            }
        }
    }

    /**
     * Given full path name of an existing file, read it, decode it, and change over to that data model
     * @param fullPath      Full absolute path name of the file to read
     */
    private void readFromFile(String fullPath) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fullPath));
            String encodedData = new String(encoded, StandardCharsets.US_ASCII);
            DataModel newDataModel = DataModel.newFromXml(encodedData);
            if (newDataModel != null) {
                this.dataModel = null;
                this.setUiFromDataModel(newDataModel, Common.simpleFileNameFromPath(fullPath));
                this.filePath = fullPath;
                this.makeNotDirty();
            }
        } catch (IOException e) {
            System.out.println("Unable to read file.");
            JOptionPane.showMessageDialog(null, "IO error, unable to read file");
        }
    }

    /**
     * We're about to do something that will erase the current frame set plan.  If it is "dirty", i.e.
     * contains changes not yet saved to disk, ask the user if they want to do a save.  If they do, do the
     * save.  There are 3 possible outcomes on a dirty document
     * 1. Do a save
     * 2. Don't do a save, losing the unsaved changes
     * 3. Cancel, don't do the operation that caused this
     * @param offerCancel      Should Cancel button be one of the offered options?
     * @return (boolean)       OK to proceed (i.e. user didn't click "Cancel")
     */
    private boolean protectedSaveProceed(boolean offerCancel) {
        boolean proceed = true;
        if (this.isDirty()) {
            Object[] options;
            if (offerCancel) {
                options = new Object[] { "Discard", "Save", "Cancel"};
            } else {
                options = new Object[] {"Discard", "Save"};
            }
            int result = JOptionPane.showOptionDialog(null,
                    "Your flat frame plan has unsaved changes. "
                            + "Save these or discard them?", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[1]);

            switch (result) {
                case 0:
                    // Discard selected - no need to save
                    break;
                case 1:
                    // Save selected - do a save then proceed
                    this.saveMenuItemActionPerformed();
                    break;
                case 2:
                    // Cancel was selected
                    proceed = false;
                    break;
            }
        }
        return proceed;
    }

    /**
     * SAVE menu invoked.  If we already have a file defined, just re-save it.
     * Otherwise, treat this like a Save-As so the file gets prompted.
     */
    private void saveMenuItemActionPerformed() {
        if (this.filePath.equals("")) {
            this.saveAsMenuItemActionPerformed();
        } else {
            this.writeToFile(new File(this.filePath));
        }
    }

    /**
     * NEW menu invoked. Create a new default data model and load it.
     */
    private void newMenuItemActionPerformed() {
        if (protectedSaveProceed(true)) {
            DataModel newDataModel = DataModel.newInstance(this.preferences);
            newDataModel.generateDataTables(this.preferences, this.preferences.getUseFilterWheel());
            this.dataModel = null;
            this.setUiFromDataModel(newDataModel, Common.UNSAVED_FILE_TITLE);
            this.filePath = "";
            this.makeNotDirty();
        }
    }

    /**
     * User has clicked the system close button on the window.
     * Do an "unsaved data protection" then exit the program
     */
    private void thisWindowClosing() {
        if (protectedSaveProceed(false)) {
            this.setVisible(false);
            System.exit(0);
        }
    }

    /**
     * Quit action invoked.  Check for unsaved data before quitting.
     */
    private void quitMenuItemActionPerformed() {
        if (this.sessionWindow != null) {
            this.sessionWindow.cancelButtonActionPerformed();
            try {
                // Give the cancel a moment to take effect
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Ignoring interrupt while quitting");
            }
        }
        if (this.protectedSaveProceed(false)) {
            //  If the acquisition subtask is running, stop it
            System.exit(0);
        }
    }

    @SuppressWarnings("deprecation")
    private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        menuBar1 = new JMenuBar();
        fileMenu = new JMenu();
        newMenuItem = new JMenuItem();
        openMenuItem = new JMenuItem();
        saveAsMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        prefsMenuItem = new JMenuItem();
        quitMenuItem = new JMenuItem();
        contentPanel = new JPanel();
        label21 = new JLabel();
        vSpacer1 = new JPanel(null);
        serverPanel = new JPanel();
        label1 = new JLabel();
        label6 = new JLabel();
        serverAddressField = new JTextField();
        label7 = new JLabel();
        portNumberField = new JTextField();
        aduPanel = new JPanel();
        label2 = new JLabel();
        label8 = new JLabel();
        targetADUfield = new JTextField();
        label10 = new JLabel();
        label9 = new JLabel();
        aduToleranceField = new JTextField();
        label11 = new JLabel();
        optionsPanel = new JPanel();
        label3 = new JLabel();
        useFilterWheelCheckbox = new JCheckBox();
        warmWhenDoneCheckbox = new JCheckBox();
        mountPanel = new JPanel();
        label4 = new JLabel();
        controlMountCheckbox = new JCheckBox();
        label12 = new JLabel();
        readScopePositionButton = new JButton();
        readScopeMessage = new JLabel();
        homeMountCheckbox = new JCheckBox();
        hSpacer1 = new JPanel(null);
        label13 = new JLabel();
        lightSourceAltField = new JTextField();
        label15 = new JLabel();
        slewToLightCheckbox = new JCheckBox();
        label14 = new JLabel();
        lightSourceAzField = new JTextField();
        label16 = new JLabel();
        trackingOffCheckbox = new JCheckBox();
        slewScopeButton = new JButton();
        cancelSlewButton = new JButton();
        slewMessage = new JLabel();
        parkWhenDoneCheckbox = new JCheckBox();
        label17 = new JLabel();
        ditherRadiusField = new JTextField();
        label19 = new JLabel();
        ditherFlatsCheckbox = new JCheckBox();
        label18 = new JLabel();
        ditherMaximumField = new JTextField();
        label20 = new JLabel();
        destinationPanel = new JPanel();
        label5 = new JLabel();
        panel1 = new JPanel();
        remoteOrLocalMessage = new JLabel();
        useAutosaveButton = new JRadioButton();
        queryAutosaveButton = new JButton();
        useLocalFolderButton = new JRadioButton();
        setLocalFolderButton = new JButton();
        panel2 = new JPanel();
        destinationPath = new JLabel();
        tablePanel = new JPanel();
        hSpacer3 = new JPanel(null);
        frameTableScrollPane = new JScrollPane();
        framesTable = new JTable();
        hSpacer4 = new JPanel(null);
        buttonPanel = new JPanel();
        allOnButton = new JButton();
        defaultsButton = new JButton();
        allOffButton = new JButton();
        hSpacer2 = new JPanel(null);
        proceedButton = new JButton();

        //======== this ========
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                thisWindowClosing();
            }
        });
        var contentPane = getContentPane();
        contentPane.setLayout(new GridLayout());

        //======== menuBar1 ========
        {

            //======== fileMenu ========
            {
                fileMenu.setText("File");

                //---- newMenuItem ----
                newMenuItem.setText("New");
                newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                newMenuItem.addActionListener(e -> newMenuItemActionPerformed());
                fileMenu.add(newMenuItem);

                //---- openMenuItem ----
                openMenuItem.setText("Open");
                openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                openMenuItem.addActionListener(e -> openMenuItemActionPerformed());
                fileMenu.add(openMenuItem);
                fileMenu.addSeparator();

                //---- saveAsMenuItem ----
                saveAsMenuItem.setText("Save As...");
                saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
                saveAsMenuItem.addActionListener(e -> saveAsMenuItemActionPerformed());
                fileMenu.add(saveAsMenuItem);

                //---- saveMenuItem ----
                saveMenuItem.setText("Save");
                saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                saveMenuItem.addActionListener(e -> saveMenuItemActionPerformed());
                fileMenu.add(saveMenuItem);
                fileMenu.addSeparator();

                //---- prefsMenuItem ----
                prefsMenuItem.setText("Preferences");
                prefsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                prefsMenuItem.addActionListener(e -> prefsMenuItemActionPerformed());
                fileMenu.add(prefsMenuItem);
                fileMenu.addSeparator();

                //---- quitMenuItem ----
                quitMenuItem.setText("Quit");
                quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                quitMenuItem.addActionListener(e -> quitMenuItemActionPerformed());
                fileMenu.add(quitMenuItem);
            }
            menuBar1.add(fileMenu);
        }
        setJMenuBar(menuBar1);

        //======== contentPanel ========
        {
            contentPanel.setPreferredSize(new Dimension(857, 600));
            contentPanel.setMinimumSize(new Dimension(642, 600));
            contentPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
            ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};

            //---- label21 ----
            label21.setText("Flat Capture Now");
            label21.setFont(new Font(".SF NS Text", Font.PLAIN, 24));
            label21.setHorizontalAlignment(SwingConstants.CENTER);
            contentPanel.add(label21, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
                GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 0), 0, 0));
            contentPanel.add(vSpacer1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== serverPanel ========
            {
                serverPanel.setBorder(LineBorder.createBlackLineBorder());
                serverPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)serverPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)serverPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
                ((GridBagLayout)serverPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)serverPanel.getLayout()).rowWeights = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0E-4};

                //---- label1 ----
                label1.setText("TheSkyX Server");
                label1.setFont(new Font("Lucida Grande", Font.BOLD, 14));
                serverPanel.add(label1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

                //---- label6 ----
                label6.setText(" Address");
                serverPanel.add(label6, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

                //---- serverAddressField ----
                serverAddressField.setColumns(32);
                serverAddressField.addActionListener(e -> serverAddressFieldActionPerformed());
                serverAddressField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        serverAddressFieldFocusLost();
                    }
                });
                serverPanel.add(serverAddressField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

                //---- label7 ----
                label7.setText(" Port");
                serverPanel.add(label7, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

                //---- portNumberField ----
                portNumberField.setColumns(6);
                portNumberField.addActionListener(e -> portNumberFieldActionPerformed());
                portNumberField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        portNumberFieldFocusLost();
                    }
                });
                serverPanel.add(portNumberField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            contentPanel.add(serverPanel, new GridBagConstraints(0, 2, 1, 2, 0.5, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(4, 4, 9, 9), 0, 0));

            //======== aduPanel ========
            {
                aduPanel.setBorder(LineBorder.createBlackLineBorder());
                aduPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)aduPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
                ((GridBagLayout)aduPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)aduPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
                ((GridBagLayout)aduPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                //---- label2 ----
                label2.setText("Flat Frame Exposure Level");
                label2.setFont(new Font("Lucida Grande", Font.BOLD, 14));
                aduPanel.add(label2, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- label8 ----
                label8.setText(" Target:");
                aduPanel.add(label8, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- targetADUfield ----
                targetADUfield.setColumns(8);
                targetADUfield.addActionListener(e -> targetADUfieldActionPerformed());
                targetADUfield.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        targetADUfieldFocusLost();
                    }
                });
                aduPanel.add(targetADUfield, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label10 ----
                label10.setText("ADUs ");
                aduPanel.add(label10, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- label9 ----
                label9.setText(" Within:");
                aduPanel.add(label9, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- aduToleranceField ----
                aduToleranceField.setColumns(8);
                aduToleranceField.addActionListener(e -> aduToleranceFieldActionPerformed());
                aduToleranceField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        aduToleranceFieldFocusLost();
                    }
                });
                aduPanel.add(aduToleranceField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- label11 ----
                label11.setText("%");
                aduPanel.add(label11, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            contentPanel.add(aduPanel, new GridBagConstraints(2, 2, 1, 1, 0.5, 0.0,
                GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                new Insets(4, 4, 9, 4), 0, 0));

            //======== optionsPanel ========
            {
                optionsPanel.setBorder(LineBorder.createBlackLineBorder());
                optionsPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)optionsPanel.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)optionsPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)optionsPanel.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
                ((GridBagLayout)optionsPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                //---- label3 ----
                label3.setText("Options");
                label3.setFont(new Font("Lucida Grande", Font.BOLD, 14));
                optionsPanel.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- useFilterWheelCheckbox ----
                useFilterWheelCheckbox.setText("Use Filter Wheel");
                useFilterWheelCheckbox.addActionListener(e -> useFilterWheelCheckboxActionPerformed());
                optionsPanel.add(useFilterWheelCheckbox, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- warmWhenDoneCheckbox ----
                warmWhenDoneCheckbox.setText("When done warm up CCD");
                warmWhenDoneCheckbox.addActionListener(e -> warmWhenDoneCheckboxActionPerformed());
                optionsPanel.add(warmWhenDoneCheckbox, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            contentPanel.add(optionsPanel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                new Insets(4, 4, 9, 9), 0, 0));

            //======== mountPanel ========
            {
                mountPanel.setBorder(LineBorder.createBlackLineBorder());
                mountPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)mountPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
                ((GridBagLayout)mountPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
                ((GridBagLayout)mountPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout)mountPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- label4 ----
                label4.setText("Mount Control");
                label4.setFont(new Font("Lucida Grande", Font.BOLD, 14));
                mountPanel.add(label4, new GridBagConstraints(0, 0, 5, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- controlMountCheckbox ----
                controlMountCheckbox.setText("Control Mount");
                controlMountCheckbox.addActionListener(e -> controlMountCheckboxActionPerformed());
                mountPanel.add(controlMountCheckbox, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label12 ----
                label12.setText("Light Source");
                mountPanel.add(label12, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- readScopePositionButton ----
                readScopePositionButton.setText("Read Scope");
                readScopePositionButton.addActionListener(e -> readScopePositionButtonActionPerformed());
                mountPanel.add(readScopePositionButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- readScopeMessage ----
                readScopeMessage.setText(" ");
                readScopeMessage.setMaximumSize(new Dimension(4, 32));
                readScopeMessage.setMinimumSize(new Dimension(4, 32));
                readScopeMessage.setPreferredSize(new Dimension(4, 32));
                mountPanel.add(readScopeMessage, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- homeMountCheckbox ----
                homeMountCheckbox.setText("Home Mount");
                homeMountCheckbox.addActionListener(e -> homeMountCheckboxActionPerformed());
                mountPanel.add(homeMountCheckbox, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- hSpacer1 ----
                hSpacer1.setMinimumSize(new Dimension(32, 12));
                mountPanel.add(hSpacer1, new GridBagConstraints(1, 2, 1, 1, 0.1, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label13 ----
                label13.setText("Alt");
                mountPanel.add(label13, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- lightSourceAltField ----
                lightSourceAltField.setColumns(8);
                lightSourceAltField.addActionListener(e -> lightSourceAltFieldActionPerformed());
                lightSourceAltField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lightSourceAltFieldFocusLost();
                    }
                });
                mountPanel.add(lightSourceAltField, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label15 ----
                label15.setText("degrees ");
                mountPanel.add(label15, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- slewToLightCheckbox ----
                slewToLightCheckbox.setText("Slew to Light Source");
                slewToLightCheckbox.addActionListener(e -> slewToLightCheckboxActionPerformed());
                mountPanel.add(slewToLightCheckbox, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label14 ----
                label14.setText("Az");
                mountPanel.add(label14, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- lightSourceAzField ----
                lightSourceAzField.addActionListener(e -> lightSourceAzFieldActionPerformed());
                lightSourceAzField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lightSourceAzFieldFocusLost();
                    }
                });
                mountPanel.add(lightSourceAzField, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label16 ----
                label16.setText("degrees ");
                mountPanel.add(label16, new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- trackingOffCheckbox ----
                trackingOffCheckbox.setText("Tracking Off");
                trackingOffCheckbox.addActionListener(e -> trackingOffCheckboxActionPerformed());
                mountPanel.add(trackingOffCheckbox, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- slewScopeButton ----
                slewScopeButton.setText("Slew");
                slewScopeButton.addActionListener(e -> slewScopeButtonActionPerformed());
                mountPanel.add(slewScopeButton, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- cancelSlewButton ----
                cancelSlewButton.setText("Cancel");
                cancelSlewButton.setEnabled(false);
                cancelSlewButton.addActionListener(e -> cancelSlewButtonActionPerformed());
                mountPanel.add(cancelSlewButton, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- slewMessage ----
                slewMessage.setText("text");
                slewMessage.setFont(slewMessage.getFont().deriveFont(11f));
                mountPanel.add(slewMessage, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- parkWhenDoneCheckbox ----
                parkWhenDoneCheckbox.setText("Park When Done");
                parkWhenDoneCheckbox.addActionListener(e -> parkWhenDoneCheckboxActionPerformed());
                mountPanel.add(parkWhenDoneCheckbox, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label17 ----
                label17.setText("Radius");
                mountPanel.add(label17, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- ditherRadiusField ----
                ditherRadiusField.addActionListener(e -> ditherRadiusFieldActionPerformed());
                ditherRadiusField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        ditherRadiusFieldFocusLost();
                    }
                });
                mountPanel.add(ditherRadiusField, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label19 ----
                label19.setText("arc sec ");
                mountPanel.add(label19, new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- ditherFlatsCheckbox ----
                ditherFlatsCheckbox.setText("Dither Flats");
                ditherFlatsCheckbox.addActionListener(e -> ditherFlatsCheckboxActionPerformed());
                mountPanel.add(ditherFlatsCheckbox, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- label18 ----
                label18.setText("Maximum");
                mountPanel.add(label18, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- ditherMaximumField ----
                ditherMaximumField.addActionListener(e -> ditherMaximumFieldActionPerformed());
                ditherMaximumField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        ditherMaximumFieldFocusLost();
                    }
                });
                mountPanel.add(ditherMaximumField, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- label20 ----
                label20.setText("arc sec ");
                mountPanel.add(label20, new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            contentPanel.add(mountPanel, new GridBagConstraints(2, 4, 1, 2, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(4, 4, 9, 4), 0, 0));

            //======== destinationPanel ========
            {
                destinationPanel.setBorder(LineBorder.createBlackLineBorder());
                destinationPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)destinationPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)destinationPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
                ((GridBagLayout)destinationPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                ((GridBagLayout)destinationPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- label5 ----
                label5.setText("Destination Folder");
                label5.setFont(new Font("Lucida Grande", Font.BOLD, 14));
                destinationPanel.add(label5, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //======== panel1 ========
                {
                    panel1.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
                    ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
                    ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                    //---- remoteOrLocalMessage ----
                    remoteOrLocalMessage.setText("<html><i>TheSkyX is running on a remote machine.</i></html>");
                    remoteOrLocalMessage.setFocusable(false);
                    remoteOrLocalMessage.setHorizontalTextPosition(SwingConstants.CENTER);
                    panel1.add(remoteOrLocalMessage, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                destinationPanel.add(panel1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 4, 5, 0), 0, 0));

                //---- useAutosaveButton ----
                useAutosaveButton.setText("Use TheSkyX Autosave Folder");
                useAutosaveButton.addActionListener(e -> useAutosaveButtonActionPerformed());
                destinationPanel.add(useAutosaveButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- queryAutosaveButton ----
                queryAutosaveButton.setText("Query");
                queryAutosaveButton.addActionListener(e -> queryAutosaveButtonActionPerformed());
                destinationPanel.add(queryAutosaveButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- useLocalFolderButton ----
                useLocalFolderButton.setText("Use Specified Local Folder");
                useLocalFolderButton.addActionListener(e -> useLocalFolderButtonActionPerformed());
                destinationPanel.add(useLocalFolderButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- setLocalFolderButton ----
                setLocalFolderButton.setText("Set");
                setLocalFolderButton.addActionListener(e -> setLocalFolderButtonActionPerformed());
                destinationPanel.add(setLocalFolderButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //======== panel2 ========
                {
                    panel2.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0};
                    ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
                    ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                    //---- destinationPath ----
                    destinationPath.setText("Display of save-folder path name goes here");
                    destinationPath.setFont(new Font("Lucida Grande", Font.PLAIN, 8));
                    panel2.add(destinationPath, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                destinationPanel.add(panel2, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 4, 0, 0), 0, 0));
            }
            contentPanel.add(destinationPanel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                new Insets(4, 4, 9, 9), 0, 0));

            //======== tablePanel ========
            {
                tablePanel.setBorder(LineBorder.createBlackLineBorder());
                tablePanel.setPreferredSize(new Dimension(766, 300));
                tablePanel.setLayout(new GridBagLayout());
                ((GridBagLayout)tablePanel.getLayout()).columnWidths = new int[] {155, 0, 150, 0};
                ((GridBagLayout)tablePanel.getLayout()).rowHeights = new int[] {0, 0};
                ((GridBagLayout)tablePanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout)tablePanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                //---- hSpacer3 ----
                hSpacer3.setMinimumSize(new Dimension(100, 12));
                tablePanel.add(hSpacer3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //======== frameTableScrollPane ========
                {

                    //---- framesTable ----
                    framesTable.setRowSelectionAllowed(false);
                    framesTable.setToolTipText("<html>Number of flat frames to be acquired for each filter/binning combination. Click a cell to set it to zero. Click and type to change it to a different value.</html>");
                    framesTable.setGridColor(new Color(100, 100, 100));
                    framesTable.setIntercellSpacing(new Dimension(1, 10));
                    framesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    framesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                    framesTable.setRowHeight(24);
                    framesTable.setFocusable(false);
                    frameTableScrollPane.setViewportView(framesTable);
                }
                tablePanel.add(frameTableScrollPane, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- hSpacer4 ----
                hSpacer4.setMinimumSize(new Dimension(100, 12));
                tablePanel.add(hSpacer4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));
            }
            contentPanel.add(tablePanel, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== buttonPanel ========
            {
                buttonPanel.setLayout(new GridLayout());

                //---- allOnButton ----
                allOnButton.setText("All On");
                allOnButton.setMaximumSize(new Dimension(50, 29));
                allOnButton.setMinimumSize(new Dimension(50, 29));
                allOnButton.addActionListener(e -> allOnButtonActionPerformed());
                buttonPanel.add(allOnButton);

                //---- defaultsButton ----
                defaultsButton.setText("Defaults");
                defaultsButton.addActionListener(e -> defaultsButtonActionPerformed());
                buttonPanel.add(defaultsButton);

                //---- allOffButton ----
                allOffButton.setText("All Off");
                allOffButton.addActionListener(e -> allOffButtonActionPerformed());
                buttonPanel.add(allOffButton);

                //---- hSpacer2 ----
                hSpacer2.setMinimumSize(new Dimension(100, 12));
                buttonPanel.add(hSpacer2);

                //---- proceedButton ----
                proceedButton.setText("Proceed");
                proceedButton.addActionListener(e -> proceedButtonActionPerformed());
                buttonPanel.add(proceedButton);
            }
            contentPanel.add(buttonPanel, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0,
                GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                new Insets(4, 4, 4, 4), 0, 0));
        }
        contentPane.add(contentPanel);
        pack();
        setLocationRelativeTo(getOwner());

        //---- buttonGroup1 ----
        var buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(useAutosaveButton);
        buttonGroup1.add(useLocalFolderButton);

        //---- bindings ----
        bindingGroup = new BindingGroup();
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            controlMountCheckbox, BeanProperty.create("selected"),
            homeMountCheckbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            controlMountCheckbox, BeanProperty.create("selected"),
            slewToLightCheckbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            controlMountCheckbox, BeanProperty.create("selected"),
            trackingOffCheckbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            controlMountCheckbox, BeanProperty.create("selected"),
            parkWhenDoneCheckbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            controlMountCheckbox, BeanProperty.create("selected"),
            ditherFlatsCheckbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            controlMountCheckbox, BeanProperty.create("selected"),
            readScopePositionButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            ditherFlatsCheckbox, BeanProperty.create("selected"),
            ditherRadiusField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            ditherFlatsCheckbox, BeanProperty.create("selected"),
            ditherMaximumField, BeanProperty.create("enabled")));
        bindingGroup.bind();
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JMenuBar menuBar1;
    private JMenu fileMenu;
    private JMenuItem newMenuItem;
    private JMenuItem openMenuItem;
    private JMenuItem saveAsMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem prefsMenuItem;
    private JMenuItem quitMenuItem;
    private JPanel contentPanel;
    private JLabel label21;
    private JPanel vSpacer1;
    private JPanel serverPanel;
    private JLabel label1;
    private JLabel label6;
    private JTextField serverAddressField;
    private JLabel label7;
    private JTextField portNumberField;
    private JPanel aduPanel;
    private JLabel label2;
    private JLabel label8;
    private JTextField targetADUfield;
    private JLabel label10;
    private JLabel label9;
    private JTextField aduToleranceField;
    private JLabel label11;
    private JPanel optionsPanel;
    private JLabel label3;
    private JCheckBox useFilterWheelCheckbox;
    private JCheckBox warmWhenDoneCheckbox;
    private JPanel mountPanel;
    private JLabel label4;
    private JCheckBox controlMountCheckbox;
    private JLabel label12;
    private JButton readScopePositionButton;
    private JLabel readScopeMessage;
    private JCheckBox homeMountCheckbox;
    private JPanel hSpacer1;
    private JLabel label13;
    private JTextField lightSourceAltField;
    private JLabel label15;
    private JCheckBox slewToLightCheckbox;
    private JLabel label14;
    private JTextField lightSourceAzField;
    private JLabel label16;
    private JCheckBox trackingOffCheckbox;
    private JButton slewScopeButton;
    private JButton cancelSlewButton;
    private JLabel slewMessage;
    private JCheckBox parkWhenDoneCheckbox;
    private JLabel label17;
    private JTextField ditherRadiusField;
    private JLabel label19;
    private JCheckBox ditherFlatsCheckbox;
    private JLabel label18;
    private JTextField ditherMaximumField;
    private JLabel label20;
    private JPanel destinationPanel;
    private JLabel label5;
    private JPanel panel1;
    private JLabel remoteOrLocalMessage;
    private JRadioButton useAutosaveButton;
    private JButton queryAutosaveButton;
    private JRadioButton useLocalFolderButton;
    private JButton setLocalFolderButton;
    private JPanel panel2;
    private JLabel destinationPath;
    private JPanel tablePanel;
    private JPanel hSpacer3;
    private JScrollPane frameTableScrollPane;
    private JTable framesTable;
    private JPanel hSpacer4;
    private JPanel buttonPanel;
    private JButton allOnButton;
    private JButton defaultsButton;
    private JButton allOffButton;
    private JPanel hSpacer2;
    private JButton proceedButton;
    private BindingGroup bindingGroup;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
