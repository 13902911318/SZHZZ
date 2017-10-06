package szhzz.sql.gui;


import szhzz.sql.database.*;
import szhzz.App.AppManager;
import szhzz.Files.TextTransfer;
import szhzz.Table.MatrixTable;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.Utilities;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * DataWindow.
 * <p/>
 * TODO TBD
 * DataWindow 是 ToProxy 的一种可视化界面，衍生于JTable
 * 建立 DataWindow 时应提供一个有效的 Database 作为参数
 * DataWindow(Database db)。然后可以用 retrive(String szhzz.sql)
 * 获取并显示数据库的数据。DataWindow 可以是只读的，这时用户不可以修改
 * DataWindow 的数据。
 * <p/>
 * DataWindow 可以针对个别字段设置只读特性，这时这些字段将拒绝被修改。
 * <p/>
 * DataWindow 利用 ToProxy 的一些来自数据库的属性修饰 JTable
 * <p/>
 * DataWindow 废弃时自动关闭 GeneralDAO dao，但不会关闭相应的 Database
 * <p/>
 * 引用的 TableSorter.java 用于提供字段抬头排序服务，来自网站：
 * http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
 * <p/>
 * Tec Reference
 * http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
 *
 * @author John
 * @version 1.0
 * @see DbComboBox
 * @see DwPanel
 * <p/>
 * <p>Title: INFO2820</p>
 * <p/>
 * <p>Description: home work INFO2820</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p/>
 * <p>Company: </p>
 */


public class DataWindow extends JTable {
    TextTransfer ClipboardReader = new TextTransfer();
    DataWindow thisWin;
    private Database db = null;
    private DBProperties prop = null;
    private String sql = null;
    private DataStore ds = null;
    private GeneralDAO dao = null;
    private boolean readOnly = true;
    private Vector redonlyCols = new Vector();
    private dbTableModel dm = null;
    private MapedTableModel activeTableModel = null;
    private Hashtable cellRenderer = new Hashtable();
    private Hashtable cellEditors = new Hashtable();
    private Hashtable EditorListeners = new Hashtable();
    private TableFilter tableFilter;
    private TableSorter tableSorter;
    private TableSorter udfTableSorter;
    private Vector ModelListeners = new Vector();
    private Vector UpdateListeners = new Vector();
    private int visibleCols[] = {};
    private boolean filterNewRow;                     //@see  TableFilter
    private Filter activeFilter = new Filter();
    private Vector UpdateKeys = new Vector();
    private Vector updateCols = new Vector();
    private String updateTable = null;
    private boolean log2DB = true;
    private ColumnLock columnLocker = null;
    private boolean isShare = false;
    private int CurrentRow = 0;
    private boolean autoScroll = true;
    private PropertyChangeListener editorRemover = null;
    private String[] popUpTreeMenus = new String[]{"拷贝字段内容", "拷贝行内容", "拷贝表格内容", "拷贝表格内容(报表)",
            "---", "黏贴(替换)", "黏贴(添加)"
    };
    private ArrayList<ActionListener> userMenuItem = new ArrayList<ActionListener>();
    private ArrayList<String> userMenuTitle = new ArrayList<String>();
    private IdleTimer idleTimer = null;

    public DataWindow() {
        super();
        setRowHeight(20);
        thisWin = this;
        // Debug only: Auto tester
        addsRowChangedListener(new rowsDebug());
        setSurrendersFocusOnKeystroke(true);
        addMouseListener(new PopClickListener());

    }

    public DataWindow(Database db) {
        super();
        setRowHeight(20);

        // Debug only: Auto tester
        addsRowChangedListener(new rowsDebug());
        setTranscatObject(db);
    }

//    public void setModel(TableModel dataModel) {
//        super.setModel(getActiveTableModel(dm));
//    }

    public void setTranscatObject(Database db) {
//        if(this.db != null)this.db.close();
        this.db = db;
        prop = this.db.getDBProperties();
    }

    public boolean hasColumn(String col) {
        return dm != null && dm.colMap(col) > -1;
    }
//    public void setDataModel(dbTableModel dm) {
//        this.dm = dm;
//    }

    public void setColumnWidth(int col, int width) {
        javax.swing.table.TableColumnModel tcm = getColumnModel();
        javax.swing.table.TableColumn tc = tcm.getColumn(col);
        tc.setPreferredWidth(width);
    }

    public int getColumnWidth(int col) {
        javax.swing.table.TableColumnModel tcm = getColumnModel();
        javax.swing.table.TableColumn tc = tcm.getColumn(col);
        return tc.getWidth();
    }

    public Object getValueAt(int row, String col) {
        return getValueAt(convertRowIndexToModel(row), convertColumnIndexToView(dm.colMap(col)));
        //return getValueAt(convertRowIndexToModel(row),dm.colMap(col));

//        return dm.getValueAt(convertRowIndexToModel(row), col);
    }


    public void setValueAt(Object o, int row, String col) {
        setValueAt(o, convertRowIndexToModel(row), convertColumnIndexToView(dm.colMap(col)));
    }

    public void setFilterNewRow(boolean filterNewRow) {
        this.filterNewRow = filterNewRow;
    }

    public int retrive(String sql) throws DBException {
        return retrive(sql, false);
    }

    public int retrive(String sql, boolean datachanges) throws DBException {
        if (!datachanges && sql != null && sql.equals(this.sql)) {
            return refresh();
        }
        this.sql = sql;
        return retrive();
    }

    public int retrive(boolean repaint) throws DBException {
        if (!checkInit()) return -1;
        isShare = false;

        try {
            if (!db.isOpened()) db.openDB();
            if (!db.isOpened()) return -1;

            //Clear interface
            removeEditor();
            this.setModel(new dbTableModel());

            //Discate currewnt Dao
            if (dao != null) dao.close();

            dao = (GeneralDAO) db.getDAO(sql, readOnly);
            dao.retriev();


            // Protect cell from edit. the call path as below:
            // Jtable->DataWindow->dbTabbleModel.isCellEditable(currentRow, col)->ds.isReadonlyCol(col)
            ds = dao.getDataStore();
            setDBUpdateProperties();

            // Linkup all mapedDataModel, ie. filter, sorter...
            dm = new dbTableModel();
            dm.setDataStore(ds);

            setVisibleCols();
            this.setModel(getActiveTableModel(dm));

            setTableModelListener();         // CheckData lines chan not change orders

            if (repaint) {
                repaintDataWindow();
            }
            setupCellEditors();
            setCellEditorListeners();
            setUpdateListener();

            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


//            for (int i = 0; i < ds.getColumnCount(); i++) {
//                TableColumn col = getColumnModel().getColumn(i);
//                col.setPreferredWidth(ds.getColLength(i) * 9);
//            }
            if (autoScroll) scrollToRow(0);

        } catch (DBException e) {
            // util.loger.getLoger().errorOut(e);
            throw e;
        } finally {
//            db.close();
        }
        //if(null != jLabelStatus) jLabelStatus.setText("Retriev " + ds.getRowCount() + " rows");
        return ds.getRowCount();
    }

    public int retrive() throws DBException {
        return retrive(true);
    }

    public void repaintDataWindow() {
        if (dm != null) {
            setVisibleCols();
            dm.fireTableStructureChanged();
            dm.fireTableDataChanged();
        }
    }

    public String getDataString(String rowSeperator) {
        StringBuffer sb = new StringBuffer();
        TableModel model = this.getModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (i > 0) sb.append(rowSeperator);
            sb.append(model.getColumnName(i));
        }
        sb.append('\n');
        for (int r = 0; r < model.getRowCount(); r++) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (i > 0) sb.append(rowSeperator);
                sb.append(model.getValueAt(r, i));
            }
            sb.append('\n');
        }
        return sb.toString();
    }


    public void shareData(DataStore ds) throws DBException {
        shareData(ds, true);
    }

    public void shareData(DataStore ds, boolean readOnly) throws DBException {
        this.readOnly = readOnly;
        isShare = true;

        this.ds = ds;
        //addsRowChangedListener(new rowsDebug());

        dm = new dbTableModel();
        dm.setDataStore(ds);

        setVisibleCols();
        this.setModel(getActiveTableModel(dm));

        setTableModelListener();         // CheckData lines chan not change orders

        dm.fireTableStructureChanged();
        dm.fireTableDataChanged();

        setupCellEditors();
        setCellEditorListeners();
        setUpdateListener();

//        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        if (this.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF) {
            for (int i = 0; i < ds.getColumnCount(); i++) {
                TableColumn col = getColumnModel().getColumn(i);
                col.setPreferredWidth(ds.getColLength(i) * 9);
            }
        }

        if (autoScroll) scrollToRow(0);
    }

    public void addData(DataStore ds, boolean repaint) throws DBException {
        this.readOnly = true;
        isShare = true;

        this.ds.addAll(ds);
        if (repaint) dm.fireTableDataChanged();
    }

    public void clear() throws DBException {
        if (ds != null) ds.clear();
        repaintDataWindow();
    }

    public int refresh() throws DBException {
        if (isShare) {
            dm.fireTableDataChanged();
            return ds.getRowCount();
        }


        if (!checkInit()) return -1;
        try {
            int currentRow = this.getSelectedRow();

            if (!db.isOpened())
                db.openDB();
            if (!db.isOpened()) return -1;
            removeEditor();

            if (dao == null) {
                dao = (GeneralDAO) db.getDAO(sql, readOnly);
            } else {
                if (dao.getDataStore() != null) dao.getDataStore().clear();
            }

            dao.retriev();
            dm.fireTableDataChanged();

            ds = dao.getDataStore();
            dm.setDataStore(ds);
            dm.fireTableDataChanged();

            //if(ds.getRowCount() > currentRow) currentRow = 0;
            if (autoScroll) scrollToRow(currentRow);
        } catch (DBException e) {
            // util.loger.getLoger().errorOut(e);
            throw e;
        } finally {
//            db.close();
        }
        //if(null != jLabelStatus) jLabelStatus.setText("Retriev " + ds.getRowCount() + " rows");
        return ds.getRowCount();
    }

    public int getCurrentRow() {
        return CurrentRow;
    }

    public void setSQL(String sql) {
        if (sql != null && !sql.equals(this.sql) && dao != null) {
            dao.close();
            dao = null;
        }
        // if szhzz.sql == null no retrive wile be executed
        this.sql = sql;
    }

//    public int retrive(String szhzz.sql) throws DBException {
//        if (! checkInit()) return -1;
//        return refresh();
//    }

    public int appendRow() {
        if (isDataWindowReadOnly()) return -1;
        int row = ds.appendRow();

        // Auto Fill columns
        int colNumber;
        if (tableFilter.hasAutoFill()) {
            Hashtable af = tableFilter.getAutoFillCols();
            for (Enumeration e = af.keys(); e.hasMoreElements(); ) {
                Object col = e.nextElement();
                if (col instanceof Integer) {
                    colNumber = ((Integer) col).intValue();
                } else {
                    colNumber = ds.getColIndex(col.toString());
                }
                Object val = af.get(col);
                ds.updateObject(colNumber, val);         // data is modified
            }
        }
        dm.fireTableDataChanged();
        if (activeTableModel != null) {
            row = activeTableModel.getViewRow(row);
        }
        return row;
    }

    public void appendFirstRow() {
        if (isDataWindowReadOnly()) return;
        ds.appendFirstRow();

        // Auto Fill columns
        int colNumber;
        if (tableFilter.hasAutoFill()) {
            Hashtable af = tableFilter.getAutoFillCols();
            for (Enumeration e = af.keys(); e.hasMoreElements(); ) {
                Object col = e.nextElement();
                if (col instanceof Integer) {
                    colNumber = ((Integer) col).intValue();
                } else {
                    colNumber = ds.getColIndex(col.toString());
                }
                Object val = af.get(col);
                ds.updateObject(colNumber, val);         // data is modified
            }
        }
        dm.fireTableDataChanged();
    }

    public boolean isDirty() {
        if (null != ds) {
            return ds.isDirty();
        }
        return false;
    }

    private void setDBUpdateProperties() {
        setUpdateKey();
        setUpdateCol();
        if (null != updateTable) ds.setUpdateTable(updateTable);
        for (int i = 0; i < redonlyCols.size(); i++) {
            ds.setReadOnlyCol(((Integer) redonlyCols.get(i)).intValue(), true);
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
    public void setUpdateKey(int col) {
        UpdateKeys.add(new Integer(col));
        if (null != ds) ds.setUpdateKey(new Integer(col));
    }

    public void setUpdateKey(String col) {
        UpdateKeys.add(col);
        if (null != ds) ds.setUpdateKey(col);
    }

    private void setUpdateKey() {
        for (Object UpdateKey : UpdateKeys) {
            if (UpdateKey instanceof String)
                ds.setUpdateKey((String) UpdateKey);
            else
                ds.setUpdateKey((Integer) UpdateKey);
        }
    }

    public void setUpdateTable(String updateTable) {
        this.updateTable = updateTable;
        if (null != ds) ds.setUpdateTable(updateTable);
    }


    public void setUpdateCol(int col) {
        updateCols.remove(new Integer(col));
        updateCols.add(new Integer(col));
        if (null != ds) ds.setUpdateCol(col);
    }

    public void setUpdateCol(String col) {
        updateCols.remove(col);
        updateCols.add(col);
        if (null != ds) ds.setUpdateCol(col);
    }

    private void setUpdateCol() {
        for (int i = 0; i < updateCols.size(); i++) {
            if (updateCols.get(i) instanceof String)
                ds.setUpdateCol((String) updateCols.get(i));
            else
                ds.setUpdateCol(((Integer) updateCols.get(i)).intValue());
        }
    }

    public void addTableModelListener(TableModelListener Listener) {
        ModelListeners.removeElement(Listener);
        ModelListeners.add(Listener);
    }

    public void addUpdateListener(UpdateListener ul) {
        UpdateListeners.remove(ul);
        UpdateListeners.add(ul);
    }

    void setUpdateListener() {
        for (int i = 0; i < UpdateListeners.size(); i++) {
            UpdateListener ul = (UpdateListener) UpdateListeners.get(i);
            dao.addUpdateListener(ul);
        }
    }

    void setTableModelListener() {
        for (int i = 0; i < ModelListeners.size(); i++) {
            TableModelListener tl = (TableModelListener) ModelListeners.get(i);
            if (null != tl)
                activeTableModel.addTableModelListener(tl);
        }
    }


    protected boolean checkInit() {
        if (db == null) {
//            loger.getLoger().errorOut(new Exception("Transact object not defined!"));
            return false;
        }
        return sql != null;
    }

    /**
     * find currentRow where col = :value
     * <p/>
     * NOTE: find() return currentRow number for ToProxy ds, not this DataWindow's maped currentRow
     *
     * @param col
     * @param value
     * @return currentRow >= 0 if found, else return -1
     */
    public int find(String col, Object value) {
        int pos;
        if (ds == null) return -1;

        pos = ds.getColIndex(col);
        if (pos >= 0) {
            return find(pos, value);
        }
        return -1;
    }

    /**
     * find currentRow where col = :value
     * NOTE: find() return currentRow number for ToProxy ds, not this DataWindow's maped currentRow
     *
     * @param col
     * @param value
     * @return currentRow >= 0 if found, else return -1
     */
    public int find(int col, Object value) {
        //return ds.find(col, value);

        if (value == null) return -1;
        TableModel tm = this.getModel();
        int rows = tm.getRowCount();
        for (int i = 0; i < rows; i++) {
            if (value.equals(tm.getValueAt(i, col))) {
                return i;
            }
        }
        return -1;
    }

    public Point findNext(Object value) {
        if (value == null) return null;
        String compString = value.toString();

        TableModel tm = this.getModel();
        int rows = tm.getRowCount();
        int cols = tm.getColumnCount();
        for (int r = this.getCurrentRow() + 1; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (tm.getValueAt(r, c) != null && tm.getValueAt(r, c).toString().contains(compString)) {
                    return new Point(r, c);
                }
            }
        }
        return null;
    }

    /**
     * Apply modfied data to database
     * <p/>
     *
     * @return modified and added rows count
     * @throws DBException
     */
    public int update() throws DBException {
        int updateRows;
        //if (isDataWindowReadOnly()) return 0;

        updateRows = dao.applyUpdates();
        if (updateRows > 0) {
            //loger.getLoger().messgeBox("Update Success!", "Update " + updateRows + " rows.");
            this.refresh();               // retrieve and repaint
        } else {
            //loger.getLoger().messgeBox("No rows Updated", "Update " + updateRows + " rows.");
        }

        return updateRows;
    }

    public int executeUpdate(String sql) throws DBException {
        return dao.executeUpdate(sql);
    }

    /**
     * Apply delete currentRow imediactly to database
     *
     * @param row
     * @throws DBException
     */
    public void deleteRow(int row) throws DBException {
        ds.deleteRow(getOraRow(row));
        dm.fireTableDataChanged();
    }


    /**
     * Invoked when editing is finished. The changes are saved and the
     * editor is discarded.
     * <p/>
     * Application code will not use these methods explicitly, they
     * are used internally by JTable.
     * <p/>
     * Added data validation by John
     *
     * @param e the event received
     * @see javax.swing.event.CellEditorListener
     */
    public void editingStopped(ChangeEvent e) {
        // Take in the new value
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            Object value = editor.getCellEditorValue();

            //John: Added validator on this overided method
            if (dm.isValidate(editingColumn, value)) {
                setValueAt(value, editingRow, editingColumn);
                removeEditor();
//                triggerCellEditorListeners(editingRow, convertColumnIndexToModel(editingColumn));
            }
        }
    }


    private void setVisibleCols() {
        if (visibleCols.length > 0) dm.setVisibleCols(visibleCols);
    }

    public void setVisibleCols(int cols[]) {
        visibleCols = cols;
    }

    public void resetVisibleCols(int cols[]) {
        visibleCols = cols;

        dm = new dbTableModel();
        dm.setDataStore(ds);

        //*********************
        setVisibleCols();
        this.setModel(getActiveTableModel(dm));

        setTableModelListener();         // CheckData lines chan not change orders

        dm.fireTableStructureChanged();
        dm.fireTableDataChanged();

        setupCellEditors();
        setCellEditorListeners();
        setUpdateListener();

//        //*****************

//        repaintDataWindow();

    }

    public boolean isNewRow(int row) {
        return ds.isNewRow(getOraRow(row));
    }

    public boolean isModified(int row) {
        return ds.isModified(getOraRow(row));
    }

    public boolean isDataWindowReadOnly() {
        return readOnly;
    }

    public void setDataWindowReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }


    public void setColReadOnly(int col, boolean readOnly) {
        //ds.setReadOnlyCol(col, readOnly);
        if (readOnly) {
            if (!redonlyCols.contains(new Integer(col))) {
                redonlyCols.add(new Integer(col));
            }
        } else {
            redonlyCols.remove(new Integer(col));
        }
    }

    public int getColIndex(String col) {
        return ds.getColIndex(col);
    }

    public void scrollToRow(int row) {
        if (row < 0 || row >= this.getRowCount()) return;

        Rectangle rect = getCellRect(row, 0, true);
        scrollRectToVisible(rect);
        clearSelection();
        setRowSelectionInterval(row, row);
    }

    /* Section:  add/change and apply cell Renderer and CellEditor **/

    public void addCellRenderer(int col, TableCellRenderer tcr) {
        cellRenderer.put(new Integer(col), tcr);
    }

    public void addCellEditor(int col, TableCellEditor ce) {
        cellEditors.put(new Integer(col), ce);
    }

    private void setupCellEditors() {
        // restrict string column's edit length,
        // may be overided by user or following setEditor.
//        for(int i = 0; i < ds.getColumnCount(); i++){
//            if(dm.getColumnClass(i).getName().equals("java.lang.String")){
//                JTextField textField = new JTextField();
//                textField.setColumns(ds.getColLength(i));
//                getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(textField));
//            }
//        }

        for (Enumeration e = cellRenderer.keys(); e.hasMoreElements(); ) {
            Integer index = (Integer) e.nextElement();
            getColumnModel().getColumn(index.intValue()).setCellRenderer((TableCellRenderer) cellRenderer.get(index));
        }
        for (Enumeration e = cellEditors.keys(); e.hasMoreElements(); ) {
            Integer index = (Integer) e.nextElement();
            getColumnModel().getColumn(index.intValue()).setCellEditor((TableCellEditor) cellEditors.get(index));
        }
    }

    public void addCellEditorListener(int col, CellEditorListener lt) {
        EditorListeners.put(new Integer(col), lt);
    }

    private void setCellEditorListeners() {
        for (Enumeration e = EditorListeners.keys(); e.hasMoreElements(); ) {
            Integer index = (Integer) e.nextElement();
            TableCellEditor tce = getColumnModel().getColumn(index.intValue()).getCellEditor();
            if (null != tce) {
                tce.addCellEditorListener((CellEditorListener) EditorListeners.get(index));
            }
        }
    }

//    public void triggerCellEditorListeners(int currentRow, int col) {
//        for (Enumeration e = EditorListeners.keys(); e.hasMoreElements();) {
//            Integer index = (Integer) e.nextElement();
//            if (index.intValue() == col) {
//                CellEditorListener ce = (CellEditorListener) EditorListeners.get(index);
//                Hashtable parm = new Hashtable();
//                parm.in("currentRow", new Integer(currentRow));
//                parm.in("col", new Integer(col));
//                ChangeEvent event = new ChangeEvent(parm);
//                ce.editingStopped(event);
//                return;
//            }
//        }
//    }

//    public void addCellEditor(int col, Object editor) throws DBException {
//        TableColumn sportColumn = getColumnModel().getColumn(col);
//        if (editor instanceof JTextField)
//            sportColumn.addCellEditor(new DefaultCellEditor((JTextField) editor));
//        else if (editor instanceof JCheckBox)
//            sportColumn.addCellEditor(new DefaultCellEditor((JCheckBox) editor));
//        else if (editor instanceof JComboBox)
//            sportColumn.addCellEditor(new DefaultCellEditor((JComboBox) editor));
//        else {
//            throw new DBException(editor.getClass().getName() +
//                    " can not set as a cell editor", DBException.UNKNOWN);
//        }
//    }


    /**
     * release all resource
     */
    private void reset() {
        removeEditor();
        if (dao != null) {
            dao.close();
            dao = null;
//            dm = null;
//            ds = null;
        }
    }

    public void setColumnLocker(ColumnLock columnLocker) {
        this.columnLocker = columnLocker;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        reset();
    }

    public void setLog2DB(boolean log2DB) {
        this.log2DB = log2DB;
        setLog2DB();
    }

    public void cancelSorting() {
        if (tableSorter != null) tableSorter.cancelSorting();
    }

    public void setSortingStatus(int column, int status) {
        if (tableSorter != null) tableSorter.setSortingStatus(column, status);
    }

    public void setSortingStatus(String column, int status) {
        if (tableSorter != null) tableSorter.setSortingStatus(dm.colMap(column), status);
    }

    public void setTableSorter(TableSorter tableSorter) {
        this.udfTableSorter = tableSorter;
    }

    private void setLog2DB() {
        if (null != dao) dao.setLog2DB(log2DB);
    }
    // Addtional services

//    /**
//     * @param model
//     */
//    public void addMapDataModel(MapedTableModel model) {
//        mapDataModels.add(model);
//    }

//    TableModel getActiveTableModel(dbTableModel dm) {
//        activeTableModel = dm;
//        MapedTableModel model;
//        for (int i = 0; i < mapDataModels.size(); i++) {
//            model = (MapedTableModel) mapDataModels.get(i);
//            model.setTableModel(activeTableModel);
//            model.setTableHeader(getTableHeader());
//            activeTableModel = model;
//        }
//        return activeTableModel;
//    }


    TableModel getActiveTableModel(dbTableModel dm) {
//        dm.setTableHeader(getTableHeader());
//        activeTableModel = dm;

        tableFilter = new TableFilter();    //Empty Filter
        tableFilter.setTableModel(dm);
        tableFilter.setTableHeader(getTableHeader());
        tableFilter.setFilterNewRow(filterNewRow);
        tableFilter.setFilter(activeFilter);

        if (udfTableSorter == null) {
            tableSorter = new TableSorter();
        } else {
            tableSorter = udfTableSorter;
        }
        tableSorter.setTableModel(tableFilter);
        tableSorter.setTableHeader(getTableHeader());

        activeTableModel = tableSorter;
        activeTableModel.setColumnLocker(columnLocker);
        return activeTableModel;
    }


    public void setFilter(Filter ft) {
        if (ft == null) ft = new Filter();
        activeFilter = ft;
        if (null != tableFilter) {
            tableFilter.setFilter(ft);
            dm.fireTableDataChanged();
        }
    }

    public void Filte() {
        if (dm != null) dm.fireTableDataChanged();
    }

    public DataStore getDatastore() {
        if (ds == null) ds = new DataStore();
        return ds;
    }

    public int getOraRow(int row) {
        return activeTableModel.getOraRow(row);
    }


    // Row selection Section

    /**
     * @param rowChangedListener
     */
    public void addsRowChangedListener(DWRowChanged rowChangedListener) {
        rowChangedListener.setDataWindow(this);
        addListSelectionListener(rowChangedListener);
    }


    /**
     * @param ls
     */
    public void addListSelectionListener(ListSelectionListener ls) {
        ListSelectionModel rowSM = getSelectionModel();
        rowSM.addListSelectionListener(ls);
    }

    public String saveAs(String colSeperator, String fileName) throws IOException {
        String selectedFile = null;
        if (colSeperator == null) {
            colSeperator = "\t";
        }
        if (fileName == null) {
            fileName = "Untitled" + File.separator + "txt";
            JFileChooser chooser = new JFileChooser(new File(fileName));
            chooser.showSaveDialog(this);
            // Get the selected file
            File f = chooser.getSelectedFile();
            if (f != null) selectedFile = f.getAbsolutePath();
        } else {
            selectedFile = fileName;
        }

        if (selectedFile != null) {
            String data = getDataString(colSeperator);
            Utilities.String2File(data, selectedFile, false);
        }
        return selectedFile;
    }

    public String saveAsTextReport(String title, String fileName) throws IOException {
        String selectedFile = null;
        if (fileName == null) {
            fileName = "Untitled" + File.separator + "txt";
            JFileChooser chooser = new JFileChooser(new File(fileName));
            chooser.showSaveDialog(this);
            File f = chooser.getSelectedFile();
            if (f != null) selectedFile = f.getAbsolutePath();
        } else {
            selectedFile = fileName;
        }

        if (selectedFile != null) {
            Utilities.String2File(getTextReport(title), selectedFile, false);
        }
        return selectedFile;
    }

    public String getTextReport(String Title) {
        return new TableFormaterWriter(Title, this.getModel()).getString();
    }

    public String getCsvReport(String Title, String separator) {
        return new TableFormaterWriter(Title, this.getModel()).getCsvString(separator);
    }

    public String getCsvReport(String Title) {
        return getCsvReport(Title, "\t");
    }

    public void clearUpdate() {
        ds.clearUpdate();
    }

    public boolean isAutoScroll() {
        return autoScroll;
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    public DefaultTableCellRenderer getNumberCellRenderer() {
        return new NumberCellRenderer();
    }

    public DefaultTableCellRenderer getNumberCellRenderer(String format) {
        NumberCellRenderer nf = new NumberCellRenderer();
        nf.setUdfFormat(format);
        return nf;
    }

    public AlignmentCellRenderer getAlignmentCellRenderer() {
        return new AlignmentCellRenderer();
    }

    /**
     * Programmatically starts editing the cell at <code>row</code> and
     * <code>column</code>, if those indices are in the valid range, and
     * the cell at those indices is editable.
     * To prevent the <code>JTable</code> from
     * editing a particular table, column or cell value, return false from
     * the <code>isCellEditable</code> method in the <code>TableModel</code>
     * interface.
     *
     * @param row    the row to be edited
     * @param column the column to be edited
     * @param e      event to pass into <code>shouldSelectCell</code>;
     *               TIG_statu that as of Java 2 platform v1.2, the call to
     *               <code>shouldSelectCell</code> is no longer made
     * @return false if for any reason the cell cannot be edited,
     * or if the indices are invalid
     */
    public boolean editCellAt(int row, int column, EventObject e) {
        if (cellEditor != null && !cellEditor.stopCellEditing()) {
            return false;
        }

        if (row < 0 || row >= getRowCount() ||
                column < 0 || column >= getColumnCount()) {
            return false;
        }

        if (!isCellEditable(row, column))
            return false;

        if (editorRemover == null) {
            KeyboardFocusManager fm =
                    KeyboardFocusManager.getCurrentKeyboardFocusManager();
            editorRemover = new CellEditorRemover(fm);
            fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);
        }

        TableCellEditor editor = getCellEditor(row, column);
        if (editor != null && editor.isCellEditable(e) && !readOnly) {
            editorComp = prepareEditor(editor, row, column);
            if (editorComp == null) {
                removeEditor();
                return false;
            }
            editorComp.setBounds(getCellRect(row, column, false));
            add(editorComp);
            editorComp.validate();
            editorComp.repaint();

            setCellEditor(editor);
            setEditingRow(row);
            setEditingColumn(column);
            editor.addCellEditorListener(this);

            return true;
        }
        return false;
    }

    /**
     * for Override
     *
     * @return
     */
    public JPopupMenu getPopupMenu() {
        return new PopUpMenu();
    }

    public void userEvent(String item) {

    }

    void readFromClipboard(boolean append) {
        String text = ClipboardReader.getClipboardContents();
//        String s = Utilities.encodeString(text, "UTF-8", "GBK");
//        s = Utilities.encodeString(text, "GBK", "UTF-8");
        MatrixTable mt = new MatrixTable();
        mt.setHasHeader(true);
        mt.read(text);
        DataStore ds = this.getDatastore();
        String[] cols = mt.getHeader();
        for (String col : cols) {
            if (!ds.hasColumnName(col)) {
                AppManager.MessageBox("数据源格式不匹配。");
                return;
            }
        }
        if (!append) {
            ds.clear();
        }
        for (int mtRow = 0; mtRow < mt.rowCount(); mtRow++) {
            int row = ds.appendRow();
            for (String col : cols) {
                String val = mt.get(mtRow, col);
                if (!"null".equals(val)) {
                    ds.setValueAt_s(val, row, col);
                } else {
                    int a = 0;
                }
            }
        }
        this.repaint(100);
    }

    /**
     * 延时刷新界面,
     * 用于数据变化较多的时候避免界面频繁闪动
     *
     * @param delayMms
     */
    public void repaint(int delayMms) {
        if (idleTimer == null) {
            idleTimer = new IdleTimer();
        }
        idleTimer.setCircleTime(delayMms);
    }

    public void repaint(long delayMms) {
        if (idleTimer == null) {
            idleTimer = new IdleTimer();
        }
        idleTimer.setCircleTime(delayMms);
    }

    public void addUdfMenu(String title, ActionListener act) {
        userMenuTitle.add(title);
        userMenuItem.add(act);
    }

    private class rowsDebug extends DWRowChanged {
        public rowsDebug() {
            debugId = "DataWindow.rowsDebug";
        }

        public void rowChanged(int currentRow, int rowCount) {
            CurrentRow = currentRow;
//            if (prop != null && prop.Debug()) {
//                int oraRow = activeTableModel.getOraRow(currentRow);
//                if (oraRow < 0) return;
//                for (int i = 0; i < activeTableModel.getColumnCount(); i++) {
//                    if (dm.getValueAt(oraRow, i) != activeTableModel.getValueAt(currentRow, i)) {
//                        //TODO Unmark
//                        //loger.getLoger().showError(new DBException("Row Map Error!", DBException.RUNTIMEERROR));
//                        break;
//                    }
//                }
//            }
        }
    }

    public class NumberCellRenderer extends DefaultTableCellRenderer {
        DecimalFormat floatFormat = new DecimalFormat("#,##0.00");
        DecimalFormat intFormat = new DecimalFormat("#,###");
        DecimalFormat defaultFormat = null;

        public void setUdfFormat(String format) {
            defaultFormat = new DecimalFormat(format);
        }

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel && value instanceof Number) {
                JLabel label = (JLabel) c;
                label.setHorizontalAlignment(JLabel.RIGHT);


                DecimalFormat ft = defaultFormat;
                if (ft == null) ft = floatFormat;
                if (value instanceof Long || value instanceof Integer || value instanceof Short) {
                    ft = intFormat;
                }


                Number num = (Number) value;
                String text = ft.format(num);
                label.setText(text);

                label.setForeground(num.doubleValue() < 0 ? Color.RED : Color.BLACK);
            }
            return c;
        }
    }

    public class AlignmentCellRenderer extends DefaultTableCellRenderer {
        int alignment = JLabel.LEFT;

        public void setAlignment(int Alignment) {
            alignment = Alignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel && value != null) {
                JLabel label = (JLabel) c;
                label.setHorizontalAlignment(alignment);
                label.setText(value.toString());
            }
            return c;
        }
    }

    class CellEditorRemover implements PropertyChangeListener {
        KeyboardFocusManager focusManager;

        public CellEditorRemover(KeyboardFocusManager fm) {
            this.focusManager = fm;
        }

        public void propertyChange(PropertyChangeEvent ev) {
            if (!isEditing() || getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE) {
                return;
            }

            Component c = focusManager.getPermanentFocusOwner();
            while (c != null) {
                if (c == DataWindow.this) {
                    // focus remains inside the table
                    return;
                } else if ((c instanceof Window) ||
                        (c instanceof Applet && c.getParent() == null)) {
                    if (c == SwingUtilities.getRoot(DataWindow.this)) {
                        if (!getCellEditor().stopCellEditing()) {
                            getCellEditor().cancelCellEditing();
                        }
                    }
                    break;
                }
                c = c.getParent();
            }
        }
    }

    class PopClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger())
                doPopUp(e);
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger())
                doPopUp(e);
        }

        private void doPopUp(MouseEvent e) {
            JPopupMenu menu = getPopupMenu();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    class PopUpMenu extends JPopupMenu {
        ItemHandler l = new ItemHandler();

        public PopUpMenu() {
            if (userMenuTitle.size() > 0) {
                for (String m : userMenuTitle) {
                    JMenuItem anItem = new JMenuItem(m);
                    add(anItem);
                    anItem.addActionListener(l);
                }
                addSeparator();
            }
            for (String m : popUpTreeMenus) {
                if (m.startsWith("---")) {
                    addSeparator();
                } else {
                    JMenuItem anItem = new JMenuItem(m);
                    add(anItem);
                    anItem.addActionListener(l);
                }
            }
        }
    }

    private class ItemHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            JMenuItem anItem = (JMenuItem) o;
            String t = anItem.getText();
            StringBuilder sb = new StringBuilder();
            boolean isCopy = true;

            if (userMenuTitle.contains(t)) {
                userMenuItem.get(userMenuTitle.indexOf(t)).actionPerformed(new ActionEvent(this, thisWin.getSelectedRow(), t));
            } else if (t.equals("拷贝字段内容")) {
                if (thisWin.getSelectedRow() >= 0 && thisWin.getSelectedRow() >= 0) {
                    for (int row : thisWin.getSelectedRows()) {
                        if (sb.length() > 0) sb.append("\n");
                        Object v = thisWin.getValueAt(row, thisWin.getSelectedColumn());
                        sb.append(v);
                        isCopy = true;
                    }
                }
            } else if (t.equals("拷贝行内容")) {
                if (thisWin.getSelectedRow() >= 0) {
                    for (int col = 0; col < thisWin.getColumnCount(); col++) {
                        if (sb.length() > 0) sb.append("\t");
                        Object v = thisWin.getColumnName(col);
                        sb.append(v);
                        isCopy = true;
                    }

                    for (int row : thisWin.getSelectedRows()) {
                        if (sb.length() > 0) sb.append("\n");
                        StringBuilder sbRow = new StringBuilder();
                        for (int col = 0; col < thisWin.getColumnCount(); col++) {
                            if (sbRow.length() > 0) sbRow.append("\t");
                            Object v = thisWin.getValueAt(row, col);
                            sbRow.append(v);
                        }
                        sb.append(sbRow);
                    }
                    isCopy = true;
                }
            } else if (t.equals("拷贝表格内容")) {
                sb.append(thisWin.getDataString("\t"));
                isCopy = true;
            } else if (t.equals("拷贝表格内容(报表)")) {
                sb.append(getTextReport(""));
                isCopy = true;
            } else if (t.equals("黏贴(替换)")) {
                readFromClipboard(false);
                isCopy = false;
            } else if (t.equals("黏贴(添加)")) {
                readFromClipboard(true);
                isCopy = false;
            }
            if (isCopy) {
                ClipboardReader.setClipboardContents(sb.toString());
            }
        }

    }

    //
    class IdleTimer extends CircleTimer {
        IdleTimer() {
            setTitle("Repaint DataWindow");
        }

        @Override
        public void execTask() {
            int selectedRow = getSelectedRow();
            repaintDataWindow();
            if (selectedRow >= 0) {
                try {
                    setRowSelectionInterval(selectedRow, selectedRow);
                } catch (Exception e) {

                }
//                scrollToRow(selectedRow);
            }
//            repaint();
        }
    }

}

