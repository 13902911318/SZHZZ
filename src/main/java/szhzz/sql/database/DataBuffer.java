package szhzz.sql.database;

import szhzz.Utils.Utilities;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: wn
 * Date: 2009-1-26
 * Time: 22:45:09
 * To change this template use File | Settings | File Templates.
 */
public class DataBuffer implements Serializable {
    LinkedList<LinkedList<Object>> table = new LinkedList<LinkedList<Object>>();
    int colCount = 0;
    int currentRowNumber = -100;
    int sortCol = 0;
    public final transient Comparator COMPARABLE_COMAPRATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            LinkedList v1 = (LinkedList) o1;
            LinkedList v2 = (LinkedList) o2;
            return ((Comparable) v1.get(sortCol)).compareTo(v2.get(sortCol));
        }
    };
    public final transient Comparator LEXICAL_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            LinkedList v1 = (LinkedList) o1;
            LinkedList v2 = (LinkedList) o2;
            return (v1.get(sortCol).toString()).compareTo(v2.get(sortCol).toString());
        }
    };
    private LinkedList<Object> currentRecord = null;
    //    LinkedList currentRow = null;
    private int fixedRows = Integer.MAX_VALUE;

    public void copyTo(DataBuffer tagetDf) {
        tagetDf.table.addAll(table);
    }

    public int getColumnCount() {
        return colCount;
    }

    public void setColumnCount(int colCount) {
        this.colCount = colCount;
    }

    public void addObject(Object obj) {
        if (currentRecord != null && currentRecord.size() <= getColumnCount()) {
            currentRecord.add(obj);
        } else {
        }
    }

    public boolean setValueAt(Object value, int row, int col) {
        if (table.size() == 0 || row < 0 || row >= table.size()) {
            return false;
        }
        try {
            LinkedList<Object> data = table.get(row);
            while ((data.size() - 1) < col) {
                data.add(null);
            }
            data.set(col, value);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    public int appendRow() {
        if (table.size() >= fixedRows) {
            table.removeFirst();
        }
        currentRecord = new LinkedList<Object>();
        table.add(currentRecord);
        currentRowNumber = table.size() - 1;
        return currentRowNumber;
    }

    public int appendRow(LinkedList row) {
        if (table.size() > fixedRows) {
            table.removeFirst();
        }
        currentRecord = row;
        table.add(currentRecord);
        currentRowNumber = table.size() - 1;
        return currentRowNumber;
    }

    public int addFirst(LinkedList row) {
        if (table.size() > fixedRows) {
            table.removeLast();
        }
        currentRecord = row;
        table.addFirst(currentRecord);
        currentRowNumber = 0;
        return currentRowNumber;
    }


    public int getRowCount() {
        return table.size();
    }

    public LinkedList getRow(int r) {
        return table.get(r);
    }

    public LinkedList deleteRow(int r) {
        if (currentRecord != null && r < currentRowNumber) {
            currentRowNumber--;
        }
        return table.remove(r);
    }

    public Object peekObject(int row, int col) {
        if (row < 0) {
            return null;
        } else if (row >= table.size()) {
            return null;
        }

        Object val = (table.get(row)).get(col);
        if (JDBCType.isNULL == val) return null;
        return val;
    }

//    public Float getFloat(int row, int col, float ifnull) {
//        Object o = peekObject(row, col);
//        if(o == null) return ifnull;
//        return (Float) o;
//    }

    public void reset() {
        table.clear();
        currentRowNumber = -100;
        currentRecord = null;
    }

//    public boolean next() {
//        pointer++;
//
//        if (pointer >= table.size()) {
//            return false;
//        }
//        currentRow = table.get(pointer);
//        return true;
//    }


    public int getCurrentRow() {
        return currentRowNumber;
    }

    public void setPointerToEndRow() {
        currentRowNumber = table.size();
    }

    public boolean next(int step) {
        if (currentRowNumber < -1 || currentRowNumber > table.size()) {
            return false;
        }

        currentRowNumber += step;

        if (currentRowNumber < 0 || currentRowNumber >= table.size()) {
            return false;
        }
        currentRecord = table.get(currentRowNumber);
        return true;
    }

    public Integer getInteger(int col) {
        Object o = get(col);
        if (o != null) {
            if (o instanceof Integer) {
                return (Integer) o;
            } else {
                return new Integer(o.toString());
            }
        }
        return null;
    }

    public Long getLong(int col) {
        Object o = get(col);
        if (o != null) {
            if (o instanceof Long) {
                return (Long) o;
            } else {
                return new Long(o.toString());
            }
        }
        return null;
    }

    public Float getFloat(int col) {
        Object o = get(col);
        if (o != null) {
            if (o instanceof Float) {
                return (Float) o;
            } else {
                return new Float(o.toString());
            }
        }
        return null;
    }

    public String getString(int col) {
        Object o = get(col);
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    public Object get(int col) {
        if (currentRecord == null || col >= currentRecord.size() || col < 0) {
            return null;
        }
        return currentRecord.get(col);
    }

    public void sort(int col) {
        if (table.size() > 0) {
            sortCol = col;
            Collections.sort(table, getComparator(col));
        }
    }

    protected Comparator getComparator(int column) {
        Class columnType = table.get(0).get(column).getClass();
        if (Comparable.class.isAssignableFrom(columnType)) {
            return COMPARABLE_COMAPRATOR;
        }
        return LEXICAL_COMPARATOR;
    }

    public int getFixedRows() {
        return fixedRows;
    }

    public void setFixedRows(int fixedRows) {
        this.fixedRows = fixedRows;
    }

    public void saveAs(String file) throws IOException {
        StringBuffer sb = new StringBuffer();

        for (LinkedList<Object> row : table) {
            for (int col = 0; col < row.size(); col++) {
                Object v = row.get(col);
                sb.append((v == null ? "NULL" : v)).append("\t");
            }
            sb.append("\r\n");
        }
        Utilities.String2File(sb.toString(), file, false);
    }
}
