import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
/*
 * Created by JFormDesigner on Wed Feb 26 14:14:57 EST 2020
 */



/**
 * Window controller for the dialog that opens during an acquisition session.  It contains a table
 * showing all the flat frame sets that will be acquired in the session (with the one currently
 * being acquired highlighted), and a console log.  Also a Cancel button that will interrupt acquisition.
 * @author Richard McDonald
 */
public class Session extends JDialog {
    DefaultTableModel sessionTableModel = null;
    DefaultListModel<String> sessionConsoleModel = null;
    DataModel dataModel;

    // Lock used to serialize UI updates coming from the sub-thread.
    // Includes console messages, progress bar updates, highlighting and updating the session plan table.
    // All those events are locked with the same lock, so the UI updates happen atomically and in sequence

    private ReentrantLock consoleLock = null;
    private SessionThread sessionRunnable;
    private Thread sessionThread;


    ArrayList<FlatSet> flatSetList;
	public Session(Window owner) {
		super(owner);
		initComponents();
	}

	/**
	 * Set up the user interface to be ready for acting as session monitor.
	 * 		- Set up a default table model for the frames table, that we'll use to show where we are
     * @param flatSetList     Array of the flat sets to be acquired
	 */
	public void setUpUI(DataModel dataModel,
                        ArrayList<FlatSet> flatSetList) {
        this.flatSetList = flatSetList;
        this.dataModel = dataModel;

		//  Set up table data model, using default model where we just manually add rows.
        this.setUpSessionTable(flatSetList);

        //  Set up data model for the JList that is used for the console log
        this.setUpSessionConsole();

        //  Don't allow window to be closed while task is running
        //  Do allow Cancel
        this.closeButton.setEnabled(false);
        this.cancelButton.setEnabled(true);
	}

    /**
     * Set up a DefaultTableModel containing a list of all the flat frames we'll be acquiring
     * One row per flat frame set, with columns:
     *     Number wanted
     *     Filter
     *     Binning
     *     Number done
     * @param flatSetList     Array of the flat sets to be acquired
     */
    private void setUpSessionTable(ArrayList<FlatSet> flatSetList) {

        //  Set up a table model with this many rows and appropriate columns
        String columnNames[] = {"Number", "Filter", "Binning", "Done"};
        this.sessionTableModel = new DefaultTableModel(columnNames, 0);
        for (FlatSet thisSet : flatSetList) {
            String[] rowValues = {String.valueOf(thisSet.getNumberOfFrames()),
            thisSet.getFilterSpec().getName(),
            String.valueOf(thisSet.getBinning()),
            String.valueOf(thisSet.getNumberDone())};
            this.sessionTableModel.addRow(rowValues);
        }

        this.sessionTable.setModel(this.sessionTableModel);
    }

    /**
     * Using a default list model, set up the session console list
     */
    private void setUpSessionConsole() {
        this.sessionConsoleModel = new DefaultListModel<>();
        this.sessionConsole.setModel(this.sessionConsoleModel);
    }

    /**
     * Store the "Show ADUs" setting from the checkbox.  This is used to determine
     * whether to display the additional detail line reporting the ADUs of a just-acquired flat frame
     */
    private void showADUsCheckboxActionPerformed() {
        //  We don't actually need to do anything.  We don't need to save this setting, just check
        //  it during a session, and we'll do that by querying the button itself.
    }

    /**
     * User clicked "close" - close the dialog.  Disabled while the acquisition thread is running.
     */
    private void closeButtonActionPerformed() {
        this.setVisible(false);
    }

    /**
     * user clicked "Cancel".  Cancel the running acquisition thread.
     */
    public void cancelButtonActionPerformed() {
        if (this.sessionThread != null) {
            this.sessionThread.interrupt();
        }
        this.sessionThread = null;
        this.sessionRunnable = null;
    }

    /**
     * Start the thread to do the flat frame acquisition
     * @param sessionWindow     This window
     * @param flatsToAcquire    The list of flat frames to acquire
     */
    public void spawnAcquisitionTask(Session sessionWindow, ArrayList<FlatSet> flatsToAcquire) {
        this.consoleLock = new ReentrantLock();
        this.sessionRunnable = new SessionThread(sessionWindow, this.dataModel, flatsToAcquire);
        this.sessionThread = new Thread(sessionRunnable);
        this.sessionThread.start();
    }

    /**
     * Receive a message from the acquisition thread that it is finished, so we can clean up
     */
    public void acquisitionThreadEnded() {
        this.closeButton.setEnabled(true);
        this.cancelButton.setEnabled(false);
    }

    private static final String INDENTATION_BLANKS = "    ";

    /**
     * Add a line to the console pane in the session pane, and scroll to keep it visible
     * We'll do a thread-lock on this code since requests will be coming from the sub-thread and
     * we want to ensure we don't try to run the code more than once in parallel.
     * @param message               Text message to be added to console log
     * @param messageLevel          Indentation level of message (1 = base level)
     */
    public void console(String message, int messageLevel) {
        this.consoleLock.lock();
        try {
            assert (messageLevel > 0);
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
            String indentation = (messageLevel == 1) ? "" : StringUtils.repeat(INDENTATION_BLANKS, messageLevel - 1);
            this.sessionConsoleModel.addElement(time + ": " + indentation + message);
            //  Ensure the line we just added is visible, scrolling if necessary
            this.sessionConsole.ensureIndexIsVisible(this.sessionConsoleModel.getSize() - 1);
        }
        finally {
            //  Use try-finally to ensure unlock happens even if some kind of exception occurs
            this.consoleLock.unlock();
        }
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
                showADUsCheckbox.addActionListener(e -> showADUsCheckboxActionPerformed());
                contentPanel.add(showADUsCheckbox, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- closeButton ----
                closeButton.setText("Close");
                closeButton.addActionListener(e -> closeButtonActionPerformed());
                contentPanel.add(closeButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.addActionListener(e -> cancelButtonActionPerformed());
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
