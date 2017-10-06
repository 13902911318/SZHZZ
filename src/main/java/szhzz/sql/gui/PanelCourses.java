package szhzz.sql.gui;

import szhzz.sql.database.DBException;
import szhzz.sql.database.DBProperties;
import szhzz.sql.database.Database;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-5
 * Time: 21:55:20
 * To change this template use File | Settings | File Templates.
 */
public class PanelCourses extends DwPanel {

    Database db;
    String sql = "select * from COURSEOFFERING";
    DBProperties prop;

    public PanelCourses() {
        super();
    }

    public void setTranscatObject(Database db) {
        this.db = db;
        prop = db.getDBProperties();
        super.setTranscatObject(db);
        init();
    }

    /**
     * add DbComboBox to datawindow column CRSCODE
     */
    public void init() {
        this.getDataWindow().setDataWindowReadOnly(true);
        this.sql = "select * from COURSEOFFERING where CRSCODE = '" + prop.getProperty("SELECTED COURSE") + "'" + "AND SEMESTER = '" + prop.getProperty("SELECTED SEMESTER") + "'";
        this.setSQL(sql);
    }


    public int retrive() throws DBException {
        // change szhzz.sql here

        return super.retrive();
    }

}
