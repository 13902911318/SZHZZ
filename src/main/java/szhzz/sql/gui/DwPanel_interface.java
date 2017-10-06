package szhzz.sql.gui;

import szhzz.sql.database.DBException;
import szhzz.sql.database.Database;

/**
 * Created by IntelliJ IDEA.
 * User: John
 * Date: 2006-10-12
 * Time: 23:04:26
 */
public interface DwPanel_interface {
    int retrive() throws DBException;

    int retrive(boolean repaint) throws DBException;

    void setSQL(String sql);

    DataWindow getDataWindow();

    void setTranscatObject(Database db);

    void reset();
}
