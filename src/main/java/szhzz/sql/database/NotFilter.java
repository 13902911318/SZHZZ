package szhzz.sql.database;

import javax.swing.table.TableModel;

/**
 * Created by IntelliJ IDEA.
 * User: wn
 * Date: 2009-1-6
 * Time: 14:35:08
 * To change this template use File | Settings | File Templates.
 */
public class NotFilter extends Filter {
    Filter filter;

    public NotFilter(Filter filter) {
        this.filter = filter;
    }

    public boolean filter(TableModel tm, int row) {
        return !filter.filter(tm, row);
    }

}
