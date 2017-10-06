package szhzz.sql.database;


import javax.swing.table.TableModel;
import java.util.Hashtable;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-5
 * Time: 20:33:02
 * To change this template use File | Settings | File Templates.
 */
public class Filter {
    // if new row added to filtered table,
    // these columns will bee fill with default value
    // NOTE: autoColumn columns may not same as Filter elements 
    Hashtable autoColumn = null;

    public boolean filter(TableModel tm, int row) {
        return true;
    }

    public String getFilterString() {
        return "No Filter";
    }

    public void setAutoFillColumn(Object col, Object value) {
        if (null == autoColumn) {
            autoColumn = new Hashtable();
        }
        if (null == value)
            autoColumn.remove(col);
        else
            autoColumn.put(col, value);
    }

    /**
     * Overide this method in subclasses,
     *
     * @param col
     */
    public void setAutoFillColumn(int col) {
//        loger.getLoger().showError(
//                new DBException("Filter did not accomplish this method ",
//                        DBException.NOTIMPLEMENTED));
    }

    public Hashtable getAutoFillCols() {
        return autoColumn;
    }

    public boolean hasAutoFill() {
        return (null != autoColumn);
    }

}
