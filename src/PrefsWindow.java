import java.awt.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import javax.swing.border.*;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
/*
 * Created by JFormDesigner on Tue Feb 25 13:25:52 EST 2020
 */



/**
 * @author Richard McDonald
 */
public class PrefsWindow extends JDialog {
	public PrefsWindow(Window owner) {
		super(owner);
		initComponents();
	}

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
		Container contentPane = getContentPane();
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

					//---- aduToleranceField ----
					aduToleranceField.setToolTipText("How close to the target, as a %, do we need to get.");

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

					//---- useSlot2Checkbox ----
					useSlot2Checkbox.setText("Use");
					useSlot2Checkbox.setToolTipText("Slot 2 is in use.");

					//---- useSlot3Checkbox ----
					useSlot3Checkbox.setText("Use");
					useSlot3Checkbox.setToolTipText("Slot 3 is in use.");

					//---- useSlot4Checkbox ----
					useSlot4Checkbox.setText("Use");
					useSlot4Checkbox.setToolTipText("Slot 4 is in use.");

					//---- useSlot5Checkbox ----
					useSlot5Checkbox.setText("Use");
					useSlot5Checkbox.setToolTipText("Slot 5 is in use.");

					//---- useSlot6Checkbox ----
					useSlot6Checkbox.setText("Use");
					useSlot6Checkbox.setToolTipText("Slot 6 is in use.");

					//---- useSlot7Checkbox ----
					useSlot7Checkbox.setText("Use");
					useSlot7Checkbox.setToolTipText("Slot 7 is in use.");

					//---- useSlot8Checkbox ----
					useSlot8Checkbox.setText("Use");
					useSlot8Checkbox.setToolTipText("Slot 8 is in use.");

					//---- label16 ----
					label16.setText("Use?");
					label16.setFont(label16.getFont().deriveFont(label16.getFont().getStyle() | Font.BOLD));

					//---- filter1NameField ----
					filter1NameField.setToolTipText("Name for this filter (letters only).");

					//---- filter2NameField ----
					filter2NameField.setToolTipText("Name for this filter (letters only).");

					//---- filter3NameField ----
					filter3NameField.setToolTipText("Name for this filter (letters only).");

					//---- filter4NameField ----
					filter4NameField.setToolTipText("Name for this filter (letters only).");

					//---- filter5NameField ----
					filter5NameField.setToolTipText("Name for this filter (letters only).");

					//---- filter6NameField ----
					filter6NameField.setToolTipText("Name for this filter (letters only).");

					//---- filter7NameField ----
					filter7NameField.setToolTipText("Name for this filter (letters only).");

					//---- filter8NameField ----
					filter8NameField.setToolTipText("Name for this filter (letters only).");

					//---- label17 ----
					label17.setText("Name");
					label17.setFont(label17.getFont().deriveFont(label17.getFont().getStyle() | Font.BOLD));

					//---- filterWheelCheckbox ----
					filterWheelCheckbox.setText("Use Filter Wheel");
					filterWheelCheckbox.setToolTipText("Use the camera's filter wheel, with the following filters.");

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
													.addComponent(filter1NameField)))
											.addGroup(filtersPanelLayout.createSequentialGroup()
												.addGap(61, 61, 61)
												.addComponent(filter2NameField))
											.addGroup(filtersPanelLayout.createSequentialGroup()
												.addComponent(useSlot2Checkbox)
												.addGap(0, 0, Short.MAX_VALUE))
											.addGroup(filtersPanelLayout.createSequentialGroup()
												.addComponent(useSlot3Checkbox)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(filter3NameField))
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
												.addComponent(filter8NameField))))
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

					//---- portNumberField ----
					portNumberField.setToolTipText("Port number where TheSkyX is listening.");

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

					//---- bin1AvailableButton ----
					bin1AvailableButton.setText("Available");
					bin1AvailableButton.setToolTipText("1 x 1 binning is included in plans, with default zero frames.");

					//---- bin1OffButton ----
					bin1OffButton.setText("Off");
					bin1OffButton.setToolTipText("1 x 1 binning will not be included in plans.");

					//---- bin2DefaultButton ----
					bin2DefaultButton.setText("Default");
					bin2DefaultButton.setToolTipText("2 x 2 binning is included in plans with default number of frames.");

					//---- bin2AvailableButton ----
					bin2AvailableButton.setText("Available");
					bin2AvailableButton.setToolTipText("2 x 3 binning is included in plans, with default zero frames.");

					//---- bin2OffButton ----
					bin2OffButton.setText("Off");
					bin2OffButton.setToolTipText("2 x 2 binning will not be included in plans.");

					//---- bin3DefaultButton ----
					bin3DefaultButton.setText("Default");
					bin3DefaultButton.setToolTipText("3 x 3 binning is included in plans with default number of frames.");

					//---- bin3AvailableButton ----
					bin3AvailableButton.setText("Available");
					bin3AvailableButton.setToolTipText("3 x 3 binning is included in plans, with default zero frames.");

					//---- bin3OffButton ----
					bin3OffButton.setText("Off");
					bin3OffButton.setToolTipText("3 x 3 binning will not be included in plans.");

					//---- bin4DefaultButton ----
					bin4DefaultButton.setText("Default");
					bin4DefaultButton.setToolTipText("4 x 4 binning is included in plans with default number of frames.");

					//---- bin4AvailableButton ----
					bin4AvailableButton.setText("Available");
					bin4AvailableButton.setToolTipText("4 x 4 binning is included in plans, with default zero frames.");

					//---- bin4OffButton ----
					bin4OffButton.setText("Off");
					bin4OffButton.setToolTipText("4 x 4 binning will not be included in plans.");

					GroupLayout binningPanelLayout = new GroupLayout(binningPanel);
					binningPanel.setLayout(binningPanelLayout);
					binningPanelLayout.setHorizontalGroup(
						binningPanelLayout.createParallelGroup()
							.addGroup(binningPanelLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(binningPanelLayout.createParallelGroup()
									.addGroup(binningPanelLayout.createSequentialGroup()
										.addComponent(label22)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(bin1DefaultButton)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(bin1AvailableButton)
										.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(bin1OffButton))
									.addGroup(binningPanelLayout.createSequentialGroup()
										.addComponent(label25)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(bin4DefaultButton)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(bin4AvailableButton)
										.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(bin4OffButton))
									.addGroup(binningPanelLayout.createSequentialGroup()
										.addComponent(label23)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(bin2DefaultButton)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(bin2AvailableButton)
										.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(bin2OffButton))
									.addGroup(binningPanelLayout.createSequentialGroup()
										.addComponent(label24)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(bin3DefaultButton)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(bin3AvailableButton)
										.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(bin3OffButton))
									.addComponent(label21))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
									.addComponent(bin2DefaultButton)
									.addComponent(bin2AvailableButton)
									.addComponent(bin2OffButton))
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

					//---- readScopeButton ----
					readScopeButton.setText("Read Scope");

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
										.addComponent(readScopeButton)))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
									.addComponent(lightLocationAzField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addContainerGap())
					);
				}

				//======== panel4 ========
				{
					panel4.setBorder(LineBorder.createBlackLineBorder());

					//---- ditherFlatsCheckbox ----
					ditherFlatsCheckbox.setText("Dither Flats");
					ditherFlatsCheckbox.setToolTipText("Dither flats (move scope a small amount between frames).");

					//---- label29 ----
					label29.setText("Radius");

					//---- label30 ----
					label30.setText("Max Radius");

					//---- ditherMaximumField ----
					ditherMaximumField.setToolTipText("Maximum distance dithering will move from light source.");

					//---- ditherRadiusField ----
					ditherRadiusField.setToolTipText("Size of dithering steps from centre of light source.");

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

				//---- resetTimesButton ----
				resetTimesButton.setText("Reset Time Estimates");
				resetTimesButton.setToolTipText("Reset the stored time estimates for every filter/dither combination.");

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
		setSize(650, 605);
		setLocationRelativeTo(getOwner());

		//---- bin1Group ----
		ButtonGroup bin1Group = new ButtonGroup();
		bin1Group.add(bin1DefaultButton);
		bin1Group.add(bin1AvailableButton);
		bin1Group.add(bin1OffButton);

		//---- bin2Group ----
		ButtonGroup bin2Group = new ButtonGroup();
		bin2Group.add(bin2DefaultButton);
		bin2Group.add(bin2AvailableButton);
		bin2Group.add(bin2OffButton);

		//---- bin3Group ----
		ButtonGroup bin3Group = new ButtonGroup();
		bin3Group.add(bin3DefaultButton);
		bin3Group.add(bin3AvailableButton);
		bin3Group.add(bin3OffButton);

		//---- bin4Group ----
		ButtonGroup bin4Group = new ButtonGroup();
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
			filter1NameField, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			useSlot2Checkbox, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			useSlot3Checkbox, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			filter3NameField, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			useSlot4Checkbox, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			filter4NameField, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			useSlot5Checkbox, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			filter5NameField, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			useSlot6Checkbox, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			filter6NameField, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			useSlot7Checkbox, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			filter7NameField, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			useSlot8Checkbox, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			filter8NameField, BeanProperty.create("enabled")));
		bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
			filterWheelCheckbox, BeanProperty.create("selected"),
			filter2NameField, BeanProperty.create("enabled")));
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
