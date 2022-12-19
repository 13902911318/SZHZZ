package szhzz.sql.database;


import szhzz.Calendar.MyDate;
import szhzz.Utils.Utilities;

import java.io.IOException;
import java.util.*;

/**
 * ToProxy.
 * todo TBD or TBM
 * <p/>
 * <p/>
 * ToProxy 是一个通用的，有修改状态的，无可视界面的数据集。
 * ToProxy 可以产生于 GeneralDAO， 介于用户界面代码和数据库查询代码之间
 * 用于暂存来自数据库的数据。这些数据可以应用于
 * 可视用户界面供用户查询，修改。 ToProxy 记录每个字段的
 * 修改状态，以便更新到数据库时采取合适的更新方法。
 * <p/>
 * ToProxy 可以是只读的，这时， ToProxy 内的数据都拒绝被修改。
 * ToProxy 可以针对个别字段设置只读特性，这时这些字段将拒绝被修改。
 * <p/>
 * 可以针对 ToProxy 的指定字段 【setColValidator(int col, DataValidater dv)】
 * 设置不同的 DataValidater 子类来进行指定字段的更新数据有效性检查 【isValidate()】 。
 * 同一字段可以同时设置数个检查类型。ToProxy 将依次进行指定的检查。
 * <p/>
 * <p/>
 * <p>Title: INFO2820</p>
 * <p/>
 * <p>Description: home work INFO2820</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author John
 * @version 1.0
 * <p/>
 * see dbquery.database.GeneralDAO
 */
public class DataStore {
    public static final int CURRENTVALUE = 0;
    public static final int MODIFIEDVALUE = 1;
    public static final int PRIMARYVALUE = 2;
    int currentRow = 0;          // current editing row number
    HashMap<String, Integer> columnNames = new HashMap<String, Integer>();
    HashMap<Integer, String> columnIndex = new HashMap<Integer, String>();
    HashMap<Integer, String> columnOriginalName = new HashMap<Integer, String>();
    Hashtable<Integer, Object> defaltValues = new Hashtable<Integer, Object>();
    private String Name = "";
    private Vector<dataRow> rows = new Vector<dataRow>();   // celection of dataRows
    private dataRow currentRecord;     // current editing record
    private DataStore deletedRows = null;    // deleted rows buffer
    private Hashtable<Integer, String> columnTypes = new Hashtable<Integer, String>();
    private Hashtable<Integer, Integer> columnLength = new Hashtable<Integer, Integer>();
    private Hashtable<Integer, Vector> columnValidater = new Hashtable<Integer, Vector>();
    private Hashtable<Integer, ColumnCalculator> columnCalculaters = new Hashtable<Integer, ColumnCalculator>();
    private Vector<Integer> readonlyCols = new Vector<Integer>();
    private Vector<Integer> updateKeyCols = new Vector<Integer>();
    private String updateTable = null;
    private Vector<Integer> updateCols = new Vector<Integer>();
    private boolean readOnly = true;     // All datas in the store cannot be changed.


    public DataStore() {

    }

    public static Double parseDouble(Object o, Double defaultValue) {
        if (o == null) return defaultValue;
        if (o instanceof Double) return (Double) o;

        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(o.toString().replace(",", "").trim());
            } catch (NumberFormatException e2) {

            }
        }
        return defaultValue;
    }

//    public String getColumnName(int pos) {
//        return columnIndex.get(pos);

    //    }

    public static Long parseLong(Object o, Long defaultValue) {
        if (o == null) return defaultValue;
        if (o instanceof Long) return (Long) o;

        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            try {
                Double v;
                if (defaultValue == null) {
                    v = parseDouble(o, null);
                } else {
                    v = parseDouble(o, defaultValue.doubleValue());
                }
                if (v != null) {
                    return v.longValue();
                }
            } catch (NumberFormatException e1) {

            }
        }
        return defaultValue;
    }

    public void setColName(String name, int pos) {
        name = name.toUpperCase();

        String oldName = columnIndex.get(pos);
        if (oldName != null) {
            columnNames.remove(oldName);
        }

        columnNames.put(name, pos);
        columnIndex.put(pos, name);
    }

    public String getOriginalName(int col) {
        return columnOriginalName.get(new Integer(col));
    }

    public void setOriginalName(String name, int pos) {
        columnOriginalName.put(pos, name);
    }


    public String getColumnName(int col) {
        return columnIndex.get(new Integer(col));
    }

    public boolean hasColumnName(String col) {
        return columnIndex.containsValue(col);
    }

    public HashMap<String, Integer> cloneColumnMap() {
        return (HashMap<String, Integer>) columnNames.clone();
    }

    public int getColIndex(String col) {
        Integer obj = columnNames.get(col.toUpperCase());
        if (obj == null) {
//            loger.getLoger().errorOut(
//                    new DBException("No such column " + col,
//                            DBException.SQLEXCEPTION));
            return -1;
        }
        return obj;
    }

    public void setColTypeName(String name, int pos) {
        columnTypes.put(pos, name);
    }

    public String getColTypeName(int col) {
        return columnTypes.get(col);
    }

    public int ColumeCount() {
        return columnTypes.size();
    }

    public boolean isSameStructure(DataStore ds) {
        if (ds == null || columnTypes == null) return false;
        if (ColumeCount() != ds.ColumeCount()) return false;
        for (int i = 0; i < this.ColumeCount(); i++) {
            if (!getColTypeName(i).equals(ds.getColTypeName(i))) {
                return false;
            }
        }
        return true;
    }

    public void setColLength(int col, int len) {
        columnLength.put(col, len);
    }

    public Class getColumnClass(String col) {
        int pos = columnNames.get(col.toUpperCase());
        return getColumnClass(pos);
    }

    public Class getColumnClass(int col) {
        //return currentRecord.getCurrent(col).getClass();

        if (rows.size() > 0) {
            Object o = rows.get(0).getCurrent(col);
            if (o != null) return o.getClass();
        }

        return SqlJavaTypeMaper.SqlToJavaClass(getColTypeName(col));
    }

    public int getColLength(int col) {
        if (columnLength.containsKey(new Integer(col)))
            return columnLength.get(col);

        return -1;
    }

    public void setColumnCalculaters(int col, ColumnCalculator cal) {
        columnCalculaters.put(col, cal);
    }

    public void setColValidator(int col, DataValidater dv) {
        Vector validaters = columnValidater.get(new Integer(col));
        if (dv == null && validaters != null) {
            validaters.clear();
            return;
        }

        if (null == validaters) {
            validaters = new Vector();
            columnValidater.put(col, validaters);
        }
        if (!validaters.contains(dv)) {
            validaters.add(dv);
        }
    }

    /**
     * UpdateKey used to create "WHERE" claause
     * while updatting data to Database.
     * if no UpdateKey is defined, all columns will be used
     * to create "WHERE" claause
     * Only applied when set : GeneralDAO.setUpdateUseScript(true)
     *
     * @param col
     */
    public void setUpdateKey(Integer col) {
        if (null == col) return;
        updateKeyCols.remove(col);
        updateKeyCols.add(col);
    }

    public void setUpdateKey(String col) {
        setUpdateKey(columnNames.get(col));
    }

    /**
     * getJDBCRowNumber
     * <p/>
     * map row of datastore to JDBC row
     *
     * @return int
     */
    public int getJDBCRowNumber() {
        if (null != currentRecord) return currentRecord.getJDBCRowNumber();
        return -1;
    }

    /**
     * isValidate
     * <p/>
     * chack if a input data valide
     * be called from a UI
     *
     * @param col
     * @param value
     * @return boolean
     */
    public boolean isValidate(int col, Object value) {
        return isValidate(col, value, true);
    }

    public String getUpdateTable() {
        if (null == updateTable) return getName();
        return updateTable;
    }

    public void setUpdateTable(String updateTable) {
        this.updateTable = updateTable;
    }

    public void setUpdateCol(int col) {
        if (!updateCols.contains(col)) updateCols.add(col);
    }

    public void setUpdateCol(String col) {
        Integer c = columnNames.get(col);
        if (null != c) {
            setUpdateCol(c);
        }
    }

    /**
     * isValidate
     * <p/>
     * chack if a input data valide
     * be called from a UI
     *
     * @param col
     * @param value
     * @param showerror
     * @return boolean
     */
    public boolean isValidate(int col, Object value, boolean showerror) {
        DataValidater dv;
        Vector validaters = columnValidater.get(new Integer(col));
        if (null != validaters) {
            for (Object validater : validaters) {
                dv = (DataValidater) validater;
                if (!dv.validate(value)) {
                    if (showerror) {
//                        loger.getLoger().showError(new DBException(dv.getErrorMsg(),
//                                DBException.ILLEGALVALUE));
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Sale colum data to current row
     * user may not use this method.
     * it be called only from DAO to populate data
     *
     * @param obj
     */
    public void addObject(Object obj) {
        if (currentRecord != null && currentRecord.size() <= getColumnCount()) {
            currentRecord.add(obj);
        } else {
//            loger.getLoger().showError(
//                    new DBException("Attempt to get beyond number of columns in Table",
//                            DBException.RUNTIMEERROR));
        }
    }

    /**
     * user may not use this method.
     * it be called only from DAO to populate data
     * <p/>
     * Sale currentRecord to rows and create a new empty currentRecord
     * can be called only from classes within pakage dbquery
     */
    void addRow(int dbRow) {
        currentRecord = new dataRow(dbRow);
        rows.add(currentRecord);
        currentRow = rows.size() - 1;
    }

    public int addAll(DataStore ds) {
        for (dataRow row : ds.rows) {
            rows.add(row);
        }
        return ds.getRowCount();
    }

    /**
     * deleteRow
     * <p/>
     * a deleted row will be buffered in deletedRows
     * when apply update, these row will be delete from database
     * befor update, if we retrieve or refresh datastore
     * all deleted rows will be removed from deletedRows
     */
    public void deleteRow(int row) {
        this.scrollToRow(row);

        dataRow drow = rows.remove(currentRow);
        if (drow.isNewRow()) return;

        if (null == deletedRows) {
            deletedRows = new DataStore();
            deletedRows.columnNames = columnNames;
            deletedRows.columnIndex = columnIndex;
            deletedRows.columnTypes = columnTypes;
            deletedRows.columnValidater = columnValidater;
            deletedRows.readonlyCols = readonlyCols;
            deletedRows.readOnly = readOnly;
            deletedRows.columnLength = columnLength;
            deletedRows.Name = Name;
        }
        deletedRows.addDeletedRow(drow);

//        currentRow --;
//        this.scrollToRow(currentRow);
    }

    /**
     * this method only function in a deletedRows set.
     *
     * @param row
     */
    private void addDeletedRow(dataRow row) {
        currentRecord = row;
        currentRecord.setDeletrow(true);
        rows.add(currentRecord);
        currentRow = rows.size() - 1;
    }

    /**
     * getDeleteRows
     * <p/>
     * user may not use this method.
     * it be called only from DAO to delete database row
     *
     * @return ToProxy
     */
    public DataStore getDeleteRows() {
        return deletedRows;
    }

    /**
     * append a nwe row.
     * NOTE: appendRow() diffrent from addrow(),
     * addrow() is protected and be called by DAO to pupulate data
     * appendRow() is public and be triggered by user from UI
     */
    public int appendRow() {
        currentRecord = new dataRow(0);
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
        currentRecord = new dataRow(0);
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

    /**
     * @param col
     * @param value
     * @throws DBException
     */
    public void updateObject(int col, Object value) {
        currentRecord.updateObject(col, value);
    }

    public void updateObject(String col, Object value) {
        Integer pos = columnNames.get(col.toUpperCase());
        currentRecord.updateObject(pos, value);
    }

    /**
     * 数值安全型写入
     *
     * @param value
     * @param row
     * @param col
     * @return
     */
    public boolean setValueAt_s(Object value, int row, String col) {
        Object o = null;
        int index = getColIndex(col);
        String typeName = getColTypeName(index);

        if ("Long".equals(typeName)) {
            o = parseLong(value, null);
        } else if ("Double".equals(typeName)) {
            o = parseDouble(value, null);
        } else if ("Integer".equals(typeName)) {
            o = parseDouble(value, null);
            if (o != null) {
                o = ((Double) o).intValue();
            }
        } else if ("Float".equals(typeName)) {
            o = parseDouble(value, null);
            if (o != null) {
                o = ((Double) o).floatValue();
            }
        } else {
            o = value;
        }

        return setValueAt(o, row, index); //getColIndex(col)
    }

    /**
     * @param value
     * @param row
     * @param col
     * @return
     */
    public boolean setValueAt(Object value, int row, String col) {
        return setValueAt(value, row, getColIndex(col));
    }


    public boolean setValueAt(Object value, int row, int col) {
        if (rows.size() == 0 || row < 0 || row >= rows.size()) {
            return false;
        }
        try {
            dataRow data = rows.get(row);
            data.updateObject(col, value);
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
            dataRow data = rows.get(row);
            Object obj = data.get(col, CURRENTVALUE);
            if (JDBCType.isNULL.equals(obj)) return null;
            return obj;
        } catch (Exception e) {

        }
        return null;
    }

    public Object getValueAt(int row, String col) {
        return getValueAt(row, getColIndex(col));
    }

    public Object getValueAt(int row, String col, Object def) {
        Object o = getValueAt(row, getColIndex(col));
        if (o == null) {
            return def;
        }
        return o;
    }

    public Object getValueAt(int row, int col, Object def) {
        Object o = getValueAt(row, col);
        if (o == null) {
            return def;
        }
        return o;
    }

    /**
     * isNewRow
     * <p/>
     * check if row is a inserted row
     *
     * @return boolean
     * @paparam row
     */
    public boolean isNewRow(int row) {
        if (row >= 0 && row < rows.size()) {
            dataRow r = rows.get(row);
            if (r != null) {
                return r.isNewRow();
            }
        }
        return true;
    }

    /**
     * get Update status for a specific row and column
     *
     * @param row
     * @param col
     * @return boolean
     */
    public boolean isModified(int row, int col) {
        if (row < 0 || row > rows.size()) return false;
        dataRow r = rows.get(row);
        return r != null && r.isModified(col);
    }

    /**
     * get the update status for the current row
     *
     * @return boolean
     */
    public boolean isModified() {
        return isModified(getRowPos());
    }

    /**
     * get the update status for a specific row
     *
     * @param row
     * @return boolean
     */
    public boolean isModified(int row) {
        dataRow r = rows.get(row);
        return r.isModified();
    }

    public boolean isDirty() {
        for (int i = 0; i < rows.size(); i++) {
            if (isModified(i)) return true;
        }
        return false;
    }

    /**
     * getRowPos
     * <p/>
     * get current row number
     *
     * @return int
     */
    public int getRowPos() {
        return currentRow;
    }

    /**
     * getRowCount
     *
     * @return int
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * getColumnCount
     *
     * @return int
     */
    public int getColumnCount() {
        return columnNames.size();
    }

    /**
     * peekObject
     * <p/>
     * not scroll current row to get data by row and col
     *
     * @param row
     * @param col
     * @return Object
     */
    public Object peekObject(Integer row, Integer col, int bufferType) {
        if (row < 0) {
            return null;
        } else if (row >= rows.size()) {
            return null;
        }

        Object val = rows.get(row).get(col, bufferType);
        if (JDBCType.isNULL == val) return null;

        ColumnCalculator cal = columnCalculaters.get(new Integer(col));
        if (cal != null) val = cal.calculate(val);

        return val;
    }

    public Object peekObject(int row, int col) {
        return peekObject(row, col, CURRENTVALUE);
    }

    public Object peekObject(int row, String col) {
        Integer c = columnNames.get(col.toUpperCase());
        return peekObject(row, c, CURRENTVALUE);
    }

    public Object peekObject(int row, String col, int buffer) {
        Integer c = columnNames.get(col);
        return peekObject(row, c, buffer);
    }

    /**
     * getObject
     * get col's data of current row
     *
     * @param col
     * @return Object
     */
    public Object getObject(int col) {
        if (col < 0 || col > columnIndex.size()) return null;

        if (null == currentRecord) return null;
        Object obj = currentRecord.get(col, CURRENTVALUE);
        if (JDBCType.isNULL == obj) return null;

        return obj;
    }

    /**
     * getObject
     * get col's data of current row
     *
     * @param col
     * @return Object
     */
    public Object getObject(String col) {
        int pos = columnNames.get(col.toUpperCase());
        return getObject(pos);
    }

    /**
     * scroll To the Row position
     *
     * @param pos
     */
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

    public Vector getRowCopy(int row) {
        dataRow data = rows.get(row);
        Vector val = new Vector();
        for (int c = 0; c < getColumnCount(); c++) {
            val.add(data.get(c, CURRENTVALUE));
        }
        return val;
    }

    public Object getRowData(int pos) {
        if (rows.size() == 0 || pos < 0 || pos >= rows.size()) {
            return null;
        }
        return rows.get(pos);
    }

    /**
     * scroll.
     * scroll to the position relaive to current row
     * pos may < 0;
     *
     * @param pos
     */
    public void scroll(int pos) {
        scrollToRow(currentRow + pos);
    }

    /**
     * clearUpdate.
     * <p/>
     * Clears all row and col update flags, dos not resetIndex data
     * to pre-update state
     */
    public void clearUpdate() {
        for (int i = 0; i < this.getRowCount(); i++) {
            rows.get(i).clearUpdate();
        }
        deletedRows = null;
    }

    /**
     * clearUpdate.
     * <p/>
     * Clears all col update flags from current row
     *
     * @param row
     */
    public void clearUpdate(int row) {
        rows.get(row).clearUpdate();
    }

    /**
     * clear, resetIndex datastore.
     * clear internal contents
     * allow this object to be reused
     */
    public void reset() {
        clear();
        columnValidater.clear();
        columnNames.clear();
        columnIndex.clear();
        columnTypes.clear();
        columnLength.clear();
        defaltValues.clear();
        currentRow = 0;
    }

    // clear only clear datas but Keepp data Structor
    public void clear() {
        rows.removeAllElements();
    }

    public String toHTML() {
        int crow;
        String col;
        Object val;

        StringBuffer sb = new StringBuffer();
        crow = currentRow;

        for (int r = 0; r < this.getRowCount(); r++) {
            scrollToRow(r);
            for (int c = 0; c < this.getColumnCount(); c++) {
                col = this.getColumnName(c);
                val = this.getObject(c);
                // .....
                //sb.append("")
            }
        }

        scrollToRow(crow);
        return sb.toString();
    }

    public int find(String col, Object value) {
        int pos;

        Integer o = columnNames.get(col);
        if (null != o) {
            pos = o;
        } else {
            return -1;
        }
        return find(pos, value);
    }

    public void BackupToFile(String fileName) throws IOException {
        for (int r = 0; r < this.getRowCount(); r++) {
            Utilities.String2File(rows.get(r).getBackupScript(), fileName, true);
        }
    }

    public int find(int col, Object value) {
        dataRow Record;
        Object val;
        for (int r = 0; r < this.getRowCount(); r++) {
            //Record = (dataRow) rows.get(currentRow);
            Record = rows.get(r);
            val = Record.get(col, CURRENTVALUE);
            if (value.equals(val)) return r;
        }
        return -1;
    }

    /**
     * setReadOnlyCol.
     * <p/>
     * protect a column from be changed.
     *
     * @param col
     * @param readOnly
     */
    public void setReadOnlyCol(int col, boolean readOnly) {
        if (readOnly || this.readOnly) {
            if (!readonlyCols.contains(new Integer(col))) {
                readonlyCols.add(col);
            }
        } else {
            readonlyCols.remove(new Integer(col));
        }
    }

    public boolean isReadonlyCol(int col) {
        boolean debig = readOnly || readonlyCols.contains(col);
        return readOnly || readonlyCols.contains(col);
    }

    /**
     * test and debug only
     */
    public void printout() {
        String col;
        Object val;
        int maxColNameLen = 0;
        int maxColTypeLen = 0;
        int l = 0;
        // Prepae for good format
        for (int c = 0; c < this.getColumnCount(); c++) {
            l = this.getColumnName(c).length();
            maxColNameLen = (l > maxColNameLen ? l : maxColNameLen);

            l = colTypeString(c).length();
            maxColTypeLen = (l > maxColTypeLen ? l : maxColTypeLen);
        }
        maxColTypeLen += 2;
        maxColNameLen += 2;

        if (null != deletedRows) {
            // System.out.println("");
            // System.out.println("");
            // System.out.println("Data    RecordTitle = " + Name);
            // System.out.println("Total   rows = " + (this.getRowCount() + deletedRows.getRowCount()));
            // System.out.println("Deleted rows = " + deletedRows.getRowCount());
            deletedRows.printout();
            // System.out.println("");
            // System.out.println("");
        }


        // System.out.println("***********************************");
        // System.out.println(" ");
        // System.out.println("Column Count:" + this.getColumnCount());
        for (int c = 0; c < this.getColumnCount(); c++) {

//            System.out.print(
//                    uitils.fill(this.getColumnName(c), " ", maxColNameLen) +
//                            uitils.fill(colTypeString(c), " ", maxColTypeLen));

            listValidater(c);
            // System.out.println("");
        }
        // System.out.println(" ");
        // System.out.println("Row Count:" + this.getRowCount());

        for (int r = 0; r < this.getRowCount(); r++) {
            scrollToRow(r);
            // System.out.println("----------------------------");
            // System.out.println("Row: " + (r + 1));
            // System.out.println("JDBCRow: " + getJDBCRowNumber());

            // System.out.println("is New Row ?: " + this.isNewRow(r));
            // System.out.println("is Modified Row ?: " + this.isModified(r));
            // System.out.println("is Deleted Row ?: " + this.isDeletrow(r));
            // System.out.println("");

            for (int c = 0; c < this.getColumnCount(); c++) {
                col = this.getColumnName(c);
                val = this.getObject(c);
//                System.out.println(uitils.fill(col, " ", 25) + " = " + val);
            }
        }
//        scrollToRow(crow);
    }

    private String colTypeString(int c) {
        int len = this.getColLength(c);
        return "  [" +
                getColTypeName(c) +
                "(" + len + ") : " +
                getColumnClass(c).getName() +
                "]";
    }

    private void listValidater(int col) {
        if (columnValidater.containsKey(new Integer(col))) {
            Vector v = columnValidater.get(new Integer(col));
            // System.out.print("  Validaters:{");
//            for (int i = 0; i < v.size(); i++) {
//                if (i > 0) // System.out.print("; ");
//                // System.out.print(((DataValidater) v.get(i)).name());
//            }
            // System.out.print("}");
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isDeletrow(int row) {
        return currentRecord.isDeletrow();
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUpdateScript() {
        if (null != currentRecord) {
            return currentRecord.getUpdateScript();
        }
        return "";
    }

    public String getLogString() {
        if (null != currentRecord) {
            return currentRecord.getLogString();
        }
        return "";
    }

    public boolean hasCol(String col) {
        return columnNames.get(col) != null;
    }

    public void setDefaltValues(String col, Object val) {
        if (hasColumnName(col)) {
            defaltValues.put(columnNames.get(col), val);
        }
    }

    public void setDefaltValues(int col, Object val) {
        defaltValues.put(col, val);
    }

    /**
     * row Class
     * used only within ToProxy
     */
    private class dataRow {
        int dbRowIndex = 0; // JDBC row
        Vector cols = new Vector();  // original
        Hashtable<Integer, Object> colsNewValue = new Hashtable<>();  // Modified cols and data
        private boolean isDeleteSet = false;


        public dataRow(int dbRowIndex) {
            this.dbRowIndex = dbRowIndex;
        }

        /**
         * @param data
         */
        void add(Object data) {
            cols.add((null == data ? JDBCType.isNULL : data));
        }

        void updateObject(Integer col, Object obj) {

            // if new data equals Original data
            if (cols.get(col) != null && cols.get(col).equals(obj)) {
                colsNewValue.remove(col);  // resetIndex to not modified
            } else {
                colsNewValue.put(col, (null == obj ? JDBCType.isNULL : obj));
            }
        }

        public int size() {
            return cols.size();
        }

        public void clear() {
            cols = new Vector();
        }

        /**
         * if data changed ,return changed data
         * else return original data
         * <p/>
         * CURRENTVALUE ;
         * MODIFIEDVALUE;
         * PRIMARYVALUE ;
         *
         * @param col
         * @return Object
         */

        public Object get(int col, int bufferType) {
            if (bufferType == CURRENTVALUE) {
                return getCurrent(col);
            } else if (bufferType == PRIMARYVALUE) {
                return getPrimary(col);
            } else {
                return getModified(col);
            }
        }

        private Object getCurrent(int col) {
            if (colsNewValue.containsKey(col))
                return colsNewValue.get(col);
            else
                return cols.get(col);
        }

        private Object getPrimary(int col) {
            return cols.get(col);
        }

        private Object getModified(int col) {
            if (colsNewValue.containsKey(col))
                return colsNewValue.get(col);
            else
                return JDBCType.isNULL;
        }

        /**
         * if any col changed, row is mark modified
         *
         * @return boolean
         */
        public boolean isModified() {
            return colsNewValue.size() > 0;
        }

        /**
         * if a col has new data, it modified
         *
         * @param col
         * @return boolean
         */
        public boolean isModified(int col) {
            if (!isModified()) return false;
            return colsNewValue.containsKey(col);
        }

        /**
         * rwos that JDBC row No == 0, are insert row
         *
         * @return boolean
         */
        boolean isNewRow() {
            return 0 == dbRowIndex;
        }

        /**
         * just remove any change
         */
        void clearUpdate() {
            for(Integer i : colsNewValue.keySet()){
                cols.set(i,colsNewValue.get(i));
            }
            colsNewValue = new Hashtable();
        }


        public int getJDBCRowNumber() {
            return dbRowIndex;
        }

        boolean isDeletrow() {
            return isDeleteSet;
        }

        // this method is private !
        void setDeletrow(boolean deletrow) {
            isDeleteSet = deletrow;
        }

        String getLogString() {
            StringBuffer script = new StringBuffer();
            String nLine = "\r\n";
            if (isDeletrow()) {
                script.append("DELETE FROM " + nameEncode(getUpdateTable()) + nLine);
                script.append(columnNames());
                script.append(nLine);
                script.append(originalValues());
            } else if (isNewRow()) {
                script.append("INSERT INTO ").append(nameEncode(getUpdateTable())).append(nLine);
                script.append(columnNames());
                script.append(nLine);
                script.append(newValues());
            } else if (isModified()) {
                script.append("UPDATE ").append(nameEncode(getUpdateTable())).append(nLine);
                script.append(columnNames());
                script.append(nLine);
                script.append(originalValues());
                script.append(nLine);
                script.append(newValues());
            }
            return script.toString();
        }

        private StringBuffer originalValues() {
            StringBuffer script = new StringBuffer();
            for (int i = 0; i < getColumnCount(); i++) {
                if (i > 0) script.append("\t");
                script.append(cols.get(i) == JDBCType.isNULL ? "" : cols.get(i));
            }
            return script;
        }

        private StringBuffer columnNames() {
            StringBuffer script = new StringBuffer();
            for (int i = 0; i < getColumnCount(); i++) {
                if (i > 0) script.append("\t");
                script.append(getColumnName(i));
            }
            return script;
        }

        private StringBuffer newValues() {
            StringBuffer script = new StringBuffer();
            for (int i = 0; i < getColumnCount(); i++) {
                if (i > 0) script.append("\t");
                Object val = (colsNewValue.containsKey(new Integer(i)) ? colsNewValue.get(new Integer(i)) : "");
                script.append(val);
            }
            return script;
        }

        String getBackupScript() {
            StringBuffer script = new StringBuffer();
            script.append("INSERT IGNORE INTO " + nameEncode(getUpdateTable()) + " ");
            script.append(insertValues());
            return script.toString();
        }

        /**
         * getUpdateScript()
         * <p/>
         * for SQL data Only ,
         * return sql update, delete, insert script for this row
         * we use this for "Log To DB" too
         *
         * @return String
         */
        String getUpdateScript() {
            StringBuffer script = new StringBuffer();

            if (isDeletrow()) {
                script.append("DELETE FROM " + nameEncode(getUpdateTable()) + " WHERE ");
                script.append(whereClause());
            } else if (isNewRow()) {
                script.append("INSERT INTO " + nameEncode(getUpdateTable()) + " ");
                script.append(insertValues());
            } else if (isModified()) {
                script.append("UPDATE " + nameEncode(getUpdateTable()) + " SET ");
                script.append(updateValues());
                script.append(" WHERE ");
                script.append(whereClause());
            }
            return script.toString();
        }

        private String dataQuotes(int col) {
            //SqlJavaTypeMaper.SqlToJavaName(getColTypeName(col))

            if (("java.lang.String".equals(getColumnClass(col).getName()) ||
                    "java.util.Date".equals(getColumnClass(col).getName()) ||
                    "java.sql.Date".equals(getColumnClass(col).getName()))
                    && JDBCType.isNULL != this.get(col, CURRENTVALUE))

                return "\'";
            else
                return "";
        }

        private StringBuffer insertValues() {
            StringBuffer names = new StringBuffer();
            StringBuffer vals = new StringBuffer();
            String quotes;

            names.append("( ");
            vals.append(" VALUES ( ");
            int count = 0;
            boolean updateColDefined = updateCols.size() > 0;
            for (int i = 0; i < getColumnCount(); i++) {
                if (updateColDefined && !updateCols.contains(i)) {
                    continue;
                }

                if (getObject(i) != null) {
                    if (count > 0) {
                        vals.append(", ");
                        names.append(", ");
                    }
                    quotes = dataQuotes(i);
                    names.append(getOriginalName(i)); //getColumnName
                    vals.append(quotes);
                    if (getObject(i) instanceof Date) {
                        Date d = (Date) getObject(i);
                        String dbg = new MyDate(d).getDate();
                        vals.append(dbg);
                    } else {
                        vals.append(getObject(i).toString());
                    }
                    vals.append(quotes);
                    count++;
                }
            }
            names.append(" )");
            vals.append(" )");
            return names.append(vals);
        }


        private StringBuffer updateValues() {
            StringBuffer script = new StringBuffer();
            String quotes;
            int count = 0;
            boolean updateColDefined = updateCols.size() > 0;
            for (Enumeration e = colsNewValue.keys(); e.hasMoreElements(); ) {
                Integer col = (Integer) e.nextElement();
                if (updateColDefined && !updateCols.contains(col))
                    continue;


                quotes = dataQuotes(col);
                if (count > 0) script.append(", ");
                script.append(getOriginalName(col)); //getColumnName()
                script.append(" = ");
                script.append(quotes);

                //
                if (colsNewValue.get(col) instanceof Date) {
                    Date d = (Date) colsNewValue.get(col);
                    String dbg = new MyDate(d).getDate();
                    script.append(dbg);
                } else {
                    script.append(colsNewValue.get(col).toString());
                }
                script.append(quotes);
                count++;
            }
            return script;
        }


        /**
         * get data from original data set
         *
         * @return StringBuffer
         */
        private StringBuffer whereClause() {
            StringBuffer script = new StringBuffer();
            String quotes;
            int count = 0;
            boolean updateUseKey = updateKeyCols.size() > 0;

            for (int i = 0; i < getColumnCount(); i++) {
                if (updateUseKey && !updateKeyCols.contains(new Integer(i)))
                    continue;

                quotes = dataQuotes(i);
                if (count > 0) script.append(" and ");
                script.append(getOriginalName(i)); //getColumnName
                script.append(cols.get(i) == JDBCType.isNULL ? " is " : " = ");
                script.append(quotes);
                if (cols.get(i) instanceof Date && cols.get(i) != JDBCType.isNULL) {
                    Date d = (Date) cols.get(i);
                    String dbg = new MyDate(d).getDate();
                    script.append(dbg);
                } else {
                    script.append(cols.get(i).toString());
                }
                script.append(quotes);
                count++;
            }
            return script;
        }

        private String nameEncode(String name) {
            name = name.replace(',', ' ').trim();
            if (name != null && name.indexOf(' ') > -1)
                name = "\"" + name + "\"";
            return name;
        }

        void reset() {
            int dbRowIndex = 0;
            cols.clear();
            colsNewValue.clear();
            isDeleteSet = false;
        }
    }

}
