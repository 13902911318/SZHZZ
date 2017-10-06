package szhzz.sql.database;


import java.util.Vector;

/**
 * <p>Title: INFO2820</p>
 * <p/>
 * <p>Description: home work INFO2820</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p/>
 * <p>Company: </p>
 * <p/>
 * <p/>
 * <br> dbTableModel 是 MapedTableModel 的子类， 用于实现对 ToProxy 的读写功能。
 * <br> 参考 MapedTableModel
 * <p/> dbTableModel 允许向最终用户隐含部分字段，被隐含得字段得操作见 Column mapping Section
 *
 * @author John
 * @version 1.0
 */
public class dbTableModel extends MapedTableModel {
    DataStore ds = null;
    Vector colMap = null;

    public dbTableModel() {
    }

    public int appendRow() {
        return ds.appendRow();
    }

    public int getColumnCount() {
        if (ds == null) return 0;
        return (isColMaped() ? colMap.size() : ds.getColumnCount());
    }

    public int getRowCount() {
        if (ds == null) return 0;
        return ds.getRowCount();
    }

    public String getColumnName(int col) {
        return ds.getColumnName(colMap(col));
    }

    public Object getValueAt(int row, int col) {
        ds.scrollToRow(row);
        return ds.getObject(colMap(col));
    }

    public Object getValueAt(int row, String col) {
        ds.scrollToRow(row);
        return ds.getObject(colMap(col));
    }

    public boolean isNewRow(int row) {
        return ds.isNewRow(row);
    }

    /**
     * @param c
     * @return Class
     */
    public Class getColumnClass(int c) {
        return ds.getColumnClass(colMap(c));
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        if (null != columnLocker) {
            if (columnLocker.isLocked(row, col)) return false;
        }
        return (!ds.isReadonlyCol(colMap(col)));   // || tableModel.isNewRow(row)
    }

    /*
    * Don't need to implement this method unless your table's
    * data can change.
    */
    public void setValueAt(Object value, int row, int col) {
//        ds.scrollToRow(row);
//        ds.updateObject(colMap(col), value);
        ds.setValueAt(value, row, colMap(col));
    }

    public int modelRowIndex(int viewIndex) {
        return viewIndex;
    }

    @Override
    public int tableRow(int dataIndex) {
        return dataIndex;
    }

    public boolean isValidate(int col, Object value) {
        return ds.isValidate(colMap(col), value);
    }

    /**
     * ************************************
     */
    // Column mapping Section
    // if order or number of display column differ from ds column
    // we have to use these method to get invisible data
    public void setVisibleCols(int cols[]) {
        colMap = new Vector();  // if colMap != null, clean it
        for (int i = 0; i < cols.length; i++) {
            colMap.add(new Integer(cols[i]));
        }
    }

    public void setVisibleCols() {
        colMap = null;
    }

    boolean isColMaped() {
        return (colMap != null && colMap.size() > 0);
    }

    int colMap(int column) {
        if (!isColMaped()) return column;
        if (column >= colMap.size()) return column; //??
        return ((Integer) colMap.get(column)).intValue();
    }

    public int colMap(String column) {
        int col = ds.getColIndex(column);
        return col;
    }

    public DataStore getDataStore() {
        return ds;
    }

    public void setDataStore(DataStore ds) {
        this.ds = ds;
    }
}
