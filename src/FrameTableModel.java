import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

/**
 * Data model for the Frame Table on the main window.  This is a matrix mapping every in-use Filter to every
 * in-use binning.  Filters down the left side, binnings across the top.  At each cell is the number of frames
 * to be taken of that binning combination.
 *
 * Example
 *                  1x1     2x2     3x3
 * Red                0       32     32
 * Green              0       32     32
 * Blue               0       32     32
 * Luminance         32       32      0
 *
 * A JTableHeader will provide the column headers - the binning values in the example above.
 * Since Swing has no built-in way to do "Row Headers" - the filter names shown - we will just fake
 * it by having the table have one more column than needed, and we'll provide filter names for the first
 * column, and some code to ensure they can't be selected or edited
 */

public class FrameTableModel extends DefaultTableModel {
    private  MainWindow mainWindow;
    private  DataModel dataModel;

    /**
     * Construct table data model, given a reference to the program's data model object
     * @param dataModel     Program's data model (containing the frame plan data)
     */
    public FrameTableModel(MainWindow mainWindow, DataModel dataModel) {
        super();
        this.mainWindow = mainWindow;
        this.dataModel = dataModel;
    }

    /**
     * How many rows should be displayed in the table?
     * This is the number of filters with "Use" checked.  Unused filters aren't displayed.
     * @return (int)
     */
    @Override
    public int getRowCount() {
        if (this.dataModel == null) {
            return 0;
        } else {
            return this.dataModel.countUsedFilters();
        }
    }

    /**
     * How many columns should be displayed in the table?
     * This is the number of Binning values whose availability is "Default" or "Available", plus
     * one more column that we use for the row headers in the left margin
     * @return  (int)
     */
    @Override
    public int getColumnCount() {
        return this.dataModel.countUsedBinning() + 1;  // Add 1 for row header
    }

    /**
     * Get value for table cell at given coordinates.
     * Row index refers to the filter in the enabled filters.  Column index is 0 for the left-margin
     * row header, and 1 or more for the filter binnings.
     * @param rowIndex          Row index being accessed
     * @param columnIndex       Column index being accessed
     * @return (String or null)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            FilterSpec thisFilter = this.dataModel.getFilterInUse(rowIndex);
            int slotNumber = thisFilter.getSlotNumber();
            String filterName = thisFilter.getName();
            return String.format("<html><b>%d: %s</b></html>", slotNumber, filterName);
        } else {
            return this.dataModel.getFrameCountAt(rowIndex, columnIndex - 1);
        }
    }

    /**
     * Get string used as the header above a given column.  Note that column "0" is the
     * pseudo-row-headers and doesn't have a column title.
     * @param column        Zero-based column index.
     * @return (String)
     */
    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return null;
        } else {
            int binning = this.dataModel.getBinning(column - 1);
            return String.format("<html><b><font size=\"-1\">%d x %d</font></b></html>", binning, binning);
        }
    }

    /**
     * Override to allow editing of all cells except the first column (which is pseudo-headers)
     * @param rowIndex          Row index being checked. (Ignored, all rows are editable)
     * @param columnIndex       Column index being checked.
     * @return (boolean)
     */
    @Override
    public boolean isCellEditable(int rowIndex,
                           int columnIndex) {
        return columnIndex > 0;
    }

    /**
     * User has changed one of the values in the table by editing on-screen.
     * Pick up the changed value and put it in the data model.
     * @param value         New value from user
     * @param rowIndex      Zero-based row index of cell
     * @param columnIndex   Zero-based column index of cell
     */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        int newValue = (Integer) value;
        //   Adjust column index to skip over the "pseudo column" of row headers
        this.dataModel.setFrameCountAt(rowIndex, columnIndex - 1, newValue);
        fireTableCellUpdated(rowIndex, columnIndex);
        // Having changed a table cell, we may have changed whether acquisition can proceed
        // Tell the main window to check that.
        this.mainWindow.enableProceedButton();
        this.mainWindow.makeDirty();
    }}

