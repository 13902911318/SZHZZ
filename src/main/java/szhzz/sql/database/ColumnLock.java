package szhzz.sql.database;

/**
 * Created by IntelliJ IDEA.
 * User: John
 * Date: 2006-10-15
 * Time: 10:28:35
 */
public interface ColumnLock {

    public boolean isLocked(int row, int col);
}
