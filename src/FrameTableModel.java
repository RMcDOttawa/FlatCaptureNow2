import javax.swing.table.AbstractTableModel;

public class FrameTableModel extends AbstractTableModel {

    private final DataModel dataModel;

    public FrameTableModel(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    @Override
    public int getRowCount() {
        // todo getRowCount
        System.out.println("getRowCount");
        return 0;
    }

    @Override
    public int getColumnCount() {
        // todo getColumnCount
        System.out.println("getColumnCount");
        return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // todo getValueAt
        System.out.println("getValueAt");
        return null;
    }
}
