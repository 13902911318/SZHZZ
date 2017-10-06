package szhzz.sql.database;


import szhzz.Calendar.MyDate;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-9-30
 * Time: 0:06:34
 * <p/>
 * DataValidater 用于数据类型的检查，可以针对 dataStore 的指定字段
 * 设置不同的 DataValidater 子类来进行指定的检查。同一字段可以同时
 * 设置数个检查类型。dataStore 将依次进行指定的检查。
 * <p/>
 * <p/>
 * DataValidater 的子类有些是静态的以便提高效率
 * <p/>
 * 我们还可以按实际需要，添加特殊的检查类型。
 *
 * @see DataStore
 * see dbquery.gui.DataWindow  editingStopped()
 */
public class DataValidater {
    private static dateValidater dv = null;  // we need only one dateValidater instance
    private static notNULL nn = null;
    private static isNumber isn = null;

    private String errMsg = "Invalid data value!";


    // private constructor
    public DataValidater() {

    }

    public static dateValidater getDateValidater() {
        if (null == dv) dv = new dateValidater();
        return dv;
    }

    public static isNumber getIsNumber() {
        if (null == isn) isn = new isNumber();
        return isn;
    }

    public static notNULL getNotNULL() {
        if (null == nn) nn = new notNULL();
        return nn;
    }

    public static maxNumber getMaxNumber(long max) {
        return new maxNumber(max);
    }

    public static minNumber getMinNumber(long min) {
        return new minNumber(min);
    }

    public static StringLength getStringLength(int max) {
        return new StringLength(max);
    }

    public String name() {
        return "DataValidater.?";
    }

    public boolean validate(Object value) {
        return true;
    }

    public String getErrorMsg() {
        return errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}


class isNumber extends DataValidater {
    public isNumber() {
        setErrMsg("Data must be a Number!");
    }

    public boolean validate(Object value) {
        if (null == value) return true;   // do not chaeck not_null here

        try {
            new Float(value.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public String name() {
        return "isNumber";
    }
}

class maxNumber extends DataValidater {
    long max;

    public maxNumber(long max) {
        this.max = max;
        setErrMsg("Data value must small than or equal to " + max + " !");
    }

    public boolean validate(Object value) {
        if (null == value) return true;   // do not chaeck not_null here
        return (new Long(value.toString())).longValue() <= max;
    }

    public String name() {
        return "Max(" + max + ")";
    }

}

class minNumber extends DataValidater {
    long min;

    public minNumber(long min) {
        this.min = min;
        setErrMsg("Data value must bigger than or equal to " + min + " !");
    }

    public boolean validate(Object value) {
        if (null == value) return false;
        return (new Long(value.toString())).longValue() <= min;
    }

    public String name() {
        return "Min(" + min + ")";
    }

}

class notNULL extends DataValidater {
    public notNULL() {
        setErrMsg("Data value can not be NULL!");
    }

    public boolean validate(Object value) {
        return (null != value);
    }

    public String name() {
        return "notNULL";
    }

}

class StringLength extends DataValidater {
    int maxLen = 0;
    int actLength = 0;

    public StringLength(int maxLen) {
        this.maxLen = maxLen;
    }

    public boolean validate(Object value) {
        actLength = value.toString().length();
        if ((actLength > maxLen)) {
            setErrMsg("String ids too long! \n" +
                    "Max Length = " + maxLen + "\n" +
                    "Act Length = " + actLength);
            return false;
        }
        return true;
    }

    public String name() {
        return "StringLength(" + maxLen + ")";
    }
}

class dateValidater extends DataValidater {
    public dateValidater() {
        setErrMsg("Invalid Date value!");
    }

    public boolean validate(Object value) {
        Date d = null;
        if (value == null) return true;

        if (value instanceof Date) {
            d = (Date) value;

            try {
                DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
                df.setLenient(false);  // this is important!
                String dbg = new MyDate(d).getDate();
//                df.parse(value.toString());
                df.parse(dbg);
            } catch (ParseException e) {
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public String name() {
        return "dateValidater";
    }
}
