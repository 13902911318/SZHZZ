package szhzz.sql.database;

import java.sql.ResultSet;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-9-7
 * Time: 19:56:15
 * To change this template use File | Settings | File Templates.
 */
public abstract class DatabaseEvent {
    public static final int NOEXECUTE = 0;
    public static final int QUERY = 1;
    public static final int UPDATE = 2;

    public abstract void TaskFinished(Database db, ResultSet rs, int rowcount, Exception e);

    public boolean TriggerEvent(Database db) {
        return true;
    }

    public String getSQL() {
        return null;
    }

    public int getExcuteType() {
        return NOEXECUTE;
    }

}
