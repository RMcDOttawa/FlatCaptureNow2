import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
/*
 * Created by JFormDesigner on Wed Feb 26 14:14:57 EST 2020
 */



/**
 * @author Richard McDonald
 */
public class Session extends JDialog {
    DefaultTableModel sessionTableModel = null;
    ArrayList<FlatSet> flatSetList;
	public Session(Window owner) {
		super(owner);
		initComponents();
	}

	/**
	 * Set up the user interface to be ready for acting as session monitor.
	 * 		- Set up a default table model for the frames table, that we'll use to show where we are
	 * @param dataModel
	 */
	public void setUpUI(DataModel dataModel) {
		//	todo setUpUI
		System.out.println("setUpUI");

		//  Set up table data model, using default model where we just manually add rows.
        this.setUpSessionTable(dataModel);

        //  Set up data model for the JList that is used for the console log
	}

    /**
     * Set up a DefaultTableModel containing a list of all the flat frames we'll be acquiring
     * One row per flat frame set, with columns:
     *     Number wanted
     *     Filter
     *     Binning
     *     Number done
     * @param dataModel     Data model containing the matrix of flats (filter x binning)
     */
    private void setUpSessionTable(DataModel dataModel) {
        // todo setUpSessionTable
        System.out.println("setUpSessionTable");

        //  Get the list of all the flat sets to be acquired
        this.flatSetList = dataModel.getFlatSetsToAcquire();

        //  Set up a table model with this many rows and appropriate columns
        String columnNames[] = {"Number", "Filter", "Binning", "Done"};
        this.sessionTableModel = new DefaultTableModel(columnNames, 0);
        for (FlatSet thisSet : this.flatSetList) {
            String[] rowValues = {String.valueOf(thisSet.getNumberOfFrames()),
            thisSet.getFilterSpec().getName(),
            String.valueOf(thisSet.getBinning()),
            String.valueOf(thisSet.getNumberDone())};
            this.sessionTableModel.addRow(rowValues);
        }

        this.sessionTable.setModel(this.sessionTableModel);
    }

    private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label4 = new JLabel();
        label5 = new JLabel();
        scrollPane1 = new JScrollPane();
        sessionConsole = new JList();
        scrollPane2 = new JScrollPane();
        sessionTable = new JTable();
        showADUsCheckbox = new JCheckBox();
        closeButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setMinimumSize(new Dimension(755, 630));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {76, 370, 0, 165, 88, 0};
                ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {54, 534, 0, 0, 0, 0};
                ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- label4 ----
                label4.setText("Session Console");
                label4.setFont(new Font(".SF NS Text", Font.PLAIN, 14));
                contentPanel.add(label4, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.NORTH, GridBagConstraints.NONE,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label5 ----
                label5.setText("Frames to Acquire");
                label5.setFont(new Font(".SF NS Text", Font.PLAIN, 14));
                contentPanel.add(label5, new GridBagConstraints(2, 0, 3, 1, 0.0, 0.0,
                    GridBagConstraints.NORTH, GridBagConstraints.NONE,
                    new Insets(0, 0, 5, 0), 0, 0));

                //======== scrollPane1 ========
                {
                    scrollPane1.setViewportView(sessionConsole);
                }
                contentPanel.add(scrollPane1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //======== scrollPane2 ========
                {

                    //---- sessionTable ----
                    sessionTable.setRowSelectionAllowed(false);
                    sessionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    sessionTable.setEnabled(false);
                    scrollPane2.setViewportView(sessionTable);
                }
                contentPanel.add(scrollPane2, new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- showADUsCheckbox ----
                showADUsCheckbox.setText("Show ADU values in log");
                contentPanel.add(showADUsCheckbox, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- closeButton ----
                closeButton.setText("Close");
                contentPanel.add(closeButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                contentPanel.add(cancelButton, new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label4;
    private JLabel label5;
    private JScrollPane scrollPane1;
    private JList sessionConsole;
    private JScrollPane scrollPane2;
    private JTable sessionTable;
    private JCheckBox showADUsCheckbox;
    private JButton closeButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
