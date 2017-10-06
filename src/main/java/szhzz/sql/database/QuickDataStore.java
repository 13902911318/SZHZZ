package szhzz.sql.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-1-3
 * Time: 下午10:12
 * To change this template use File | Settings | File Templates.
 */
public class QuickDataStore extends DataStore {
    private Vector<ArrayList> rows = new Vector<ArrayList>();   // celection of dataRows
    private ArrayList currentRecord;

    public int getJDBCRowNumber() {
        return -1;
    }

    public String getUpdateTable() {
        return null;
    }

    public void setUpdateTable(String updateTable) {
    }

    /**
     * 非线程安全
     *
     * @param obj
     */
    public void addObject(Object obj) {
        if (currentRecord.size() <= getColumnCount()) {
            currentRecord.add(obj);
        } else {
        }
    }

    /**
     * 非线程安全
     *
     * @param dbRow
     */
    void addRow(int dbRow) {
        currentRecord = new ArrayList();
        rows.add(currentRecord);
        currentRow = rows.size() - 1;
    }

    /**
     * 不支持的函数
     *
     * @param ds
     * @return
     */
    public int addAll(DataStore ds) {
        return -1;
    }

    public void deleteRow(int row) {
        rows.remove(row);
    }

    public DataStore getDeleteRows() {
        return null;
    }

    public int appendRow() {
        currentRecord = new ArrayList();
        for (int i = 0; i < columnNames.size(); i++) {
            Object val = defaltValues.get(i);
            if (val != null) {
                addObject(val);
            } else {
                addObject(JDBCType.isNULL);
            }
        }
        rows.add(currentRecord);
        currentRow = rows.size() - 1; // we are at the last row
        return currentRow;
    }

    public int appendFirstRow() {
        currentRecord = new ArrayList();
        for (int i = 0; i < columnNames.size(); i++) {
            Object val = defaltValues.get(i);
            if (val != null) {
                addObject(val);
            } else {
                addObject(JDBCType.isNULL);
            }
        }
        rows.insertElementAt(currentRecord, 0);
        currentRow = 0; // we are at the last row
        return currentRow;
    }

    public void updateObject(int col, Object value) {
        currentRecord.set(col, value);
    }

    public void updateObject(String col, Object value) {
        Integer pos = columnNames.get(col.toUpperCase());
        currentRecord.set(pos, value);
    }

    public boolean setValueAt(Object value, int row, int col) {
        if (rows.size() == 0 || row < 0 || row >= rows.size()) {
            return false;
        }
        try {
            ArrayList data = rows.get(row);
            data.set(col, value);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    public Object getValueAt(int row, int col) {
        if (rows.size() == 0 || row < 0 || row >= rows.size()) {
            return null;
        }
        try {
            ArrayList data = rows.get(row);
            Object obj = data.get(col);
            if (JDBCType.isNULL.equals(obj)) return null;
            return obj;
        } catch (Exception e) {

        }
        return null;
    }

    public boolean isNewRow(int row) {
        return true;
    }

    public boolean isModified(int row, int col) {
        return true;
    }

    public boolean isModified(int row) {
        return true;
    }

    public boolean isDirty() {
        return true;
    }

    public int getRowCount() {
        return rows.size();
    }

    public Object peekObject(Integer row, Integer col, int bufferType) {
        return getValueAt(row, col);
    }

    public Object peekObject(int row, int col) {
        return getValueAt(row, col);
    }

    public Object peekObject(int row, String col) {
        Integer c = columnNames.get(col.toUpperCase());
        return peekObject(row, c);
    }

    public Object peekObject(int row, String col, int buffer) {
        Integer c = columnNames.get(col);
        return peekObject(row, c, buffer);
    }

    public Object getObject(int col) {
        if (col < 0 || col > columnIndex.size()) return null;

        if (null == currentRecord) return null;

        if (JDBCType.isNULL == currentRecord.get(col)) return null;

        return currentRecord.get(col);
    }

    public int scrollToRow(int pos) {
        if (rows.size() == 0 || pos < 0 || pos >= rows.size()) {
            currentRecord = null; // for addtion operation throw exception
            return -1;
        }
        if (currentRow == pos) return currentRow;

        currentRow = pos;
        currentRecord = rows.get(currentRow);
        return currentRow;
    }

    public Object getRowData(int pos) {
        if (rows.size() == 0 || pos < 0 || pos >= rows.size()) {
            return null;
        }
        return rows.get(pos);
    }

    public void scroll(int pos) {
        scrollToRow(currentRow + pos);
    }

    public void clearUpdate() {
    }

    public void clearUpdate(int row) {
    }

    public void BackupToFile(String fileName) throws IOException {
    }

    public int find(int col, Object value) {
        ArrayList Record;
        Object val;
        for (int r = 0; r < this.getRowCount(); r++) {
            Record = rows.get(r);
            val = Record.get(col);
            if (value.equals(val)) return r;
        }
        return -1;
    }

    public void clear() {
        rows.removeAllElements();
    }


    public void printout() {
    }

    public boolean isDeletrow(int row) {
        return false;
    }

    public String getUpdateScript() {
        return "";
    }

    public String getLogString() {
        return "";
    }

}
