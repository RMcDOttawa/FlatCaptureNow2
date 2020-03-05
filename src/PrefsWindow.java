import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import javax.swing.border.*;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.apache.commons.lang3.tuple.ImmutablePair;
/*
 * Created by JFormDesigner on Tue Feb 25 13:25:52 EST 2020
 */


/**
 * UI window controller for editing applicaton Preferences
 * @author Richard McDonald
 */
public class PrefsWindow extends JDialog {
	private AppPreferences preferences;

	//	Array to map binning and availability to a radio button in the interface
	//	First index is the binning value, second index is the numeric code of the availability
	private JRadioButton[][] binningAvailabilityToButton = null;

	//	Array to map filter slot numbers to the corresponding "use" and "name" fields
	private JCheckBox[] filterSlotToUseCheckbox = null;
	private JTextField[] filterSlotToNameField = null;

	//  Map to record valid/invalid state of validated fields in the UI
    //  Fields not in the map are considered valid.  Fields in the map are valid or not
    //  depending on the boolean stored.
    private HashMap<JTextField, Boolean> fieldValidity = null;

	/**
	 * Create an initialize window controller for displaying and changing preferences
	 * @param owner		Parent window owning this modal dialog
	 */
	public PrefsWindow(Window owner) {
		super(owner);
		initComponents();
	}

    /**
     * Set up the fields in the user interface from the given Preferences object
     * @param preferences       App preferences to be displayed and edited
     */
    public void setUpUI(AppPreferences preferences) {

        this.preferences = preferences;
        fieldValidity = new HashMap<JTextField, Boolean>();

        // Map binning number and availability to the radio button that represents that setting
        this.binningAvailabilityToButton = new JRadioButton[][]{
                {this.bin1OffButton, this.bin1AvailableButton, this.bin1DefaultButton},
                {this.bin2OffButton, this.bin2AvailableButton, this.bin2DefaultButton},
                {this.bin3OffButton, this.bin3AvailableButton, this.bin3DefaultButton},
                {this.bin4OffButton, this.bin4AvailableButton, this.bin4DefaultButton}};

        this.filterSlotToUseCheckbox = new JCheckBox[] {this.useSlot1Checkbox,
                this.useSlot2Checkbox, this.useSlot3Checkbox,
                this.useSlot4Checkbox, this.useSlot5Checkbox,
                this.useSlot6Checkbox, this.useSlot7Checkbox,
                this.useSlot8Checkbox};

        this.filterSlotToNameField = new JTextField[] {this.filter1NameField,
                this.filter2NameField, this.filter3NameField,
                this.filter4NameField, this.filter5NameField,
                this.filter6NameField, this.filter7NameField,
                this.filter8NameField};

        // Initialize the simple fields in the UI
        this.numFlatsField.setText(String.valueOf(preferences.getDefaultFrameCount()));
        this.targetADUfield.setText(String.valueOf(preferences.getTargetADUs()));
        this.aduToleranceField.setText(String.valueOf(preferences.getAduTolerance() * 100.0));
        this.serverAddressField.setText(preferences.getServerAddress());
        this.portNumberField.setText(String.valueOf(preferences.getPortNumber()));
        this.lightLocationAltField.setText(String.valueOf(preferences.getLightSourceAlt()));
        this.lightLocationAzField.setText(String.valueOf(preferences.getLightSourceAz()));
        this.filterWheelCheckbox.setSelected(preferences.getUseFilterWheel());
        this.ditherFlatsCheckbox.setSelected(preferences.getDitherFlats());
        this.ditherRadiusField.setText(String.valueOf(preferences.getDitherRadius()));
        this.ditherMaximumField.setText(String.valueOf(preferences.getMaximumDither()));

        this.populateBinningList();
        this.populateFilterList();
        this.enforceFilterNameUniqueness();
        this.enableCloseButton();
    }

    /**
     * Enforce filter name uniqueness.
     * Filters that are in use must have unique names.  Every time a filter name or "use" checkbox changes
     * we'll check all the "in use" filters for this, and flag as invalid any name fields that are duplicates
     */
    private void enforceFilterNameUniqueness() {
        List<Integer> slotNumbers = this.preferences.getFilterSlotNumbers();
        int numSlotNumbers = slotNumbers.size();
        //  Run down the slot numbers list.  At each position, count the total
        //  number of "in use" filters with that name.  If > 1, set error
        for (int slotIndex = 0; slotIndex < numSlotNumbers; slotIndex++) {
            int thisSlotNumber = slotNumbers.get(slotIndex);
            String thisName = this.preferences.getFilterName(thisSlotNumber);
            int namesInUse = this.countFiltersInUseNamed(thisName);
            this.recordTextFieldValidity(this.filterSlotToNameField[slotIndex], namesInUse <= 1);
        }
    }

    /**
     * Count how many filters are In Use and have the given name (case insensitive)
     * @param filterName        Name to check for
     * @return (int)            Number found (must be at least 1)
     */
    private int countFiltersInUseNamed(String filterName) {
        List<Integer> slotNumbers = this.preferences.getFilterSlotNumbers();
        int totalNamesFound = 0;
        for (int slotNumber : slotNumbers) {
            if (this.preferences.getFilterUse(slotNumber)) {
                String thisName = this.preferences.getFilterName(slotNumber);
//                System.out.println("  Slot " + slotNumber + " named " + thisName);
                if (filterName.equalsIgnoreCase(thisName)) {
                    totalNamesFound++;
//                    System.out.println("      Match, count incremented to " + totalNamesFound);
                }
            }
        }
        return totalNamesFound;
    }

    /**
     * For each of the binning values in use, set the appropriate radio button for whether that
     * value is used by default, available but not default, or not available.
     */
    private void populateBinningList() {
        List<Integer> binningNumbers = this.preferences.getBinningNumbers();
        for (int binning : binningNumbers) {
            BinningAvailability thisBinAvailability = this.preferences.getBinningAvailability(binning);
            JRadioButton[] tableRow = this.binningAvailabilityToButton[binning - 1];
            JRadioButton thisButton = tableRow[thisBinAvailability.codeNumber()];
            thisButton.setSelected(true);
        }
    }

    /**
     * For each filter stored in preferences, populate the "use" and "name" fields in the UI
     */
    private void populateFilterList() {
        List<Integer> filterSlots = this.preferences.getFilterSlotNumbers();
        for (int slotNumber : filterSlots) {
            this.filterSlotToUseCheckbox[slotNumber - 1].setSelected(this.preferences.getFilterUse(slotNumber));
            this.filterSlotToNameField[slotNumber - 1].setText(this.preferences.getFilterName(slotNumber));
        }
    }

    /**
     * Validate and store the field giving the default number of flat frames for each filter combination
     */
    private void numFlatsFieldActionPerformed() {
        String proposedValue = this.numFlatsField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(proposedValue, 1, 32767);
            valid = validation.left;
            if (valid) {
                this.preferences.setDefaultFrameCount(validation.right);
            }
        }
        this.recordTextFieldValidity(this.numFlatsField, valid);
        this.enableCloseButton();
    }

    /**
     * the CLose button is enabled only if there are no invalid text fields remaining to be corrected
     */
    private void enableCloseButton() {
        this.closeButton.setEnabled(this.allTextFieldsValid());
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
     * Losing focus on Number of Flats field triggers the field's action
     */
    private void numFlatsFieldFocusLost() {
        this.numFlatsFieldActionPerformed();
    }

    /**
     * "Use Filter Wheel" checkbox changed.  Record the preference.
     */
    private void filterWheelCheckboxActionPerformed() {
        this.preferences.setUseFilterWheel(this.filterWheelCheckbox.isSelected());
    }

    /**
     * Checkbox for Filter 1 in use has been clicked.  Record the setting.
     */
    private void useSlot1CheckboxActionPerformed() {
        this.preferences.setFilterUse(1, this.useSlot1Checkbox.isSelected());
        this.enforceFilterNameUniqueness();
        this.enableCloseButton();
    }

    /**
     * Checkbox for Filter 2 in use has been clicked.  Record the setting.
     */
    private void useSlot2CheckboxActionPerformed() {
        this.preferences.setFilterUse(2, this.useSlot2Checkbox.isSelected());
        this.enforceFilterNameUniqueness();
        this.enableCloseButton();
    }

    /**
     * Checkbox for Filter 3 in use has been clicked.  Record the setting.
     */
    private void useSlot3CheckboxActionPerformed() {
        this.preferences.setFilterUse(3, this.useSlot3Checkbox.isSelected());
        this.enforceFilterNameUniqueness();
        this.enableCloseButton();
    }

    /**
     * Checkbox for Filter 4 in use has been clicked.  Record the setting.
     */
    private void useSlot4CheckboxActionPerformed() {
        this.preferences.setFilterUse(4, this.useSlot4Checkbox.isSelected());
        this.enforceFilterNameUniqueness();
        this.enableCloseButton();
    }

    /**
     * Checkbox for Filter 5 in use has been clicked.  Record the setting.
     */
    private void useSlot5CheckboxActionPerformed() {
        this.preferences.setFilterUse(5, this.useSlot5Checkbox.isSelected());
        this.enforceFilterNameUniqueness();
        this.enableCloseButton();
    }

    /**
     * Checkbox for Filter 6 in use has been clicked.  Record the setting.
     */
    private void useSlot6CheckboxActionPerformed() {
        this.preferences.setFilterUse(6, this.useSlot6Checkbox.isSelected());
        this.enforceFilterNameUniqueness();
        this.enableCloseButton();
    }

    /**
     * Checkbox for Filter 7 in use has been clicked.  Record the setting.
     */
    private void useSlot7CheckboxActionPerformed() {
        this.preferences.setFilterUse(7, this.useSlot7Checkbox.isSelected());
        this.enforceFilterNameUniqueness();
    }

    /**
     * Checkbox for Filter 8 in use has been clicked.  Record the setting.
     */
    private void useSlot8CheckboxActionPerformed() {
        this.preferences.setFilterUse(8, this.useSlot8Checkbox.isSelected());
        this.enforceFilterNameUniqueness();
        this.enableCloseButton();
    }

    /**
     * user has changed the name field for the filter in slot 1
     * Validate it and store it
     */
    private void filter1NameFieldActionPerformed() {
        String proposedFilterName = this.filter1NameField.getText().trim();
        boolean valid = this.validateFilterName(proposedFilterName);
        if (valid) {
            this.preferences.setFilterName(1, proposedFilterName);
        }
        this.recordTextFieldValidity(this.filter1NameField, valid);
        if (valid) {
            this.enforceFilterNameUniqueness();
        }
        this.enableCloseButton();
    }

    /**
     * Determine if proposed filter name is syntactically valid.  We don't check for uniqueness
     * here, just for syntax.  Starts with a letter, then letters and digits are allowed.
     * @param proposedFilterName        String to validate as filter name
     * @return (boolean)                String is a valid filter name
     */
    private boolean validateFilterName(String proposedFilterName) {
        String nameUpper = proposedFilterName.toUpperCase();
        String validationExpression = "^[A-Z][A-Z0-9]*$";
        return nameUpper.matches(validationExpression);
    }

    /**
     * Leaving focus on text field is treated like pressing "enter", causing action.
     */
    private void filter1NameFieldFocusLost() {
        this.filter1NameFieldActionPerformed();
    }

    /**
     * user has changed the name field for the filter in slot 2
     * Validate it and store it
     */
    private void filter2NameFieldActionPerformed() {
        String proposedFilterName = this.filter2NameField.getText().trim();
        boolean valid = this.validateFilterName(proposedFilterName);
        if (valid) {
            this.preferences.setFilterName(2, proposedFilterName);
        }
        this.recordTextFieldValidity(this.filter2NameField, valid);
        if (valid) {
            this.enforceFilterNameUniqueness();
        }
        this.enableCloseButton();
    }

    /**
     * Leaving focus on text field is treated like pressing "enter", causing action.
     */
    private void filter2NameFieldFocusLost() {
        this.filter2NameFieldActionPerformed();
    }

    /**
     * user has changed the name field for the filter in slot 3
     * Validate it and store it
     */
    private void filter3NameFieldActionPerformed() {
        String proposedFilterName = this.filter3NameField.getText().trim();
        boolean valid = this.validateFilterName(proposedFilterName);
        if (valid) {
            this.preferences.setFilterName(3, proposedFilterName);
        }
        this.recordTextFieldValidity(this.filter3NameField, valid);
        if (valid) {
            this.enforceFilterNameUniqueness();
        }
        this.enableCloseButton();
    }

    /**
     * Leaving focus on text field is treated like pressing "enter", causing action.
     */
    private void filter3NameFieldFocusLost() {
        this.filter3NameFieldActionPerformed();
    }

    /**
     * user has changed the name field for the filter in slot 4
     * Validate it and store it
     */
    private void filter4NameFieldActionPerformed() {
        String proposedFilterName = this.filter4NameField.getText().trim();
        boolean valid = this.validateFilterName(proposedFilterName);
        if (valid) {
            this.preferences.setFilterName(4, proposedFilterName);
        }
        this.recordTextFieldValidity(this.filter4NameField, valid);
        if (valid) {
            this.enforceFilterNameUniqueness();
        }
        this.enableCloseButton();
    }

    /**
     * Leaving focus on text field is treated like pressing "enter", causing action.
     */
    private void filter4NameFieldFocusLost() {
        this.filter4NameFieldActionPerformed();
    }

    /**
     * user has changed the name field for the filter in slot 5
     * Validate it and store it
     */
    private void filter5NameFieldActionPerformed() {
        String proposedFilterName = this.filter5NameField.getText().trim();
        boolean valid = this.validateFilterName(proposedFilterName);
        if (valid) {
            this.preferences.setFilterName(5, proposedFilterName);
        }
        this.recordTextFieldValidity(this.filter5NameField, valid);
        if (valid) {
            this.enforceFilterNameUniqueness();
        }
        this.enableCloseButton();
    }

    /**
     * Leaving focus on text field is treated like pressing "enter", causing action.
     */
    private void filter5NameFieldFocusLost() {
        this.filter5NameFieldActionPerformed();
    }

    /**
     * user has changed the name field for the filter in slot 6
     * Validate it and store it
     */
    private void filter6NameFieldActionPerformed() {
        String proposedFilterName = this.filter6NameField.getText().trim();
        boolean valid = this.validateFilterName(proposedFilterName);
        if (valid) {
            this.preferences.setFilterName(6, proposedFilterName);
        }
        this.recordTextFieldValidity(this.filter6NameField, valid);
        if (valid) {
            this.enforceFilterNameUniqueness();
        }
        this.enableCloseButton();
    }

    /**
     * Leaving focus on text field is treated like pressing "enter", causing action.
     */
    private void filter6NameFieldFocusLost() {
        this.filter6NameFieldActionPerformed();
    }

    /**
     * user has changed the name field for the filter in slot 7
     * Validate it and store it
     */
    private void filter7NameFieldActionPerformed() {
        String proposedFilterName = this.filter7NameField.getText().trim();
        boolean valid = this.validateFilterName(proposedFilterName);
        if (valid) {
            this.preferences.setFilterName(7, proposedFilterName);
        }
        this.recordTextFieldValidity(this.filter7NameField, valid);
        if (valid) {
            this.enforceFilterNameUniqueness();
        }
        this.enableCloseButton();
    }

    /**
     * Leaving focus on text field is treated like pressing "enter", causing action.
     */
    private void filter7NameFieldFocusLost() {
        this.filter7NameFieldActionPerformed();
    }

    /**
     * user has changed the name field for the filter in slot 8
     * Validate it and store it
     */
    private void filter8NameFieldActionPerformed() {
        String proposedFilterName = this.filter8NameField.getText().trim();
        boolean valid = this.validateFilterName(proposedFilterName);
        if (valid) {
            this.preferences.setFilterName(8, proposedFilterName);
        }
        this.recordTextFieldValidity(this.filter8NameField, valid);
        if (valid) {
            this.enforceFilterNameUniqueness();
        }
        this.enableCloseButton();
    }

    /**
     * Leaving focus on text field is treated like pressing "enter", causing action.
     */
    private void filter8NameFieldFocusLost() {
        this.filter8NameFieldActionPerformed();
    }

    /**
     * Validate and store Altitude of flat light source
      */
    private void lightLocationAltFieldActionPerformed() {
        String proposedValue = this.lightLocationAltField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, -90.0, +90.0);
            valid = validation.left;
            if (valid) {
                this.preferences.setLightSourceAlt(validation.right);
            }
        }
        this.recordTextFieldValidity(this.lightLocationAltField, valid);
        this.enableCloseButton();
    }

    /**
     * Losing focus on field triggers its action
     */
    private void lightLocationAltFieldFocusLost() {
        this.lightLocationAltFieldActionPerformed();
    }

    /**
     * Validate and store Azimuth of flat light source
     */
    private void lightLocationAzFieldActionPerformed() {
        String proposedValue = this.lightLocationAzField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, -360.0, +360.0);
            valid = validation.left;
            if (valid) {
                this.preferences.setLightSourceAz(validation.right);
            }
        }
        this.recordTextFieldValidity(this.lightLocationAzField, valid);
        this.enableCloseButton();
    }

    /**
     * Losing focus on field triggers its action
     */
    private void lightLocationAzFieldFocusLost() {
        this.lightLocationAzFieldActionPerformed();
    }

    /**
     * User clicked the "Read Scope" button.  Read the current mount pointing location
     * and remember it as the Alt/Az of the flat light source.
      */
    private void readScopeButtonActionPerformed() {
        TheSkyXServer server = null;
        try {
            server = new TheSkyXServer(this.preferences.getServerAddress(),
                    this.preferences.getPortNumber());
            ImmutablePair<Double,Double> serverResponse = server.getScopeAltAz();
            double altitude = serverResponse.left;
            double azimuth = serverResponse.right;

            this.preferences.setLightSourceAlt(altitude);
            this.preferences.setLightSourceAz(azimuth);

            this.lightLocationAltField.setText(String.format("%.8f", altitude));
            this.lightLocationAzField.setText(String.format("%.8f",azimuth));

            this.readScopeMessage.setText("Read scope OK");
        } catch (IOException e) {
            this.readScopeMessage.setText("I/O Error");
        } catch (NumberFormatException e) {
            this.readScopeMessage.setText("Bad Server Response");
        }

    }

    /**
     * Validate and store the "Target ADUs" field
     */
    private void targetADUfieldActionPerformed() {
        String proposedValue = this.targetADUfield.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(proposedValue, 1, Integer.MAX_VALUE);
            valid = validation.left;
            if (valid) {
                this.preferences.setTargetADUs(validation.right);
            }
        }
        this.recordTextFieldValidity(this.targetADUfield, valid);
        this.enableCloseButton();
    }

    /**
     * Losing focus on field triggers its action
     */
    private void targetADUfieldFocusLost() {
        this.targetADUfieldActionPerformed();
    }

    /**
     * Validate and store the ADU Tolerance field.
     * Note we display it as a percentage - a number between 0 and 100; but we store it
     * as the equivalent fraction, between 0 and 1
     */
    private void aduToleranceFieldActionPerformed() {
        String proposedValue = this.aduToleranceField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, 0, 100);
            valid = validation.left;
            if (valid) {
                this.preferences.setAduTolerance(validation.right / 100.0);
            }
        }
        this.recordTextFieldValidity(this.aduToleranceField, valid);
        this.enableCloseButton();
    }

    /**
     * Losing focus on field triggers its action
     */
    private void aduToleranceFieldFocusLost() {
        this.aduToleranceFieldActionPerformed();
    }

    /**
     * Validate and store the "Server Address" field
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
            this.preferences.setServerAddress(proposedServerAddress);
        }
        this.recordTextFieldValidity(this.serverAddressField, valid);
        this.enableCloseButton();
    }

    /**
     * Losing focus on field triggers its action
     */
    private void serverAddressFieldFocusLost() {
        this.serverAddressFieldActionPerformed();
    }

    /**
     * Validate and store the "Server port number" field
     */
    private void portNumberFieldActionPerformed() {
        String proposedValue = this.portNumberField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(proposedValue, 0, 65535);
            valid = validation.left;
            if (valid) {
                this.preferences.setPortNumber(validation.right);
            }
        }
        this.recordTextFieldValidity(this.portNumberField, valid);
        this.enableCloseButton();
    }

    /**
     * Losing focus on field triggers its action
     */
    private void portNumberFieldFocusLost() {
        this.portNumberFieldActionPerformed();
    }

    /**
     * One of the radio buttons setting which binnings are used
      */
    private void bin1DefaultButtonActionPerformed() {
        this.preferences.setBinningAvailability(1, BinningAvailability.DEFAULT);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin1AvailableButtonActionPerformed() {
        this.preferences.setBinningAvailability(1, BinningAvailability.AVAILABLE);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin1OffButtonActionPerformed() {
        this.preferences.setBinningAvailability(1, BinningAvailability.OFF);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin2DefaultButtonActionPerformed() {
        this.preferences.setBinningAvailability(2, BinningAvailability.DEFAULT);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin2AvailableButtonActionPerformed() {
        this.preferences.setBinningAvailability(2, BinningAvailability.AVAILABLE);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin2OffButtonActionPerformed() {
        this.preferences.setBinningAvailability(2, BinningAvailability.OFF);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin3DefaultButtonActionPerformed() {
        this.preferences.setBinningAvailability(3, BinningAvailability.DEFAULT);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin3AvailableButtonActionPerformed() {
        this.preferences.setBinningAvailability(3, BinningAvailability.AVAILABLE);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin3OffButtonActionPerformed() {
        this.preferences.setBinningAvailability(3, BinningAvailability.OFF);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin4DefaultButtonActionPerformed() {
        this.preferences.setBinningAvailability(4, BinningAvailability.DEFAULT);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin4AvailableButtonActionPerformed() {
        this.preferences.setBinningAvailability(4, BinningAvailability.AVAILABLE);
    }

    /**
     * One of the radio buttons setting which binnings are used
     */
    private void bin4OffButtonActionPerformed() {
        this.preferences.setBinningAvailability(4, BinningAvailability.OFF);
    }

    /**
     * "Dither Flats" checkbox clicked.  Store the on/off setting.
     */
    private void ditherFlatsCheckboxActionPerformed() {
        this.preferences.setDitherFlats(this.ditherFlatsCheckbox.isSelected());
    }

    /**
     * Validate and store the "Dither Radius" field
     */
    private void ditherRadiusFieldActionPerformed() {
        String proposedValue = this.ditherRadiusField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, .001, 360.0*60*60);
            valid = validation.left;
            if (valid) {
                this.preferences.setDitherRadius(validation.right);
            }
        }
        this.recordTextFieldValidity(this.ditherRadiusField, valid);
        this.enableCloseButton();
    }

    /**
     * Losing focus on field triggers its action
     */
    private void ditherRadiusFieldFocusLost() {
        this.ditherRadiusFieldActionPerformed();
    }

    /**
     * Validate and store the "Maximum dither radius" field
      */
    private void ditherMaximumFieldActionPerformed() {
        String proposedValue = this.ditherMaximumField.getText().trim();
        boolean valid = false;
        if (proposedValue.length() > 0) {
            // Validate field value
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(proposedValue, .001, 360.0*60*60);
            valid = validation.left;
            if (valid) {
                this.preferences.setMaximumDither(validation.right);
            }
        }
        this.recordTextFieldValidity(this.ditherMaximumField, valid);
        this.enableCloseButton();
    }

    /**
     * Losing focus on field triggers its action
     */
    private void ditherMaximumFieldFocusLost() {
        this.ditherMaximumFieldActionPerformed();
    }

    /**
     * Reset Times button clicked.
     * We issue a "are you sure" dialog, then reset all the initial exposure time estimates
     * to a default of 10 seconds.  They'll be recalculated and refined the next time they're used.
      */
    private void resetTimesButtonActionPerformed() {
        int response = JOptionPane.showConfirmDialog(this,
                "This will reset ALL the remembered exposure time estimates.\n"
                + "The next runs will need to recalculate exposure times from scratch.\n"
                + "Are you sure you want to do this?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION);
        if (response == 0) {
            this.preferences.resetInitialExposures();
        }
    }

    /**
     * Close button has been clicked, so we close this dialog.
     * Preferences changes have been made in real time, so no need to save anything.
     */
    private void closeButtonActionPerformed() {
        this.setVisible(false);
    }

    /**
     * Component initializer geneated by JFormDesigner
     */

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        numFlatsField = new JTextField();
        label2 = new JLabel();
        aduPanel = new JPanel();
        label3 = new JLabel();
        label4 = new JLabel();
        targetADUfield = new JTextField();
        aduToleranceField = new JTextField();
        label5 = new JLabel();
        filtersPanel = new JPanel();
        label6 = new JLabel();
        label7 = new JLabel();
        label8 = new JLabel();
        label9 = new JLabel();
        label10 = new JLabel();
        label11 = new JLabel();
        label12 = new JLabel();
        label13 = new JLabel();
        label14 = new JLabel();
        label15 = new JLabel();
        useSlot1Checkbox = new JCheckBox();
        useSlot2Checkbox = new JCheckBox();
        useSlot3Checkbox = new JCheckBox();
        useSlot4Checkbox = new JCheckBox();
        useSlot5Checkbox = new JCheckBox();
        useSlot6Checkbox = new JCheckBox();
        useSlot7Checkbox = new JCheckBox();
        useSlot8Checkbox = new JCheckBox();
        label16 = new JLabel();
        filter1NameField = new JTextField();
        filter2NameField = new JTextField();
        filter3NameField = new JTextField();
        filter4NameField = new JTextField();
        filter5NameField = new JTextField();
        filter6NameField = new JTextField();
        filter7NameField = new JTextField();
        filter8NameField = new JTextField();
        label17 = new JLabel();
        filterWheelCheckbox = new JCheckBox();
        serverPanel = new JPanel();
        label18 = new JLabel();
        label19 = new JLabel();
        label20 = new JLabel();
        serverAddressField = new JTextField();
        portNumberField = new JTextField();
        binningPanel = new JPanel();
        label21 = new JLabel();
        label22 = new JLabel();
        label23 = new JLabel();
        label24 = new JLabel();
        label25 = new JLabel();
        bin1DefaultButton = new JRadioButton();
        bin1AvailableButton = new JRadioButton();
        bin1OffButton = new JRadioButton();
        bin2DefaultButton = new JRadioButton();
        bin2AvailableButton = new JRadioButton();
        bin2OffButton = new JRadioButton();
        bin3DefaultButton = new JRadioButton();
        bin3AvailableButton = new JRadioButton();
        bin3OffButton = new JRadioButton();
        bin4DefaultButton = new JRadioButton();
        bin4AvailableButton = new JRadioButton();
        bin4OffButton = new JRadioButton();
        lightSourcePanel = new JPanel();
        label26 = new JLabel();
        label27 = new JLabel();
        label28 = new JLabel();
        lightLocationAltField = new JTextField();
        lightLocationAzField = new JTextField();
        readScopeButton = new JButton();
        readScopeMessage = new JLabel();
        panel4 = new JPanel();
        ditherFlatsCheckbox = new JCheckBox();
        label29 = new JLabel();
        label30 = new JLabel();
        ditherMaximumField = new JTextField();
        ditherRadiusField = new JTextField();
        label31 = new JLabel();
        label32 = new JLabel();
        closeButton = new JButton();
        resetTimesButton = new JButton();
        label1 = new JLabel();

        //======== this ========
        setResizable(false);
        setModal(true);
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setMaximumSize(new Dimension(650, 605));
            dialogPane.setMinimumSize(new Dimension(650, 605));
            dialogPane.setPreferredSize(new Dimension(650, 605));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {

                //---- numFlatsField ----
                numFlatsField.setToolTipText("Default number of flat frames to take for each filter.");
                numFlatsField.addActionListener(e -> numFlatsFieldActionPerformed());
                numFlatsField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        numFlatsFieldFocusLost();
                    }
                });

                //---- label2 ----
                label2.setText("Flats per filter");

                //======== aduPanel ========
                {
                    aduPanel.setBorder(LineBorder.createBlackLineBorder());

                    //---- label3 ----
                    label3.setText("Target ADUs:");

                    //---- label4 ----
                    label4.setText("Tolerance:");

                    //---- targetADUfield ----
                    targetADUfield.setToolTipText("Average ADUs desired in flat frame.");
                    targetADUfield.addActionListener(e -> targetADUfieldActionPerformed());
                    targetADUfield.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            targetADUfieldFocusLost();
                        }
                    });

                    //---- aduToleranceField ----
                    aduToleranceField.setToolTipText("How close to the target, as a %, do we need to get.");
                    aduToleranceField.addActionListener(e -> aduToleranceFieldActionPerformed());
                    aduToleranceField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            aduToleranceFieldFocusLost();
                        }
                    });

                    //---- label5 ----
                    label5.setText("%");

                    GroupLayout aduPanelLayout = new GroupLayout(aduPanel);
                    aduPanel.setLayout(aduPanelLayout);
                    aduPanelLayout.setHorizontalGroup(
                        aduPanelLayout.createParallelGroup()
                            .addGroup(aduPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(aduPanelLayout.createParallelGroup()
                                    .addComponent(label3)
                                    .addComponent(label4))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(aduPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addComponent(targetADUfield, GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                    .addComponent(aduToleranceField, GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
                                .addGap(5, 5, 5)
                                .addComponent(label5)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
                    aduPanelLayout.setVerticalGroup(
                        aduPanelLayout.createParallelGroup()
                            .addGroup(aduPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(aduPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label3)
                                    .addComponent(targetADUfield, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(aduPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label4)
                                    .addComponent(label5)
                                    .addComponent(aduToleranceField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
                    );
                }

                //======== filtersPanel ========
                {
                    filtersPanel.setBorder(LineBorder.createBlackLineBorder());

                    //---- label6 ----
                    label6.setText("Filters");
                    label6.setFont(new Font("Lucida Grande", Font.BOLD, 14));

                    //---- label7 ----
                    label7.setText("Slot");
                    label7.setFont(new Font("Lucida Grande", Font.BOLD, 13));

                    //---- label8 ----
                    label8.setText("1");

                    //---- label9 ----
                    label9.setText("2");

                    //---- label10 ----
                    label10.setText("3");

                    //---- label11 ----
                    label11.setText("4");

                    //---- label12 ----
                    label12.setText("5");

                    //---- label13 ----
                    label13.setText("6");

                    //---- label14 ----
                    label14.setText("7");

                    //---- label15 ----
                    label15.setText("8");

                    //---- useSlot1Checkbox ----
                    useSlot1Checkbox.setText("Use");
                    useSlot1Checkbox.setToolTipText("Slot 1 is in use.");
                    useSlot1Checkbox.addActionListener(e -> useSlot1CheckboxActionPerformed());

                    //---- useSlot2Checkbox ----
                    useSlot2Checkbox.setText("Use");
                    useSlot2Checkbox.setToolTipText("Slot 2 is in use.");
                    useSlot2Checkbox.addActionListener(e -> useSlot2CheckboxActionPerformed());

                    //---- useSlot3Checkbox ----
                    useSlot3Checkbox.setText("Use");
                    useSlot3Checkbox.setToolTipText("Slot 3 is in use.");
                    useSlot3Checkbox.addActionListener(e -> useSlot3CheckboxActionPerformed());

                    //---- useSlot4Checkbox ----
                    useSlot4Checkbox.setText("Use");
                    useSlot4Checkbox.setToolTipText("Slot 4 is in use.");
                    useSlot4Checkbox.addActionListener(e -> useSlot4CheckboxActionPerformed());

                    //---- useSlot5Checkbox ----
                    useSlot5Checkbox.setText("Use");
                    useSlot5Checkbox.setToolTipText("Slot 5 is in use.");
                    useSlot5Checkbox.addActionListener(e -> useSlot5CheckboxActionPerformed());

                    //---- useSlot6Checkbox ----
                    useSlot6Checkbox.setText("Use");
                    useSlot6Checkbox.setToolTipText("Slot 6 is in use.");
                    useSlot6Checkbox.addActionListener(e -> useSlot6CheckboxActionPerformed());

                    //---- useSlot7Checkbox ----
                    useSlot7Checkbox.setText("Use");
                    useSlot7Checkbox.setToolTipText("Slot 7 is in use.");
                    useSlot7Checkbox.addActionListener(e -> useSlot7CheckboxActionPerformed());

                    //---- useSlot8Checkbox ----
                    useSlot8Checkbox.setText("Use");
                    useSlot8Checkbox.setToolTipText("Slot 8 is in use.");
                    useSlot8Checkbox.addActionListener(e -> useSlot8CheckboxActionPerformed());

                    //---- label16 ----
                    label16.setText("Use?");
                    label16.setFont(label16.getFont().deriveFont(label16.getFont().getStyle() | Font.BOLD));

                    //---- filter1NameField ----
                    filter1NameField.setToolTipText("Name for this filter (letters and digits only).");
                    filter1NameField.addActionListener(e -> filter1NameFieldActionPerformed());
                    filter1NameField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            filter1NameFieldFocusLost();
                        }
                    });

                    //---- filter2NameField ----
                    filter2NameField.setToolTipText("Name for this filter (letters and digits only).");
                    filter2NameField.addActionListener(e -> filter2NameFieldActionPerformed());
                    filter2NameField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            filter2NameFieldFocusLost();
                        }
                    });

                    //---- filter3NameField ----
                    filter3NameField.setToolTipText("Name for this filter (letters and digits only).");
                    filter3NameField.addActionListener(e -> filter3NameFieldActionPerformed());
                    filter3NameField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            filter3NameFieldFocusLost();
                        }
                    });

                    //---- filter4NameField ----
                    filter4NameField.setToolTipText("Name for this filter (letters and digits only).");
                    filter4NameField.addActionListener(e -> filter4NameFieldActionPerformed());
                    filter4NameField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            filter4NameFieldFocusLost();
                        }
                    });

                    //---- filter5NameField ----
                    filter5NameField.setToolTipText("Name for this filter (letters and digits only).");
                    filter5NameField.addActionListener(e -> filter5NameFieldActionPerformed());
                    filter5NameField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            filter5NameFieldFocusLost();
                        }
                    });

                    //---- filter6NameField ----
                    filter6NameField.setToolTipText("Name for this filter (letters and digits only).");
                    filter6NameField.addActionListener(e -> filter6NameFieldActionPerformed());
                    filter6NameField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            filter6NameFieldFocusLost();
                        }
                    });

                    //---- filter7NameField ----
                    filter7NameField.setToolTipText("Name for this filter (letters and digits only).");
                    filter7NameField.addActionListener(e -> filter7NameFieldActionPerformed());
                    filter7NameField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            filter7NameFieldFocusLost();
                        }
                    });

                    //---- filter8NameField ----
                    filter8NameField.setToolTipText("Name for this filter (letters and digits only).");
                    filter8NameField.addActionListener(e -> filter8NameFieldActionPerformed());
                    filter8NameField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            filter8NameFieldFocusLost();
                        }
                    });

                    //---- label17 ----
                    label17.setText("Name");
                    label17.setFont(label17.getFont().deriveFont(label17.getFont().getStyle() | Font.BOLD));

                    //---- filterWheelCheckbox ----
                    filterWheelCheckbox.setText("Use Filter Wheel");
                    filterWheelCheckbox.setToolTipText("Use the camera's filter wheel, with the following filters.");
                    filterWheelCheckbox.addActionListener(e -> filterWheelCheckboxActionPerformed());

                    GroupLayout filtersPanelLayout = new GroupLayout(filtersPanel);
                    filtersPanel.setLayout(filtersPanelLayout);
                    filtersPanelLayout.setHorizontalGroup(
                        filtersPanelLayout.createParallelGroup()
                            .addGroup(filtersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(filtersPanelLayout.createParallelGroup()
                                    .addGroup(filtersPanelLayout.createSequentialGroup()
                                        .addGroup(filtersPanelLayout.createParallelGroup()
                                            .addGroup(filtersPanelLayout.createParallelGroup()
                                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(label7, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(label8, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
                                                .addComponent(label9, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label10, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
                                            .addComponent(label11, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label12, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label13, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label14, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label15, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(filtersPanelLayout.createParallelGroup()
                                            .addGroup(filtersPanelLayout.createSequentialGroup()
                                                .addGroup(filtersPanelLayout.createParallelGroup()
                                                    .addComponent(useSlot1Checkbox)
                                                    .addGroup(filtersPanelLayout.createSequentialGroup()
                                                        .addGap(6, 6, 6)
                                                        .addComponent(label16)))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(filtersPanelLayout.createParallelGroup()
                                                    .addGroup(filtersPanelLayout.createSequentialGroup()
                                                        .addComponent(label17, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                                                        .addGap(0, 0, Short.MAX_VALUE))
                                                    .addComponent(filter1NameField, GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)))
                                            .addGroup(filtersPanelLayout.createSequentialGroup()
                                                .addComponent(useSlot4Checkbox)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(filter4NameField))
                                            .addGroup(filtersPanelLayout.createSequentialGroup()
                                                .addComponent(useSlot5Checkbox)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(filter5NameField))
                                            .addGroup(filtersPanelLayout.createSequentialGroup()
                                                .addComponent(useSlot6Checkbox)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(filter6NameField))
                                            .addGroup(filtersPanelLayout.createSequentialGroup()
                                                .addComponent(useSlot7Checkbox)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(filter7NameField))
                                            .addGroup(filtersPanelLayout.createSequentialGroup()
                                                .addComponent(useSlot8Checkbox)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(filter8NameField))
                                            .addGroup(filtersPanelLayout.createSequentialGroup()
                                                .addGroup(filtersPanelLayout.createParallelGroup()
                                                    .addComponent(useSlot3Checkbox)
                                                    .addComponent(useSlot2Checkbox))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(filtersPanelLayout.createParallelGroup()
                                                    .addComponent(filter2NameField, GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                                                    .addComponent(filter3NameField)))))
                                    .addGroup(filtersPanelLayout.createSequentialGroup()
                                        .addComponent(label6)
                                        .addGap(18, 18, 18)
                                        .addComponent(filterWheelCheckbox)))
                                .addContainerGap())
                    );
                    filtersPanelLayout.setVerticalGroup(
                        filtersPanelLayout.createParallelGroup()
                            .addGroup(filtersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(filterWheelCheckbox)
                                    .addComponent(label6))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersPanelLayout.createParallelGroup()
                                    .addComponent(label7)
                                    .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label16)
                                        .addComponent(label17)))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label8)
                                    .addComponent(useSlot1Checkbox)
                                    .addComponent(filter1NameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label9)
                                    .addComponent(useSlot2Checkbox)
                                    .addComponent(filter2NameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label10)
                                    .addComponent(useSlot3Checkbox)
                                    .addComponent(filter3NameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(useSlot4Checkbox)
                                    .addComponent(label11)
                                    .addComponent(filter4NameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(useSlot5Checkbox)
                                    .addComponent(label12)
                                    .addComponent(filter5NameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(useSlot6Checkbox)
                                    .addComponent(label13)
                                    .addComponent(filter6NameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(useSlot7Checkbox)
                                    .addComponent(label14)
                                    .addComponent(filter7NameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filtersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(useSlot8Checkbox)
                                    .addComponent(label15)
                                    .addComponent(filter8NameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
                    );
                }

                //======== serverPanel ========
                {
                    serverPanel.setBorder(LineBorder.createBlackLineBorder());

                    //---- label18 ----
                    label18.setText("TheSkyX Server");
                    label18.setFont(new Font("Lucida Grande", Font.BOLD, 14));

                    //---- label19 ----
                    label19.setText("Address:");

                    //---- label20 ----
                    label20.setText("Port:");

                    //---- serverAddressField ----
                    serverAddressField.setToolTipText("IP address or host name of server where TheSkyX is running.");
                    serverAddressField.addActionListener(e -> serverAddressFieldActionPerformed());
                    serverAddressField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            serverAddressFieldFocusLost();
                        }
                    });

                    //---- portNumberField ----
                    portNumberField.setToolTipText("Port number where TheSkyX is listening.");
                    portNumberField.addActionListener(e -> portNumberFieldActionPerformed());
                    portNumberField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            portNumberFieldFocusLost();
                        }
                    });

                    GroupLayout serverPanelLayout = new GroupLayout(serverPanel);
                    serverPanel.setLayout(serverPanelLayout);
                    serverPanelLayout.setHorizontalGroup(
                        serverPanelLayout.createParallelGroup()
                            .addGroup(serverPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(serverPanelLayout.createParallelGroup()
                                    .addGroup(serverPanelLayout.createSequentialGroup()
                                        .addGroup(serverPanelLayout.createParallelGroup()
                                            .addComponent(label19)
                                            .addComponent(label20))
                                        .addGap(10, 10, 10)
                                        .addGroup(serverPanelLayout.createParallelGroup()
                                            .addComponent(serverAddressField)
                                            .addComponent(portNumberField)))
                                    .addGroup(serverPanelLayout.createSequentialGroup()
                                        .addComponent(label18)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                    );
                    serverPanelLayout.setVerticalGroup(
                        serverPanelLayout.createParallelGroup()
                            .addGroup(serverPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(label18)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(serverPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label19)
                                    .addComponent(serverAddressField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(serverPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label20)
                                    .addComponent(portNumberField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
                    );
                }

                //======== binningPanel ========
                {
                    binningPanel.setBorder(LineBorder.createBlackLineBorder());

                    //---- label21 ----
                    label21.setText("Binning");
                    label21.setFont(new Font("Lucida Grande", Font.BOLD, 14));

                    //---- label22 ----
                    label22.setText("1 x 1");

                    //---- label23 ----
                    label23.setText("2 x 2");

                    //---- label24 ----
                    label24.setText("3 x 3");

                    //---- label25 ----
                    label25.setText("4 x 4");

                    //---- bin1DefaultButton ----
                    bin1DefaultButton.setText("Default");
                    bin1DefaultButton.setToolTipText("1 x 1 binning is included in plans with default number of frames.");
                    bin1DefaultButton.addActionListener(e -> bin1DefaultButtonActionPerformed());

                    //---- bin1AvailableButton ----
                    bin1AvailableButton.setText("Available");
                    bin1AvailableButton.setToolTipText("1 x 1 binning is included in plans, with default zero frames.");
                    bin1AvailableButton.addActionListener(e -> bin1AvailableButtonActionPerformed());

                    //---- bin1OffButton ----
                    bin1OffButton.setText("Off");
                    bin1OffButton.setToolTipText("1 x 1 binning will not be included in plans.");
                    bin1OffButton.addActionListener(e -> bin1OffButtonActionPerformed());

                    //---- bin2DefaultButton ----
                    bin2DefaultButton.setText("Default");
                    bin2DefaultButton.setToolTipText("2 x 2 binning is included in plans with default number of frames.");
                    bin2DefaultButton.addActionListener(e -> bin2DefaultButtonActionPerformed());

                    //---- bin2AvailableButton ----
                    bin2AvailableButton.setText("Available");
                    bin2AvailableButton.setToolTipText("2 x 3 binning is included in plans, with default zero frames.");
                    bin2AvailableButton.addActionListener(e -> bin2AvailableButtonActionPerformed());

                    //---- bin2OffButton ----
                    bin2OffButton.setText("Off");
                    bin2OffButton.setToolTipText("2 x 2 binning will not be included in plans.");
                    bin2OffButton.addActionListener(e -> bin2OffButtonActionPerformed());

                    //---- bin3DefaultButton ----
                    bin3DefaultButton.setText("Default");
                    bin3DefaultButton.setToolTipText("3 x 3 binning is included in plans with default number of frames.");
                    bin3DefaultButton.addActionListener(e -> bin3DefaultButtonActionPerformed());

                    //---- bin3AvailableButton ----
                    bin3AvailableButton.setText("Available");
                    bin3AvailableButton.setToolTipText("3 x 3 binning is included in plans, with default zero frames.");
                    bin3AvailableButton.addActionListener(e -> bin3AvailableButtonActionPerformed());

                    //---- bin3OffButton ----
                    bin3OffButton.setText("Off");
                    bin3OffButton.setToolTipText("3 x 3 binning will not be included in plans.");
                    bin3OffButton.addActionListener(e -> bin3OffButtonActionPerformed());

                    //---- bin4DefaultButton ----
                    bin4DefaultButton.setText("Default");
                    bin4DefaultButton.setToolTipText("4 x 4 binning is included in plans with default number of frames.");
                    bin4DefaultButton.addActionListener(e -> bin4DefaultButtonActionPerformed());

                    //---- bin4AvailableButton ----
                    bin4AvailableButton.setText("Available");
                    bin4AvailableButton.setToolTipText("4 x 4 binning is included in plans, with default zero frames.");
                    bin4AvailableButton.addActionListener(e -> bin4AvailableButtonActionPerformed());

                    //---- bin4OffButton ----
                    bin4OffButton.setText("Off");
                    bin4OffButton.setToolTipText("4 x 4 binning will not be included in plans.");
                    bin4OffButton.addActionListener(e -> bin4OffButtonActionPerformed());

                    GroupLayout binningPanelLayout = new GroupLayout(binningPanel);
                    binningPanel.setLayout(binningPanelLayout);
                    binningPanelLayout.setHorizontalGroup(
                        binningPanelLayout.createParallelGroup()
                            .addGroup(binningPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(binningPanelLayout.createParallelGroup()
                                    .addGroup(binningPanelLayout.createSequentialGroup()
                                        .addComponent(label25)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bin4DefaultButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bin4AvailableButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bin4OffButton))
                                    .addGroup(binningPanelLayout.createSequentialGroup()
                                        .addComponent(label24)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bin3DefaultButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bin3AvailableButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bin3OffButton))
                                    .addComponent(label21)
                                    .addGroup(binningPanelLayout.createSequentialGroup()
                                        .addGroup(binningPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(label22, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(label23, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(binningPanelLayout.createParallelGroup()
                                            .addGroup(binningPanelLayout.createSequentialGroup()
                                                .addComponent(bin2DefaultButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(bin2AvailableButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(bin2OffButton))
                                            .addGroup(binningPanelLayout.createSequentialGroup()
                                                .addComponent(bin1DefaultButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(bin1AvailableButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(bin1OffButton)))))
                                .addContainerGap(26, Short.MAX_VALUE))
                    );
                    binningPanelLayout.setVerticalGroup(
                        binningPanelLayout.createParallelGroup()
                            .addGroup(binningPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(label21)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(binningPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label22)
                                    .addComponent(bin1DefaultButton)
                                    .addComponent(bin1AvailableButton)
                                    .addComponent(bin1OffButton))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(binningPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label23)
                                    .addComponent(bin2AvailableButton)
                                    .addComponent(bin2OffButton)
                                    .addComponent(bin2DefaultButton))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(binningPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label24)
                                    .addComponent(bin3DefaultButton)
                                    .addComponent(bin3AvailableButton)
                                    .addComponent(bin3OffButton))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(binningPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label25)
                                    .addComponent(bin4DefaultButton)
                                    .addComponent(bin4AvailableButton)
                                    .addComponent(bin4OffButton))
                                .addContainerGap())
                    );
                }

                //======== lightSourcePanel ========
                {
                    lightSourcePanel.setBorder(LineBorder.createBlackLineBorder());

                    //---- label26 ----
                    label26.setText("Location of Flat Light Source");
                    label26.setFont(new Font("Lucida Grande", Font.BOLD, 14));

                    //---- label27 ----
                    label27.setText("Alt");

                    //---- label28 ----
                    label28.setText("Az");

                    //---- lightLocationAltField ----
                    lightLocationAltField.addActionListener(e -> lightLocationAltFieldActionPerformed());
                    lightLocationAltField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            lightLocationAltFieldFocusLost();
                        }
                    });

                    //---- lightLocationAzField ----
                    lightLocationAzField.addActionListener(e -> lightLocationAzFieldActionPerformed());
                    lightLocationAzField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            lightLocationAzFieldFocusLost();
                        }
                    });

                    //---- readScopeButton ----
                    readScopeButton.setText("Read Scope");
                    readScopeButton.addActionListener(e -> readScopeButtonActionPerformed());

                    //---- readScopeMessage ----
                    readScopeMessage.setText(" ");

                    GroupLayout lightSourcePanelLayout = new GroupLayout(lightSourcePanel);
                    lightSourcePanel.setLayout(lightSourcePanelLayout);
                    lightSourcePanelLayout.setHorizontalGroup(
                        lightSourcePanelLayout.createParallelGroup()
                            .addGroup(lightSourcePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(lightSourcePanelLayout.createParallelGroup()
                                    .addComponent(label26)
                                    .addGroup(lightSourcePanelLayout.createSequentialGroup()
                                        .addGroup(lightSourcePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addGroup(lightSourcePanelLayout.createSequentialGroup()
                                                .addComponent(label28)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lightLocationAzField, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
                                            .addGroup(GroupLayout.Alignment.LEADING, lightSourcePanelLayout.createSequentialGroup()
                                                .addComponent(label27)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lightLocationAltField, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(lightSourcePanelLayout.createParallelGroup()
                                            .addGroup(lightSourcePanelLayout.createSequentialGroup()
                                                .addGap(6, 6, 6)
                                                .addComponent(readScopeMessage, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                                            .addGroup(lightSourcePanelLayout.createSequentialGroup()
                                                .addComponent(readScopeButton)
                                                .addGap(0, 0, Short.MAX_VALUE)))))
                                .addContainerGap())
                    );
                    lightSourcePanelLayout.setVerticalGroup(
                        lightSourcePanelLayout.createParallelGroup()
                            .addGroup(lightSourcePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(label26)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(lightSourcePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label27)
                                    .addComponent(lightLocationAltField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(readScopeButton))
                                .addGap(9, 9, 9)
                                .addGroup(lightSourcePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label28)
                                    .addComponent(lightLocationAzField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(readScopeMessage))
                                .addContainerGap())
                    );
                }

                //======== panel4 ========
                {
                    panel4.setBorder(LineBorder.createBlackLineBorder());

                    //---- ditherFlatsCheckbox ----
                    ditherFlatsCheckbox.setText("Dither Flats");
                    ditherFlatsCheckbox.setToolTipText("Dither flats (move scope a small amount between frames).");
                    ditherFlatsCheckbox.addActionListener(e -> ditherFlatsCheckboxActionPerformed());

                    //---- label29 ----
                    label29.setText("Radius");

                    //---- label30 ----
                    label30.setText("Max Radius");

                    //---- ditherMaximumField ----
                    ditherMaximumField.setToolTipText("Maximum distance dithering will move from light source.");
                    ditherMaximumField.addActionListener(e -> ditherMaximumFieldActionPerformed());
                    ditherMaximumField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            ditherMaximumFieldFocusLost();
                        }
                    });

                    //---- ditherRadiusField ----
                    ditherRadiusField.setToolTipText("Size of dithering steps from centre of light source.");
                    ditherRadiusField.addActionListener(e -> ditherRadiusFieldActionPerformed());
                    ditherRadiusField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            ditherRadiusFieldFocusLost();
                        }
                    });

                    //---- label31 ----
                    label31.setText("arc secs");

                    //---- label32 ----
                    label32.setText("arc secs");

                    GroupLayout panel4Layout = new GroupLayout(panel4);
                    panel4.setLayout(panel4Layout);
                    panel4Layout.setHorizontalGroup(
                        panel4Layout.createParallelGroup()
                            .addGroup(panel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panel4Layout.createParallelGroup()
                                    .addGroup(panel4Layout.createSequentialGroup()
                                        .addGap(8, 8, 8)
                                        .addGroup(panel4Layout.createParallelGroup()
                                            .addComponent(label30)
                                            .addComponent(label29))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                            .addComponent(ditherMaximumField)
                                            .addComponent(ditherRadiusField, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(panel4Layout.createParallelGroup()
                                            .addComponent(label31)
                                            .addComponent(label32)))
                                    .addComponent(ditherFlatsCheckbox))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
                    panel4Layout.setVerticalGroup(
                        panel4Layout.createParallelGroup()
                            .addGroup(panel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(ditherFlatsCheckbox)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label29)
                                    .addComponent(ditherRadiusField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(label31))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label30)
                                    .addComponent(ditherMaximumField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(label32))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
                }

                //---- closeButton ----
                closeButton.setText("Close");
                closeButton.setToolTipText("Close this preferences window.");
                closeButton.addActionListener(e -> closeButtonActionPerformed());

                //---- resetTimesButton ----
                resetTimesButton.setText("Reset Time Estimates");
                resetTimesButton.setToolTipText("Reset the stored time estimates for every filter/dither combination.");
                resetTimesButton.addActionListener(e -> resetTimesButtonActionPerformed());

                GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
                contentPanel.setLayout(contentPanelLayout);
                contentPanelLayout.setHorizontalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(lightSourcePanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(GroupLayout.Alignment.LEADING, contentPanelLayout.createSequentialGroup()
                                            .addComponent(numFlatsField, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(label2)
                                            .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(filtersPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGap(18, 18, 18)
                                    .addGroup(contentPanelLayout.createParallelGroup()
                                        .addComponent(aduPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(serverPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(binningPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(panel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addComponent(resetTimesButton, GroupLayout.PREFERRED_SIZE, 178, GroupLayout.PREFERRED_SIZE)
                                    .addGap(361, 361, 361)
                                    .addComponent(closeButton)))
                            .addContainerGap())
                );
                contentPanelLayout.setVerticalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addGap(21, 21, 21)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addComponent(aduPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(serverPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(binningPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(numFlatsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label2))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(filtersPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addComponent(lightSourcePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(panel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(resetTimesButton)
                                .addComponent(closeButton))
                            .addGap(0, 0, 0))
                );
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //---- label1 ----
            label1.setText("<html>These are defaults, used the next time you run the program or create a session. They do not affect the <i>current</i> session or any saved file.");
            dialogPane.add(label1, BorderLayout.NORTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        setSize(650, 615);
        setLocationRelativeTo(getOwner());

        //---- bin1Group ----
        var bin1Group = new ButtonGroup();
        bin1Group.add(bin1DefaultButton);
        bin1Group.add(bin1AvailableButton);
        bin1Group.add(bin1OffButton);

        //---- bin2Group ----
        var bin2Group = new ButtonGroup();
        bin2Group.add(bin2DefaultButton);
        bin2Group.add(bin2AvailableButton);
        bin2Group.add(bin2OffButton);

        //---- bin3Group ----
        var bin3Group = new ButtonGroup();
        bin3Group.add(bin3DefaultButton);
        bin3Group.add(bin3AvailableButton);
        bin3Group.add(bin3OffButton);

        //---- bin4Group ----
        var bin4Group = new ButtonGroup();
        bin4Group.add(bin4DefaultButton);
        bin4Group.add(bin4AvailableButton);
        bin4Group.add(bin4OffButton);

        //---- bindings ----
        bindingGroup = new BindingGroup();
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            filterWheelCheckbox, BeanProperty.create("selected"),
            useSlot1Checkbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            filterWheelCheckbox, BeanProperty.create("selected"),
            useSlot2Checkbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            filterWheelCheckbox, BeanProperty.create("selected"),
            useSlot3Checkbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            useSlot3Checkbox, BeanProperty.create("selected"),
            filter3NameField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            filterWheelCheckbox, BeanProperty.create("selected"),
            useSlot4Checkbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            useSlot4Checkbox, BeanProperty.create("selected"),
            filter4NameField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            filterWheelCheckbox, BeanProperty.create("selected"),
            useSlot5Checkbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            useSlot5Checkbox, BeanProperty.create("selected"),
            filter5NameField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            filterWheelCheckbox, BeanProperty.create("selected"),
            useSlot6Checkbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            useSlot6Checkbox, BeanProperty.create("selected"),
            filter6NameField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            filterWheelCheckbox, BeanProperty.create("selected"),
            useSlot7Checkbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            useSlot7Checkbox, BeanProperty.create("selected"),
            filter7NameField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            filterWheelCheckbox, BeanProperty.create("selected"),
            useSlot8Checkbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            useSlot8Checkbox, BeanProperty.create("selected"),
            filter8NameField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            useSlot2Checkbox, BeanProperty.create("selected"),
            filter2NameField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            ditherFlatsCheckbox, BeanProperty.create("selected"),
            ditherRadiusField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            ditherFlatsCheckbox, BeanProperty.create("selected"),
            ditherMaximumField, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            useSlot1Checkbox, BeanProperty.create("selected"),
            filter1NameField, BeanProperty.create("enabled")));
        bindingGroup.bind();
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JTextField numFlatsField;
    private JLabel label2;
    private JPanel aduPanel;
    private JLabel label3;
    private JLabel label4;
    private JTextField targetADUfield;
    private JTextField aduToleranceField;
    private JLabel label5;
    private JPanel filtersPanel;
    private JLabel label6;
    private JLabel label7;
    private JLabel label8;
    private JLabel label9;
    private JLabel label10;
    private JLabel label11;
    private JLabel label12;
    private JLabel label13;
    private JLabel label14;
    private JLabel label15;
    private JCheckBox useSlot1Checkbox;
    private JCheckBox useSlot2Checkbox;
    private JCheckBox useSlot3Checkbox;
    private JCheckBox useSlot4Checkbox;
    private JCheckBox useSlot5Checkbox;
    private JCheckBox useSlot6Checkbox;
    private JCheckBox useSlot7Checkbox;
    private JCheckBox useSlot8Checkbox;
    private JLabel label16;
    private JTextField filter1NameField;
    private JTextField filter2NameField;
    private JTextField filter3NameField;
    private JTextField filter4NameField;
    private JTextField filter5NameField;
    private JTextField filter6NameField;
    private JTextField filter7NameField;
    private JTextField filter8NameField;
    private JLabel label17;
    private JCheckBox filterWheelCheckbox;
    private JPanel serverPanel;
    private JLabel label18;
    private JLabel label19;
    private JLabel label20;
    private JTextField serverAddressField;
    private JTextField portNumberField;
    private JPanel binningPanel;
    private JLabel label21;
    private JLabel label22;
    private JLabel label23;
    private JLabel label24;
    private JLabel label25;
    private JRadioButton bin1DefaultButton;
    private JRadioButton bin1AvailableButton;
    private JRadioButton bin1OffButton;
    private JRadioButton bin2DefaultButton;
    private JRadioButton bin2AvailableButton;
    private JRadioButton bin2OffButton;
    private JRadioButton bin3DefaultButton;
    private JRadioButton bin3AvailableButton;
    private JRadioButton bin3OffButton;
    private JRadioButton bin4DefaultButton;
    private JRadioButton bin4AvailableButton;
    private JRadioButton bin4OffButton;
    private JPanel lightSourcePanel;
    private JLabel label26;
    private JLabel label27;
    private JLabel label28;
    private JTextField lightLocationAltField;
    private JTextField lightLocationAzField;
    private JButton readScopeButton;
    private JLabel readScopeMessage;
    private JPanel panel4;
    private JCheckBox ditherFlatsCheckbox;
    private JLabel label29;
    private JLabel label30;
    private JTextField ditherMaximumField;
    private JTextField ditherRadiusField;
    private JLabel label31;
    private JLabel label32;
    private JButton closeButton;
    private JButton resetTimesButton;
    private JLabel label1;
    private BindingGroup bindingGroup;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

