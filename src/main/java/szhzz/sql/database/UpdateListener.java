package szhzz.sql.database;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-7
 * Time: 20:12:43
 *
 * @see GeneralDAO
 * see jBgui.Panel_D
 */
public interface UpdateListener {

    /**
     * if we need other update script, executeEvent here ,
     * then return true to continue other update
     * or false prevent other update
     * <p/>
     * update some other table use szhzz.sql script:
     * we can use db.getPreparedStatement(String szhzz.sql) to get a PreparedStatement
     * for dynamic update
     * <p/>
     * may calre update status for row's in ds: ds.clearUpdate(row);
     * to inform  GeneralDAO to skip updating the row
     *
     * @param db
     * @param ds
     * @param row
     * @return continue update or prevent update
     */
    public boolean applyUpdate(Database db, DataStore ds, int row, boolean postUpdate);


}
