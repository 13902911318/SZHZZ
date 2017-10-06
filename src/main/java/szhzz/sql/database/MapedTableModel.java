package szhzz.sql.database;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

/**
 * MapedTableModel 衍生于 TableModel 接口，实现了 JTable (UI)用于询问表格式数据模型
 * 的方法。只要数据模型实现了 TableModel 接口，就可以通过以下两行代码设置 JTable 显示
 * 该模型的数据：
 * <br>      TableModel myData = new MyTableModel();
 * <br>      JTable table = new JTable(myData);
 * <p/>      refer to http://gceclub.sun.com.cn/Java_Docs/html/zh_CN/api/javax/swing/table/TableModel.html
 * <p/>
 * MapedTableModel 是 AbstractTableModel的子类，但不是可实例化的最终类
 * <br> MapedTableModel 子类实现一些特殊的数据集操作功能例如：
 * <br>
 * <br> dbTableModel 用于从 ToProxy 读写数据,允许向最终用户隐含部分字段
 * <br> TableFilter  用于过滤数据
 * <br> TableSorter  用于排序数据集...
 * <br>
 * <br> 这些 MapedTableModel 可以串接起来，同时实现各种复杂功能。
 * <br>
 * <br> ToProxy <=> dbTableModel <=> TableFilter <=> TableSorter <=> Jtable (DataWindow)
 * <br>
 * <br> 这些 MapedTableModel 类具有一些共同的特点，就是输出行号和输入行号不一定相同，
 * 因此，需要映射输出输入行号，才能正确地通过 UI 操作DataStore.
 * <p/>
 * <p/>
 * Created by Jhon.
 * User: Jhon
 * Date: 2006-10-1
 * Time: 12:53:04
 *
 * @see dbTableModel
 * @see TableFilter
 * @see TableSorter
 */
public abstract class MapedTableModel extends AbstractTableModel {
    protected TableModel tableModel = null;
    protected JTableHeader tableHeader = null;
    protected ColumnLock columnLocker = null;

    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
    }

    public void setTableHeader(JTableHeader tableHeader) {
        this.tableHeader = tableHeader;
    }

    /**
     * getOraRow
     * get row number map to of dataStore
     *
     * @param row
     * @return int
     */
    public int getOraRow(int row) {
        if (null != tableModel && tableModel instanceof MapedTableModel)
            return ((MapedTableModel) tableModel).getOraRow(modelRowIndex(row));
        return row;
    }

    public int getViewRow(int row) {
        if (null != tableModel && tableModel instanceof MapedTableModel)
            return tableRow(((MapedTableModel) tableModel).getViewRow(row));
        return row;
    }

    public boolean isNewRow(int row) {
        if (row < 0) return false;
        if (null != tableModel && tableModel instanceof MapedTableModel)
            return ((MapedTableModel) tableModel).isNewRow(modelRowIndex(row));

        return false;
    }

    public boolean isValidate(int col, Object value) {
        if (null != tableModel && tableModel instanceof MapedTableModel) {
            return ((MapedTableModel) tableModel).isValidate(col, value);
        }
        return false;
    }

    public Class getColumnClass(int c) {
        if (null != tableModel && tableModel instanceof MapedTableModel) {
            //return getValueAt(0, c).getClass();
            return ((MapedTableModel) tableModel).getColumnClass(c);
        }
        if (tableModel.getRowCount() > 0)
            return getValueAt(0, c).getClass();
        else
            return Object.class;
    }

    public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }

    public Object getValueAt(int row, String col) {
        return null;
    }

    /**
     * implement row mapping
     *
     * @param viewIndex
     * @return int
     */
    public abstract int modelRowIndex(int viewIndex);

    public abstract int tableRow(int dataIndex);

    /**
     * set the row has total function
     *
     * @param col
     */
    public void setCalculateCol(int col) {
    }


    /**
     * *********************************************************
     */
    // Column mapping Section
    // if order or number of display column differ from ds column
    // we have to use these method to get invisible data
    public void setVisibleCols(int cols[]) {
    }

    // resetIndex colum visible to all
    public void setVisibleCols() {
    }

    boolean isColMaped() {
        if (null != tableModel && tableModel instanceof MapedTableModel) {
            return ((MapedTableModel) tableModel).isColMaped();
        }
        return false;
    }

    int colMap(int column) {
        if (null != tableModel && tableModel instanceof MapedTableModel) {
            return ((MapedTableModel) tableModel).colMap(column);
        }
        return column;
    }

    public int colMap(String column) {
        if (null != tableModel && tableModel instanceof MapedTableModel) {
            return ((MapedTableModel) tableModel).colMap(column);
        }
        return 0;
    }

    public ColumnLock getColumnLocker() {
        return this.columnLocker;
    }

    public void setColumnLocker(ColumnLock columnLocker) {
        this.columnLocker = columnLocker;
    }
}

