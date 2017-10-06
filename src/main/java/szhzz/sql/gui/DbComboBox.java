package szhzz.sql.gui;


import szhzz.sql.database.*;

import javax.swing.*;
import java.awt.event.InputMethodEvent;
import java.util.Vector;


/**
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
 */
public class DbComboBox extends MutiColCombBox {
    Vector<Integer> displayComs = new Vector<Integer>();
    int currentRow = 0;
    private Database db = null;
    private DBProperties prop = null;
    private String sql = null;
    private DataStore ds = null;
    private GeneralDAO dao;
    private boolean readOnly = true;

    public DbComboBox() {
        super();
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DbComboBox(MutiColComboBoxEditoer editor) {
        this();
        setEditor(editor);
    }

    public void setTranscatObject(Database db) {
        this.db = db;
        if (db != null) prop = this.db.getDBProperties();
    }


    public void copyData(DataStore ds) {
        Vector data = new Vector();
        for (int r = 0; r < ds.getRowCount(); r++) {
            Vector row = new Vector();
            for (Integer displayCom : displayComs) {
                row.add(ds.peekObject(r, displayCom));
            }
            data.add(row);
        }
        this.setModel(new DefaultComboBoxModel(data));
    }

    public void copyData(Vector has) {
//        Vector data = new Vector();
//        for (Enumeration e = has.keys(); e.hasMoreElements();) {
//            data.add(has.get(e.nextElement()));
//        }
        this.setModel(new DefaultComboBoxModel(has));
    }

    public DataStore getDataStore() {
        return ds;
    }

    public int refresh() throws DBException {
        if (!checkInit()) return -1;

        if (!db.isOpened()) db.openDB();
        if (null == dao) {
            dao = (GeneralDAO) db.getDAO(sql, readOnly);
            ds = null;
        }
        dao.retriev();

        // Protect cell from editing. the call path as below:
        // Jtable->dataWindow->dbTabbleModel.isCellEditable(currentRow, col)->ds.isReadonlyCol(col)
        if (null == ds) {
            ds = dao.getDataStore();
            if (displayComs.size() == 0) {
                for (int i = 0; i < ds.getColumnCount(); i++) {
                    displayComs.add(new Integer(i));
                }
            }
            Vector data = new Vector();
            for (int r = 0; r < ds.getRowCount(); r++) {
                Vector row = new Vector();
                for (Integer displayCom : displayComs) {
                    row.add(ds.peekObject(r, displayCom));
                }
                data.add(row);
            }
            this.setModel(new DefaultComboBoxModel(data));
        }
        return ds.getRowCount();
    }


    public int retrive(String sql) throws DBException {
        this.sql = sql;
        if (!checkInit()) return -1;
        reset();
        return refresh();
    }

    protected boolean checkInit() {
        if (db == null) {
            //TODO Unmark
//            loger.getLoger().errorOut("Transact object not defined!");
            return false;
        }
        if (sql == null) {
            //TODO Unmark
//            loger.getLoger().errorOut("Query statement not defined!");
            return false;
        }
        return true;
    }

    public void reset() {
        if (dao != null) dao.close();
        dao = null;
    }

    public void removeDisplayComs(int col) {
        displayComs.remove(new Integer(col));
    }

    protected void finalize() throws Throwable {
        reset();
        super.finalize();
    }

    public void addDisplayComs(int col) {
        if (!displayComs.contains(new Integer(col))) {
            displayComs.add(new Integer(col));
        }
    }

    public void EditChanged(InputMethodEvent event) {
    }

    public void TrggerItemChanged(Object oldData, Object newData) {
        // if currentRow = -1, user edite data
        currentRow = this.getSelectedIndex();
        if (ds != null && currentRow > 0) ds.scrollToRow(currentRow);

        //loger.getLoger().debug("Row=" + currentRow + "\nOld data = " + oldData + "\nNew data=" + CheckData);
    }

    private void jbInit() throws Exception {
//        this.setMaximumSize(new Dimension(32767, 20));
//        this.setMinimumSize(new Dimension(200, 19));
//        this.setPreferredSize(new Dimension(200, 19));
    }


    public void setCurrentValue(int col, Object Val) {
        Object o = this.getModel().getSelectedItem();
        if (o != null && o instanceof Vector) {
            ((Vector) o).removeElementAt(col);
            ((Vector) o).insertElementAt(Val, col);
        }
    }

    public int findItem(Object val) {
        int i = -1;
        javax.swing.MutableComboBoxModel m = (MutableComboBoxModel) getModel();
        Object o;
        while ((o = m.getElementAt(++i)) != null) {
            if (o instanceof Vector) {
                Vector v = (Vector) o;
                for (Object c : v) {
                    if (val.equals(c)) return i;
                }
            }
        }
        return -1;
    }

    public void addItem(int col, Object Val) {
        Vector n = new Vector();
        n.insertElementAt(Val, col);
        javax.swing.MutableComboBoxModel m = (MutableComboBoxModel) getModel();
        m.addElement(n);
        this.setSelectedItem(n);
    }

}

