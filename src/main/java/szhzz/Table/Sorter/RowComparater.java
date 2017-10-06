package Table.Sorter;

import szhzz.Calendar.MyDate;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-7-5
 * Time: 19:55:01
 * To change this template use File | Settings | File Templates.
 */
public class RowComparater implements Comparator {
    public static final int DataTypeInt = 1;
    public static final int DataTypeLong = 2;
    public static final int DataTypeFloat = 3;
    public static final int DataTypeString = 4;
    public static final int DataTypeDate = 5;

    public int sortColumnDataType = DataTypeString;
    int sortColumn = -1;

    int revese = 1;
    MyDate sortDate1 = null;
    MyDate sortDate2 = null;


    public RowComparater(int col, int dataType, boolean setRevers) {
        setUp(col, dataType, setRevers);
    }

    public void reverseOrder() {
        revese = -1;
    }

    public void setUp(int col, int dataType, boolean setRevers) {
        this.sortColumn = col;
        sortColumnDataType = dataType;
        if (setRevers) revese = -1;
    }

    public int compare(Object o1, Object o2) {
        int c = 0;
        LinkedList<String> row1 = (LinkedList<String>) o1;
        LinkedList<String> row2 = (LinkedList<String>) o2;

        switch (sortColumnDataType) {
            case DataTypeInt:
                c = new Integer(row1.get(sortColumn)).compareTo(new Integer(row2.get(sortColumn)));
                break;
            case DataTypeLong:
                c = new Long(row1.get(sortColumn)).compareTo(new Long(row2.get(sortColumn)));
                break;
            case DataTypeFloat:
                c = new Float(row1.get(sortColumn)).compareTo(new Float(row2.get(sortColumn)));
                break;
            case DataTypeString:
                c = row1.get(sortColumn).compareTo(row2.get(sortColumn));
                break;
            case DataTypeDate:
//                    if (sortDate1 == null) {
//                        sortDate1 = new MyDate(row1.get(sortColumn));
//                        sortDate2 = new MyDate(row1.get(sortColumn));
//                    } else {
//                        sortDate1.setDate(row1.get(sortColumn));
//                        sortDate2.setDate(row2.get(sortColumn));
//                    }
//                    c = sortDate1.compareDays(sortDate2);
                c = row1.get(sortColumn).compareTo(row2.get(sortColumn));
                break;
            default:
                c = row1.get(sortColumn).compareTo(row2.get(sortColumn));
        }
        return revese * c;
    }
}
