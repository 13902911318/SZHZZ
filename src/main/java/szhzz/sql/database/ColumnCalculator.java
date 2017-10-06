package szhzz.sql.database;


/**
 * Created by IntelliJ IDEA.
 * User: wn
 * Date: 2009-1-17
 * Time: 10:21:09
 * To change this template use File | Settings | File Templates.
 */
public class ColumnCalculator {

    public static ColumnCalculator getInstance(String url) {
        Object anInstance = null;
        Class aClass = null;
        try {
            aClass = Class.forName(url);
            anInstance = aClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        if (anInstance != null) {
            return (ColumnCalculator) anInstance;
        }
        return new ColumnCalculator();
    }

    public static void main(String args[]) {
        ColumnCalculator col = getInstance("stock.Optimizer.NJL.NJLStateRigntWait");

        if (col != null) {
            Object o = col.calculate("G");
            o = null;
        }
        col = getInstance("stock.Optimizer.NJL.NJLState");
        if (col != null) {
            Object o = col.calculate("G");
            o = null;
        }
    }

    public Object calculate(Object o) {
        return o;
    }
}
