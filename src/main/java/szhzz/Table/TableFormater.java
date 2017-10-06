package szhzz.Table;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;


/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-1-23
 * Time: 7:37:42
 * To change this template use File | Settings | File Templates.
 */
public class TableFormater {
    MatrixTable Rows = new MatrixTable();
    LinkedList<Integer> columnWidth = new LinkedList<Integer>();
    String columnSpace = " | ";
    String Header = null;
    String Sumerry = null;
    private boolean textOutput = true;

    public MatrixTable getMatrixTable() {
        return Rows;
    }

    public void getMatrixTable(MatrixTable table) {
        Rows = table;
    }

    public void setTitle(String titles[]) {
        Rows.setHasHeader(true);
        for (String title : titles) {
            Rows.setColumnName(title);
        }
    }

    public void setTitle(LinkedList<String> t) {
        setTitle((String[]) t.toArray());
    }

    public void setHeader(String Header) {
        this.Header = Header;
    }

    public void setSumerry(String Sumerry) {
        this.Sumerry = Sumerry;
    }

    public void addRow(String data[]) {
        LinkedList<String> t = Rows.appendRow();
        for (String aData : data) {
            t.add(aData);
        }
    }

    public void addEmptyRow() {
        LinkedList<String> t = Rows.appendRow();
        for (int c = 0; c < Rows.colCount(); c++) {
            t.add("");
        }
    }

    public void addEmptyLine(String sibol) {
        LinkedList<String> t = Rows.appendRow();
        for (int c = 0; c < Rows.colCount(); c++) {
            t.add("&line&" + sibol);
        }
    }

    private void checkColumnWidth() {
        if (textOutput) {
            String heade[] = Rows.getHeader();
            if (heade != null) {
                for (int i = 0; i < heade.length; i++) {
                    checkColumnWidth(i, heade[i]);
                }
            }
            for (int r = Rows.rowCount() - 1; r >= 0; r--) {
                LinkedList<String> Row = Rows.getRow(r);
                for (int i = 0; i < Row.size(); i++) {
                    checkColumnWidth(i, Row.get(i));
                }
            }
        }
    }

    private void checkColumnWidth(int col, String data) {
        if (textOutput) {
            while (columnWidth.size() < col + 1) columnWidth.add(4);
            int sl = stringLength(data);

            if (sl > columnWidth.get(col))
                columnWidth.set(col, sl);
        }
    }

    int stringLength(String s) {
        int len = s.length();
        try {
            len = new String(s.getBytes("GBK"), "8859_1").length();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
        if (!textOutput) {
            return getStringTab(false);
        }
        return getString(false);
    }

    private void drawLine(StringBuffer sb, String s) {
        int lineLenth = rowlineLenth();
        sb.append(" ");
        sb.append(repeat(s, lineLenth - 1));
        sb.append("\r\n");
    }

    public String getString(boolean reves) {
        if (!textOutput) {
            return getStringTab(reves);
        }

        checkColumnWidth();

        StringBuffer sb = new StringBuffer();
        int lineLenth = rowlineLenth();


        if (Header != null) {
            sb.append("\r\n");
            sb.append(center(Header, lineLenth));
            sb.append("\r\n");
        }

        drawLine(sb, "-");
        String heade[] = Rows.getHeader();
        if (heade != null) {
            sb.append(columnSpace.trim());
            for (int i = 0; i < heade.length; i++) {
                sb.append(center(heade[i], columnWidth.get(i))).append(columnSpace);
            }
            sb.append("\r\n");
            drawLine(sb, "=");
        }

        if (reves) {
            for (int r = Rows.rowCount() - 1; r >= 0; r--) {
                writeRow(sb, r);
            }
        } else {
            for (int r = 0; r < Rows.rowCount(); r++) {
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


    public String getStringTab(boolean reves) {
        checkColumnWidth();

        StringBuffer sb = new StringBuffer();
        String heade[] = Rows.getHeader();
        if (heade != null) {
            for (int i = 0; i < heade.length; i++) {
                if (i > 0)
                    sb.append("\t");

                sb.append(heade[i]);
            }
            sb.append("\r\n");
        }

        if (reves) {
            for (int r = Rows.rowCount() - 1; r >= 0; r--) {
                writeRow(sb, r);
            }
        } else {
            for (int r = 0; r < Rows.rowCount(); r++) {
                writeRow(sb, r);
            }
        }
        return sb.toString();
    }

    void writeRow(StringBuffer sb, int r) {
        LinkedList<String> Row = Rows.getRow(r);
        if (textOutput) sb.append(columnSpace.trim());
        for (int i = 0; i < Row.size(); i++) {
            if (Row.get(i).startsWith("&line&")) {
                if (textOutput) {
                    String s = Row.get(i).substring("&line&".length());
                    String l = repeat(s, columnWidth.get(i));
                    String sp = columnSpace.replace(" ", s);
                    sb.append(l).append(sp);
                }
            } else {
                sb.append(right(Row.get(i), columnWidth.get(i)));
                if (textOutput) {
                    sb.append(columnSpace);
                } else {
                    sb.append("\t");
                }
            }
        }
        sb.append("\r\n");
    }

    public void setTextOutput(boolean textOutput) {
        this.textOutput = textOutput;
    }
}
