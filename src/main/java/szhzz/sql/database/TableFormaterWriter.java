package szhzz.sql.database;

import szhzz.Utils.DawLogger;
import szhzz.Utils.Utilities;

import javax.swing.table.TableModel;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-1-23
 * Time: 7:37:42
 * To change this template use File | Settings | File Templates.
 */
public class TableFormaterWriter {
    private static final DecimalFormat floatFormat = new DecimalFormat("#,##0.00");
    private static final DecimalFormat intFormat = new DecimalFormat("#,###");
    private static DawLogger logger = DawLogger.getLogger(TableFormaterWriter.class);
    TableModel model;
    LinkedList<Integer> columnWidth;
    String columnSpace = " | ";
    String Title = null;
    String Sumerry = null;
    DecimalFormat defaultFormat = null;

    public TableFormaterWriter(String Title, TableModel model) {
        this.model = model;
        this.Title = Title;
        columnWidth = new LinkedList<Integer>();
    }

    public void setSumerry(String Sumerry) {
        this.Sumerry = Sumerry;
    }

    private void checkColumnWidth() {
        for (int i = 0; i < model.getColumnCount(); i++) {
            checkColumnWidth(i, (model.getColumnName(i)));
        }
        for (int r = 0; r < model.getRowCount(); r++) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (model.getValueAt(r, i) != null)
                    checkColumnWidth(i, formatObject(model.getValueAt(r, i)));
            }
        }
    }

    private void checkColumnWidth(int col, String data) {
        while (columnWidth.size() < col + 1) columnWidth.add(4);
        int sl = stringLength(data);
        if (col == 0) sl++;
        if (sl > columnWidth.get(col))
            columnWidth.set(col, sl);
    }

    int stringLength(String s) {
        int len = s.length();
        try {
            len = new String(s.getBytes("GBK"), "8859_1").length();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return len;
    }

    private String center(String s, int len) {
        for (; stringLength(s) < len; ) {
            s = left(s, stringLength(s) + 1);
            if (stringLength(s) < len)
                s = right(s, stringLength(s) + 1);
        }
        return s;
    }

    private String left(String s, int len) {
        for (; stringLength(s) < len; ) {
            s += " ";
        }
        return s;
    }

    private String right(String s, int len) {
        for (; stringLength(s) < len; ) {
            s = " " + s;
        }
        return s;
    }

    int rowlineLenth() {
        int l = 0;
        for (Integer aColumnWidth : columnWidth) {
            l += aColumnWidth + columnSpace.length();
        }
        return l - 1;
    }

    String repeat(String s, int len) {
        StringBuffer sb = new StringBuffer();
        while (sb.length() < len)
            sb.append(s);
        return sb.toString();
    }

    public String getString() {
        return getString(false);
    }

    private void drawLine(StringBuffer sb, String s) {
        int lineLenth = rowlineLenth();
        sb.append(" ");
        sb.append(repeat(s, lineLenth - 1));
        sb.append("\r\n");
    }

    public String getString(boolean reves) {
        checkColumnWidth();

        StringBuffer sb = new StringBuffer();
        int lineLenth = rowlineLenth();


        if (Title != null) {
            sb.append("\r\n");
            sb.append(center(Title, lineLenth));
            sb.append("\r\n");
        }

        drawLine(sb, "-");

        sb.append(columnSpace.trim());
        for (int i = 0; i < model.getColumnCount(); i++) {
            sb.append(center(model.getColumnName(i), columnWidth.get(i))).append(columnSpace);
        }
        sb.append("\r\n");
        drawLine(sb, "=");

        int rowcount = model.getRowCount();
        if (reves) {
            for (int r = rowcount - 1; r >= 0; r--) {
                writeRow(sb, r);
            }
        } else {
            for (int r = 0; r < rowcount; r++) {
                writeRow(sb, r);
            }
        }

        drawLine(sb, "-");

        if (Sumerry != null) {
            sb.append("\r\n");
            sb.append(Sumerry);
        }
        return sb.toString();
    }

    public String getCsvString() {
        return getCsvString("\t");
    }

    public String getCsvString(String separator) {
        return getCsvString(false, separator);
    }

    public String getCsvString(boolean reves, String separator) {
//        checkColumnWidth();

        StringBuffer sb = new StringBuffer();
//        int lineLenth = rowlineLenth();

        if (Title != null) {
            sb.append(Title);
            sb.append("\r\n");
        }

        sb.append("\r\n");


        for (int i = 0; i < model.getColumnCount(); i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(model.getColumnName(i));
        }

        int rowcount = model.getRowCount();
        if (reves) {
            for (int r = rowcount - 1; r >= 0; r--) {
                sb.append("\r\n");
                writeXlsRow(sb, r, separator);
            }
        } else {
            for (int r = 0; r < rowcount; r++) {
                sb.append("\r\n");
                writeXlsRow(sb, r, separator);
            }
        }

        if (Sumerry != null) {
            sb.append("\r\n");
            sb.append(Sumerry);
        }
        return sb.toString();
    }

    void writeXlsRow(StringBuffer sb, int r, String separator) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (i > 0) {
                sb.append(separator);
            }
            String s;
            if (model.getValueAt(r, i) == null) {
                s = "";
            } else {
                s = formatObject(model.getValueAt(r, i));
            }
//            if(i == 0) s = " " + s;
            sb.append(s);
        }
    }

    void writeRow(StringBuffer sb, int r) {
        sb.append(columnSpace.trim());
        for (int i = 0; i < model.getColumnCount(); i++) {
            String s;
            if (model.getValueAt(r, i) == null) {
                s = "";
            } else {
                s = formatObject(model.getValueAt(r, i));
            }
            if (i == 0) s = " " + s;
            if (Utilities.isNumber(s)) {
                sb.append(right(s, columnWidth.get(i)));
            } else {
                sb.append(left(s, columnWidth.get(i)));
            }
            sb.append(columnSpace);
        }
        sb.append("\r\n");
    }


    String formatObject(Object value) {
        if (value == null) return null;
        DecimalFormat ft = null;
        if (value instanceof Number) {
            ft = defaultFormat;
            if (ft == null) ft = floatFormat;
            if (value instanceof Long || value instanceof Integer || value instanceof Short) {
                ft = intFormat;
            }
            return ft.format(value);
        }
        return value.toString().trim();
    }
}