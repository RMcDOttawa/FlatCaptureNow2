import java.awt.*;
import java.util.Timer;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
/*
 * Created by JFormDesigner on Tue Feb 25 15:18:41 EST 2020
 */



/**
 * Main window controller for the application
 * @author Richard McDonald
 */
public class MainWindow extends JFrame {

    private AppPreferences preferences = null;
    private DataModel dataModel = null;

    //  Map to record valid/invalid state of validated fields in the UI
    //  Fields not in the map are considered valid.  Fields in the map are valid or not
    //  depending on the boolean stored.
    private HashMap<JTextField, Boolean> fieldValidity = null;
    private SlewScopeThread slewScopeRunnable;
    private Thread slewScopeThread;
    private boolean rememberProceedEnabled;  // Remember proceed state during a slew

    private SlewingFeedbackTask slewingFeedbackTask = null;
    private Timer slewingFeedbackTimer = null;
    private boolean slewMessageIsPulsed;
    private FrameTableModel frameTableModel;

    public MainWindow ( AppPreferences preferences,
                        DataModel dataModel) {
        this.preferences = preferences;
        this.dataModel = dataModel;

        //  Catch main Quit menu so we can check for unsaved data
//        if (Desktop.isDesktopSupported()) {
//            Desktop desktop = Desktop.getDesktop();
//            desktop.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
//            desktop.setQuitHandler((QuitEvent evt, QuitResponse res) -> quitMenuItemClicked());
//        }
        initComponents();
    }

    public void setUiFromDataModel() {
        this.fieldValidity = new HashMap<JTextField, Boolean>();

        this.serverAddressField.setText(this.dataModel.getServerAddress());
        this.portNumberField.setText(String.valueOf(this.dataModel.getPortNumber()));

        this.targetADUfield.setText(String.valueOf(this.dataModel.getTargetADUs()));
        this.aduToleranceField.setText(String.valueOf(this.dataModel.getAduTolerance() * 100.0));

        this.useFilterWheelCheckbox.setSelected(this.dataModel.getUseFilterWheel());
        this.warmWhenDoneCheckbox.setSelected(this.dataModel.getWarmUpWhenDone());

        if (this.dataModel.getUseTheSkyAutosave()) {
            this.useAutosaveButton.setSelected(true);
        } else {
            this.useLocalFolderButton.setSelected(true);
        }

        this.controlMountCheckbox.setSelected(this.dataModel.getControlMount());
        this.homeMountCheckbox.setSelected(this.dataModel.getHomeMount());
        this.trackingOffCheckbox.setSelected(this.dataModel.getTrackingOff());
        this.slewToLightCheckbox.setSelected(this.dataModel.getSlewToLight());
        this.parkWhenDoneCheckbox.setSelected(this.dataModel.getParkWhenDone());

        this.lightSourceAltField.setText(String.format("%.8f",this.dataModel.getLightSourceAlt()));
        this.lightSourceAzField.setText(String.format("%.8f",this.dataModel.getLightSourceAz()));

        this.ditherFlatsCheckbox.setSelected(this.dataModel.getDitherFlats());
        this.ditherRadiusField.setText(String.valueOf(this.dataModel.getDitherRadius()));
        this.ditherMaximumField.setText(String.valueOf(this.dataModel.getDitherMaximum()));

        //  Set up table model for the frames table
        this.frameTableModel = new FrameTableModel(this.dataModel);
        this.framesTable.setModel(frameTableModel);

        //  Set some appearance attributes for the frames table that can't be set in the JFormDesigner
        this.framesTable.setRowHeight(this.calcGoodFramesTableRowHeight());

        //  Set up table to accept edits on single clicks not double

        this.setUpFramesTableEditing();

        this.setLocalOrRemoteMessage();
        this.enableSlewControls();
        this.enableProceedButton();
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

    private void targetADUfieldActionPerformed() {
        String proposedValue = this.targetADUfield.getText().trim();
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
        this.recordTextFieldValidity(this.targetADUfield, valid);
        this.enableProceedButton();
    }

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
        if (this.fieldValidity.containsKey(fieldToCheck)) {
            return this.fieldValidity.get(fieldToCheck);
        } else {
            return true;
        }
    }

    /**
     * Enable the "Proceed" button only if there are no outstanding invalid fields
     */
    private void enableProceedButton() {
        this.proceedButton.setEnabled(this.allTextFieldsValid());
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
    private void makeDirty() {
        // todo makeDirty
    }

    /**
     * Store the "Use Filter Wheel" setting from the checkbox to the data model
     */
    private void useFilterWheelCheckboxActionPerformed() {
        this.dataModel.setUseFilterWheel(this.useFilterWheelCheckbox.isSelected());
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
        this.makeDirty();
    }

    /**
     * Store the "Use Local Folder" setting from the checkbox to the data model.
     * There is no separate setting for this - it is the inverse of the Use TheSkyX Autosave setting
     */
    private void useLocalFolderButtonActionPerformed() {
        this.dataModel.setUseTheSkyAutosave(!this.useLocalFolderButton.isSelected());
        this.makeDirty();
    }

    /**
     * Respond to button that asks us to query the TheSkyX server for the Autosave path
     */
    private void queryAutosaveButtonActionPerformed() {
        // TODO queryAutosaveButtonActionPerformed
        System.out.println("queryAutosaveButtonActionPerformed");
    }

    /**
     * Respond to button that asks us to select a file folder local to this computer
     */
    private void setLocalFolderButtonActionPerformed() {
        // TODO setLocalFolderButtonActionPerformed
        System.out.println("setLocalFolderButtonActionPerformed");
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
     * other controls are handled, prevents manual adjustement.
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
        try {
            this.slewScopeToLightSource();
        } catch (InterruptedException e) {
            // Slew was interrupted - nothing we need to do
        }

    }

    /**
     * Spawn a sub-thread to slew the scope
     * @throws InterruptedException
     */
    private void slewScopeToLightSource() throws InterruptedException {

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
                (long) Common.SLEWING_FEEDBACK_INTERVAL_MILLISECONDS,
                (long) Common.SLEWING_FEEDBACK_INTERVAL_MILLISECONDS);

    }

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
     * Respond to button to turn on all filter/binning combinations in the table
     */
    private void allOnButtonActionPerformed() {
        // TODO allOnButtonActionPerformed
        System.out.println("allOnButtonActionPerformed");
    }

    /**
     * Respond to button asking to set table to default filter/binning combinations
     */
    private void defaultsButtonActionPerformed() {
        // TODO defaultsButtonActionPerformed
        System.out.println("defaultsButtonActionPerformed");
    }

    /**
     * Respond to button asking to turn off all filter/binning combinations in the table
     */
    private void allOffButtonActionPerformed() {
        // TODO allOffButtonActionPerformed
        System.out.println("allOffButtonActionPerformed");
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
     * Respond to Proceed button to start the acquisition session
     */
    private void proceedButtonActionPerformed() {
        // TODO proceedButtonActionPerformed
        System.out.println("proceedButtonActionPerformed");
    }

    private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        prefsMenuItem = new JMenuItem();
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
        scrollPane1 = new JScrollPane();
        framesTable = new JTable();
        buttonPanel = new JPanel();
        allOnButton = new JButton();
        defaultsButton = new JButton();
        allOffButton = new JButton();
        hSpacer2 = new JPanel(null);
        proceedButton = new JButton();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(new GridLayout());

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("File");

                //---- prefsMenuItem ----
                prefsMenuItem.setText("Preferences");
                prefsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                prefsMenuItem.addActionListener(e -> prefsMenuItemActionPerformed());
                menu1.add(prefsMenuItem);
            }
            menuBar1.add(menu1);
        }
        setJMenuBar(menuBar1);

        //======== contentPanel ========
        {
            contentPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
            ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- label21 ----
            label21.setText("Flat Capture Now");
            label21.setFont(new Font(".SF NS Text", Font.PLAIN, 24));
            label21.setHorizontalAlignment(SwingConstants.CENTER);
            contentPanel.add(label21, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
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
                label9.setText(" WIthin:");
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
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
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
                useFilterWheelCheckbox.setText("Use FIlter Wheel");
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
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
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
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(4, 4, 9, 9), 0, 0));

            //======== tablePanel ========
            {
                tablePanel.setBorder(LineBorder.createBlackLineBorder());
                tablePanel.setLayout(new GridBagLayout());
                ((GridBagLayout)tablePanel.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)tablePanel.getLayout()).rowHeights = new int[] {0, 0};
                ((GridBagLayout)tablePanel.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
                ((GridBagLayout)tablePanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                //======== scrollPane1 ========
                {

                    //---- framesTable ----
                    framesTable.setRowSelectionAllowed(false);
                    framesTable.setToolTipText("Number of flat frames to be acquired for each filter/binning combination. Click one to change it.");
                    framesTable.setGridColor(new Color(100, 100, 100));
                    framesTable.setIntercellSpacing(new Dimension(1, 10));
                    framesTable.setCellSelectionEnabled(true);
                    framesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    framesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                    framesTable.setRowHeight(24);
                    scrollPane1.setViewportView(framesTable);
                }
                tablePanel.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            contentPanel.add(tablePanel, new GridBagConstraints(0, 6, 3, 1, 0.0, 1.0,
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
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
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
            useAutosaveButton, BeanProperty.create("selected"),
            queryAutosaveButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            useLocalFolderButton, BeanProperty.create("selected"),
            setLocalFolderButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            ditherFlatsCheckbox, BeanProperty.create("selected"),
            ditherMaximumField, BeanProperty.create("enabled")));
        bindingGroup.bind();
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem prefsMenuItem;
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
    private JScrollPane scrollPane1;
    private JTable framesTable;
    private JPanel buttonPanel;
    private JButton allOnButton;
    private JButton defaultsButton;
    private JButton allOffButton;
    private JPanel hSpacer2;
    private JButton proceedButton;
    private BindingGroup bindingGroup;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}

//  todo Bug: cmd-A selcting bottom-right cell in table