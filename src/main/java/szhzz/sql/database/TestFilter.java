package szhzz.sql.database;

import javax.swing.table.TableModel;


/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-1
 * Time: 22:07:20
 * To change this template use File | Settings | File Templates.
 */
public class TestFilter extends Filter {
    public TestFilter() {
    }

    public boolean filter(TableModel tm, int row) {
        Object o = tm.getValueAt(row, 6);
        // NOTE for new rows
        if (null == o) return true;
        return (new Integer(o.toString()).intValue() > 5);
    }
}
