import javax.swing.table.DefaultTableModel;

/**
 * Extension of Default Table Model used for the table displayed in the Session window
 * that shows the planned frames, and highlights the set in collection.
 * The extension is so that the "is Editable" method can be overridden to always return false.
 */
public class SessionSetsTableModel extends DefaultTableModel {

    public SessionSetsTableModel(String[] columnNames, int i) {
        super(columnNames, i);
    }

    /**
     * Override to disallow all cell editing
     * @param rowIndex          Row index being checked. (Ignored, all rows are editable)
     * @param columnIndex       Column index being checked.
     * @return (boolean)
     */
    @Override
    public boolean isCellEditable(int rowIndex,
                                  int columnIndex) {
        return false;
    }
}
