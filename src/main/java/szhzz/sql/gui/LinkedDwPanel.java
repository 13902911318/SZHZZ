package szhzz.sql.gui;


import szhzz.sql.database.DBException;
import szhzz.sql.database.DBProperties;
import szhzz.sql.database.Database;

import javax.swing.*;

/**
 * <p>Title: INFO2820</p>
 * <p/>
 * <p>Description: Home Work</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author John
 * @version 1.0
 */
public class LinkedDwPanel extends JSplitPane implements DwPanel_interface {

    public DBProperties prop = null;
    public DwPanel dwPanel1 = null;
    public DwPanel dwPanel2 = null;
    public DataWindow dwMaster, dwSlave;
    protected Database db;
    DwLinker_ dwLink;

    public LinkedDwPanel() {
        dwPanel1 = new DwPanel();
        dwPanel2 = new DwPanel();
        try {
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public LinkedDwPanel(DataWindow dwMaster, DataWindow dwSlave) {
        dwPanel1 = new DwPanel(dwMaster);
        dwPanel2 = new DwPanel(dwSlave);
        try {
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    private void jbInit() throws Exception {
        this.setOrientation(JSplitPane.VERTICAL_SPLIT);
        this.add(dwPanel1, JSplitPane.TOP);
        this.add(dwPanel2, JSplitPane.BOTTOM);
        this.setDividerLocation(150);
        dwMaster = dwPanel1.getDataWindow();
        dwSlave = dwPanel2.getDataWindow();
        dwLink = new DwLinker_(dwMaster, dwSlave);
        init();
    }

    public void init() {
        dwPanel1.init();
        dwPanel2.init();
    }

    public void MasterRowChanged(int currentRow, int rowCount) {

    }

    public int retrive() throws DBException {
        return retrive(true);
    }

    public int retrive(boolean repaint) throws DBException {
        return 0;
    }

    public void setSQL(String sql) {
    }

    public DataWindow getDataWindow() {
        return null;
    }

    public void setTranscatObject(Database db) {
        this.db = db;
        dwMaster.setTranscatObject(db);
        dwSlave.setTranscatObject(db);
        prop = db.getDBProperties();
    }

    public void reset() {

    }

    public void setNoFilterNewRow(boolean noFilterNewRow) {
    }


    class DwLinker_ extends DwLinker {
        public DwLinker_(DataWindow dwMaster, DataWindow dwSlave) {
            setDataWindows(dwMaster, dwSlave);
        }

        public void RowChanged(int currentRow, int rowCount) {
            MasterRowChanged(currentRow, rowCount);
        }
    }
}
