package JNI;


import szhzz.Utils.NU;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * TODO 此数据包在C++接口已经定于了属于 JNI 包. 不可移动到其它位置
 * Created with IntelliJ IDEA.
 * User: HuangFang
 * Date: 13-11-5
 * Time: 下午9:01
 * To change this template use File | Settings | File Templates.
 * <p>
 * 非线程安全数据包
 * <p>
 * 第一行, 数据属性
 * 第二行,数据字段名称(如Quant等动态字段数据),
 * 或数据行数,或空行
 * 第三行起为数据
 */
public class ExchangeData implements Serializable {
    //数据包第一行字段定义
    public static final int colErrCode = 0;   //for all
    public static final int colLogonID = 1;   //For Trans,Bag.Market.TDF
    public static final int colEventType = 1; //For Quant
    public static final int colRequestID = 2; //
    public static final int colFunID = 3;     //
    public static final int colMessage = 4;  //

    private StringBuilder dbgMsg = null;
    private int handledCount = 0;
    public static final String nl = System.getProperty("line.separator");
//    public static final String nl = "\n";

    public void initData() {
    }

    //public long responseID = 0;
//    ExchangeObjAddLongData(env, execData, (jlong)ErrID);
//    ExchangeObjAddLongData(env, execData, (jlong)LogonID);

    protected Vector<Vector> table = null;
    protected String logFile = null;
    private Vector currentRecord = null;

    public void clear() {
        if (table != null) {
            table.clear();
        }
        if (currentRecord != null) {
            currentRecord.clear();
        }


    }

    protected void setTitleCol(Object o, int col) {
        Vector row0 = null;
        if (table.size() == 0) {
            row0 = new Vector();
            table.add(row0);
        } else {
            row0 = table.get(0);
        }
        if (row0 != null) {
            while (row0.size() <= col) {
                row0.add("");
            }
            row0.setElementAt(o, col);
        }
    }

    public void appendRow() {
        if (table == null) {
            table = new Vector<Vector>();
        }
        currentRecord = new Vector();
        table.add(currentRecord);
    }

    public Vector getRow(int rowNo) {
        if (rowNo < 0 || rowNo >= table.size()) return null;
        return table.get(rowNo);
    }

    public void addData(Object o) {
        if (currentRecord == null)
            appendRow();

        currentRecord.add(o);
    }

    public void addData(int o) {
        if (currentRecord == null)
            appendRow();

        currentRecord.add(o);
    }

    public void addData(long o) {
        if (currentRecord == null)
            appendRow();

        currentRecord.add(o);
    }

    public void addData(float o) {
        if (currentRecord == null)
            appendRow();

        currentRecord.add(o);
    }

    public void addData(double o) {
        if (currentRecord == null)
            appendRow();

        currentRecord.add(o);
    }

    public void addNull() {
        if (currentRecord == null)
            appendRow();

        currentRecord.add(null);
    }

    public Long getRequestID() {
        return NU.parseLong(getValue(0, colRequestID), -1L);
    }

    /**
     * 不适合Quant，Quant返回的是流水号
     *
     * @return
     */
    public Long getFunID() {
        return NU.parseLong(getValue(0, colFunID), -1L);
    }


    public Long getErrorCode() {
        return NU.parseLong(getValue(0, colErrCode), -1L);
    }

    public Long getEventType() {
        return NU.parseLong(getValue(0, colEventType), -1L);
    }

    public Long getLogonID() {
        return NU.parseLong(getValue(0, colLogonID), -1L);
    }


    public String getMessageString() {
        String msg = (String) getValue(0, colMessage);
        if (msg == null) {
//            return Err.getErrorMsg(getErrorCode(), null);
            return "Unknow Error";
        } else {
            return msg;
        }
    }

    /**
     * this for Trans Only
     *
     * @return
     */
    public long getDataRowCount() {
        Long rowCount = null;
        Object o = getValue(1, 0);
        if (o instanceof Long) {
            rowCount = (Long) o;
        } else if (o instanceof Integer) {
            rowCount = ((Integer) o).longValue();
        }

        if (rowCount == null) {
            rowCount = table.size() - 2L;
            if (rowCount < 0) rowCount = 0l;
        } else {
            rowCount = Math.min(rowCount, table.size() - 2L);
        }
        return rowCount;
    }

    public Object getDataValue(int row, int col) {
        return getValue(row + 2, col, null);
    }

    public Object getDataValue(int row, int col, Object def) {
        return getValue(row + 2, col, def);
    }

    public Object getDataValue(int row, String col, Object def) {
        if (table.size() < 2) return def;
        try {
            return getValue(row + 2, table.get(1).indexOf(col), def);
        } catch (Exception ignored) {

        }
        return def;
    }

    public Object getValue(int row, int col) {
        return getValue(row, col, null);
    }

    public Object getValue(int row, int col, Object def) {
        if (col < 0) return def;
        if (table == null) return def;
        if (table.size() < (row + 1)) return def;
        if (table.get(row).size() < (col + 1)) return def;
        if (table.get(row).get(col) == null) return def;
        return table.get(row).get(col);
    }


    public void setFunID(Object funID) {
        setTitleCol(funID, colFunID);
    }

    public void setErrorCode(Object coed) {
        setTitleCol(coed, colErrCode);
    }

    public void setRequestID(Object requestID) {
        setTitleCol(requestID, colRequestID);
    }

    public void setMessage(Object msg) {
        setTitleCol(msg, colMessage);
    }

    public void setLoginCode(Object msg) {
        setTitleCol(msg, colLogonID);
    }

    public void setColEventType(Object msg) {
        setTitleCol(msg, colEventType);
    }

    public int getColumnIndex(String label) {
        return table.get(1).indexOf(label);
    }

    public String toString() {
        if (table == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Vector row : table) {
            for (Object o : row) {
                sb.append(o).append("\t");
            }
            sb.append(nl);
        }
        //sb.append(">>");
        return sb.toString();
    }


    public String toDataString(String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < table.size(); i++) {
            Vector row = table.get(i);
            for (Object o : row) {
                sb.append(o).append(separator);
            }
            sb.append(nl);
        }
        return sb.toString();
    }

    public Vector<Vector> getTable() {
        return table;
    }

    public boolean isEmpty() {
        return (table == null || table.size() == 0);
    }


    public String getRalData() {
        return "";
    }


    public void sort(Comparator comparator) {
        Vector<Vector> tRows = new Vector<Vector>();
        for (int i = table.size() - 1; i > 1; i--) {
            tRows.add(table.remove(i));
        }
        Collections.sort(tRows, comparator);
        table.addAll(tRows);
    }

    public int getHandledCount() {
        return handledCount;
    }

    public void setHandledCount() {
        this.handledCount++;
    }

    public String getDbgMsg() {
        if (this.dbgMsg == null) return "";
        return dbgMsg.toString();
    }

    public void setDbgMsg(String dbgMsg_) {
        if (this.dbgMsg == null) {
            this.dbgMsg = new StringBuilder();
        }
        this.dbgMsg.append("->").append(dbgMsg_);
    }
}
