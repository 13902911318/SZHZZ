package szhzz.sql.database;


import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.Hashtable;
import java.util.Vector;

/**
 * <br> TableFilter 是 MapedTableModel 的子类， 用于实现数据行过滤功能例如。
 * <br> 参考 MapedTableModel
 * <p/>
 * 在 DataWindow 类里，TableFilter 总是自动加载的，插入在 ToProxy 和 DataWindow 之间。
 * 但是，一个缺省的 TableFilter 并不做什么事情。 只有当我们用 TableFilter(Filter ft)
 * 设置或变换一个过滤器的时候，TableFilter 才会起作用。这使得 TableFilter 的使用灵活方便。
 * <p/>
 * <p/>
 * 变量 filterNewRow 的用途：
 * 在 ToProxy 添加一个记录后，如果不输入与当前Filter相对应的
 * 字段值，Filter 发生变化时这些新记录就可能不在看得到，
 * (特别是在 Linked DataWindow 的情况下)。
 * 设置 filterNewRow ＝ true，使 filter 总是允许新记录不被过滤掉。
 * <p/>
 * 另一种替代 filterNewRow  的方法是在 Filter 实例里设置 AutoFillCol
 * 当向  ToProxy 添加新记录时，自动填写某些字段的值，从而避免
 * 新记录丢失。 参见 Panel_C
 * <p/>
 * <p/>
 * Created by John.
 * User: John
 * Date: 2006-10-1
 * Time: 20:12:44
 *
 * @see Filter
 * see jBgui.Panel_C  Filter 的实现方法
 */
public class TableFilter extends MapedTableModel {
    Vector<Integer> filterdRow = null;
    Hashtable<Integer, Integer> rowIndex = null;
    Filter ft = null;
    TableModelHandler tableModelListener;
    boolean filtering = false;
    private boolean filterNewRow = false;

    public TableFilter() {
        ft = new Filter();  // defalut, no function Filter
        this.tableModelListener = new TableModelHandler();  // auto filter if data changed
    }

    public TableFilter(Filter ft) {
        this();
        setFilter(ft);
    }

    /**
     * setTableModel.
     * <p/>
     * link to tableModel
     *
     * @param tableModel
     */
    public void setTableModel(TableModel tableModel) {
        if (this.tableModel != null) {
            this.tableModel.removeTableModelListener(tableModelListener);
        }

        this.tableModel = tableModel;
        if (this.tableModel != null) {
            this.tableModel.addTableModelListener(tableModelListener);
        }
        filter();
//        fireTableStructureChanged();
    }

    public void setFilter(Filter ft) {
        if (ft == null) ft = new Filter();
        this.ft = ft;
    }


    /**
     * apply filter
     * <p/>
     * Note: synchronized
     * concurrent filtering is allowed
     * <p/>
     * when use fiter for linked datawindow,
     * synchronized(filterdRow) prevent user quickly clicked 2 or more rows
     * on a marster datawindow
     *
     * @see synchronized
     */
    public void filter() {
        synchronized (this) {
            filterdRow = new Vector<Integer>();
            rowIndex = null;

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (!filterNewRow &&
                        tableModel instanceof MapedTableModel &&
                        ((MapedTableModel) tableModel).isNewRow(i)) {

                    filterdRow.add(i);
                } else if (ft.filter(tableModel, i)) {
                    filterdRow.add(new Integer(i));
                }
            }
            fireTableDataChanged();
        }
    }

    public int getColumnCount() {
        return tableModel.getColumnCount();
    }

    public int getRowCount() {
        if (null != filterdRow) return filterdRow.size();
        return tableModel.getRowCount();
    }

    public Object getValueAt(int row, int col) {
        return tableModel.getValueAt(modelRowIndex(row), col);
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        if (null != columnLocker) {
            if (columnLocker.isLocked(row, col)) return false;
        }
        return tableModel.isCellEditable(modelRowIndex(row), col);
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        tableModel.setValueAt(value, modelRowIndex(row), col);
    }

    public boolean hasAutoFill() {
        return ft.hasAutoFill();
    }

    public Hashtable getAutoFillCols() {
        return ft.getAutoFillCols();
    }

    /**
     * implement row mapping
     *
     * @param row
     * @return int
     */
    public int modelRowIndex(int row) {
        if (null == filterdRow) filter();

        if (row < filterdRow.size() && row >= 0)
            return filterdRow.get(row);
        else
            return -1;
    }

    @Override
    public int tableRow(int dataIndex) {
        if (null == filterdRow) filter();
        if (rowIndex == null) {
            rowIndex = new Hashtable<Integer, Integer>();
            for (int i = 0; i < filterdRow.size(); i++) {
                rowIndex.put(filterdRow.get(i), i);
            }
        }
        Integer i = rowIndex.get(dataIndex);
        return i == null ? -1 : i;
    }

    public void setFilterNewRow(boolean filterNewRow) {
        this.filterNewRow = filterNewRow;
    }

    /**
     * TableModelHandler
     * <p/>
     * TableModelHandler defines the interface for an object that listens
     * to changes in a TableModel.
     *
     * @see javax.swing.event.TableModelListener
     */
    private class TableModelHandler implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            filter();
        }
    }
}

