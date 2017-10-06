package szhzz.sql.gui;


import szhzz.sql.database.DBException;
import szhzz.sql.database.Database;

import javax.swing.*;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-5
 * Time: 15:39:18
 * To change this template use File | Settings | File Templates.
 */
public class PanelStudents extends DwPanel {
    DbComboBox dblist = new DbComboBox(new MutiColComboBoxEditoer());
    Database db;
    //String listSql = "select CRSCODE from course order by CRSCODE";
    String listSql = "select * from course order by CRSCODE";
    String sql = "select ID, NAME, PASSWORD, ADDRESS from student";


    public PanelStudents() {
        super();
    }

    public void setTranscatObject(Database db) {
        this.db = db;
        super.setTranscatObject(db);
        init();
    }

    /**
     * add DbComboBox to datawindow column CRSCODE
     */
    public void init() {
        this.setTitle(" Students ");
        dblist.addDisplayComs(0);
        dblist.setTranscatObject(db);
        dblist.setEditable(true);
        getDataWindow().addCellEditor(0, new DefaultCellEditor(dblist));
//        getDataWindow().readOnly = false;
        this.setSQL(sql);
    }


    public void reset() {
        super.reset();
        dblist.reset();
    }


    public int retrive() throws DBException {
        dblist.retrive(listSql);
        super.retrive();
        //getDataWindow().getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(dblist));
        return 1;
    }
}
