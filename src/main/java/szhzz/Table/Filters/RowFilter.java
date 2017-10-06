package szhzz.Table.Filters;

import szhzz.Config.Config;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-3-5
 * Time: 14:46:18
 * To change this template use File | Settings | File Templates.
 */
public abstract class RowFilter {

    public RowFilter() {
    }

    public void setUp(Config prop) {

    }

    public abstract boolean accept(LinkedList<String> row);

    public abstract boolean accept(String val);

    public abstract boolean accept(int col, String val);

    public abstract boolean accept(String e[]);

//    public static RowFilter getInstance(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
//        return (RowFilter)Class.forName(className).newInstance();
//    }
}
