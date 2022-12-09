package szhzz.Table;

import szhzz.Calendar.MyDate;
import szhzz.Files.FileZiper;
import szhzz.Table.Filters.RowFilter;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2007-12-23
 * Time: 13:09:30
 * To change this template use File | Settings | File Templates.
 */
public class MatrixTable {
    public static final int DataTypeLong = 1;
    public static final int DataTypeFloat = 2;
    public static final int DataTypeString = 3;
    public static final int DataTypeDate = 4;
    public static final boolean reverseOrder = true;
    public int sortColumnDataType = DataTypeString;
    String dataFileName = null;
    LinkedList<String> header = null;
    Hashtable<String, LinkedList<String>> indexMap = new Hashtable<String, LinkedList<String>>();
    private boolean has_Header = false;
    private boolean isDirty = false;
    private int sortColumn = -1;
    private boolean sortReves = false;
    private String nullValue = "";
    private RowFilter filter = null;
    private String delimiter = "\t";
    private LinkedList<LinkedList<String>> table;
    private Comparator comparator = null;
    private int readLines = 0;
    private String charsetName = null;
    private boolean noTrim = false;
    private MatrixTableModel tableModel = null;

    public MatrixTable() {
        table = new LinkedList<LinkedList<String>>();
    }

//    public MatrixTable(LinkedList<LinkedList<String>> table) {
//        this.table = table;
//    }

    public static void main(String args[]) throws IOException {
        MatrixTable testT = new MatrixTable();
        testT.setHasHeader(true);
        //testT.readZipFile(new File("T:\\QTS_CSV\\20190716\\SSEL2\\STOCK\\TAQ\\RealSSEL2Quote_S0101.zip"), 0, "");
        testT.readZipFile(new File("T:\\QTS_CSV\\20190716\\SSEL2\\STOCK\\TAQ\\RealSSEL2Quote_S0101.zip"), 0, "600030.csv");
        testT.sort(0);
//        testT.save();
    }

    public void reset() {
        table = new LinkedList<LinkedList<String>>();
        setDirty(false);
        header = null;
    }

    public int getColumnNo(String col) {
        if (header == null || col == null) return -1;
        return header.indexOf(col);
    }

    public boolean hasColumn(String col) {
        return getColumnNo(col) >= 0;
    }

    public int find(String col, String val) {
        return find(col, val, 0);
    }

    public int find(String col, String val, int startRow) {
        return find(getColumnNo(col), val, startRow);
    }

    public int find(int col, String val) {
        return find(col, val, 0);
    }

    public int find(int col, String val, int startRow) {
        if (col < 0) return -1;
        if (startRow < 0) startRow = 0;
        for (int r = startRow; r < rowCount(); r++) {
            String v = get(r, col);
            if (val.equals(v)) return r;
        }
        return -1;
    }

    public boolean sort(int sortColumn) {
        return sort(sortColumn, DataTypeString);
    }

    public boolean sort(int sortColumn, int dataType) {
        return sort(sortColumn, dataType, false);
    }

    public boolean sort(int sortColumn, boolean reverse) {
        return sort(sortColumn, DataTypeString, reverse);
    }

    public boolean sort(int sortColumn, int dataType, boolean reverse) {

        sortColumnDataType = dataType;
        this.sortReves = reverse;
        this.sortColumn = sortColumn;

        if (comparator == null) {
            comparator = new MyComparator();
            if (reverse) {
                ((MyComparator) comparator).reverseOrder();
            }
        }
        Collections.sort(table, comparator);

        if (comparator instanceof MyComparator) {
            indexMap.clear();
            for (LinkedList<String> r : table) {
                indexMap.put(r.get(sortColumn), r);
            }
        }
        setDirty(false);

        return true;
    }

    void reSort() {
        if (sortColumn != -1 && isDirty()) {
            sort(this.sortColumn, this.sortColumnDataType, this.sortReves);
        }
    }

    public LinkedList<String> getRow(int row) {
        if (row >= 0 && row < rowCount()) {
            return table.get(row);
        }
        return null;
    }

    public LinkedList<String> getRow(String rowKey) {
        reSort();
        LinkedList<String> r = null;
        if (indexMap != null) {
            r = indexMap.get(rowKey);
        }

        return r;
    }

    public int getRowNumber(int col, String val) {
        int row = -1;
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).get(col).equals(val)) {
                row = i;
                break;
            }
        }
        return row;
    }

    public String get(String rowKey, int col) {
        String val = null;
        LinkedList<String> r = getRow(rowKey);
        if (r != null) {
            val = r.get(col);
        }
        return val;
    }

    public String get(String rowKey, String col) {
        String val = null;
        LinkedList<String> r = null;

        int i = getRowNumber(0, rowKey);
        if (i >= 0) {
            r = getRow(i);
        }

        if (r != null) {
            val = r.get(getColumnNo(col));
        }
        return val;
    }

    public String get(int row, String col) {
        return get(row, getColumnNo(col));
    }
    public String get(int row, String col, String def) {
        return get(row, getColumnNo(col),def);
    }

    public String get(int row, int col, String def) {
        String val = null;
        LinkedList<String> dataRow = getRow(row);
        if (dataRow != null && col >= 0 && col < dataRow.size())
            val = dataRow.get(col);
        if (val != null) {
            if (!noTrim) {
                val = val.trim();
            }
        }
        if (val == null) return def;

        return val;
    }

    public String get(int row, int col) {
        return get(row, col, null);
    }

    public String[] getHeader() {
        String h[] = null;
        String h0[] = {};
        if (header != null) {
            h = header.toArray(h0);
        }
        return h;
    }

    public int rowCount() {
        if (table == null) return -1;
        return table.size();
    }

    public int colCount(int row) {
        if (row >= 0 && row < rowCount())
            return getRow(row).size();
        return 0;
    }

    public int colCount() {
        if (header != null) {
            return header.size();
        }
        if (table.size() > 0)
            return getRow(0).size();
        return 0;
    }

    public boolean isHasHeader() {
        return has_Header;
    }

    public void setHasHeader(boolean hasHeader) {
        this.has_Header = hasHeader;
    }

    public LinkedList<String> appendRow() {
        LinkedList<String> r = new LinkedList<String>();
        table.add(r);
        setDirty(true);
        return r;
    }

    public LinkedList<String> insertRow(int index) {
        LinkedList<String> r = new LinkedList<String>();
        if (index >= table.size()) {
            table.add(r);
        } else {
            table.add(index, r);
        }
        setDirty(true);
        return r;
    }

    public LinkedList<String> deleteRow(String rowKey) {
        LinkedList<String> r = null;
        setDirty(true);
        table.remove(r);
        return r;
    }

    public LinkedList<String> deleteRow(int r) {
        setDirty(true);
        return table.remove(r);
    }

    public boolean setAll(String col, String val) {
        return setAll(this.getColumnNo(col), val);
    }

    public boolean setAll(int col, String val) {
        if (val == null) return false;

        for (int i = 0; i < this.rowCount(); i++) {
            if (!setData(i, col, val)) return false;
        }
        return true;
    }

    public boolean setData(int row, int col, String val) {
        boolean ok = false;
        if (val != null && col >= 0) {
            LinkedList<String> r = this.getRow(row);
            if (r != null) {
                while (r.size() < (col + 1)) {
                    r.add("");
                }
                r.set(col, val);
                ok = true;
            }
        }
        return ok;
    }

    public boolean setData(int row, int col, Object val) {
        String nulString = null;
        return setData(row, col, (val == null ? nulString : val.toString()));
    }

    public boolean setData(int row, String col, Object val) {
        String nulString = null;
        int colI = header.indexOf(col);
        return setData(row, colI, (val == null ? nulString : val.toString()));
    }

    public boolean setColumnName(String cName) {
        boolean ok = false;
        has_Header = true;
        if (header == null) {
            header = new LinkedList<String>();
        }
        if (!this.hasColumn(cName)) {
            header.add(cName.trim());
            ok = true;
        }
        return ok;
    }

    public boolean addNewColumn(String cName, String initValue) {
        boolean ok = false;
        if (cName != null && has_Header) {
            if (!this.hasColumn(cName)) {
                if (header == null) {
                    header = new LinkedList<String>();
                }
                header.add(cName.trim());
                if (initValue != null) {
                    for (int i = 0; i < rowCount(); i++) {
                        LinkedList<String> row = getRow(i);
                        if (row != null)
                            row.add(initValue);
                    }
                }
                ok = true;
            }
        }
        return ok;
    }

    public boolean headerFilled() {
        return header != null;
    }

    public void saveAs(File file) throws IOException {
        PrintWriter out = new PrintWriter(file.getAbsolutePath(), "UTF-8");
        reSort();
        try {
            if (has_Header) {
                for (int i = 0; i < header.size(); i++) {
                    if (i > 0) out.print(delimiter);
                    String aHeader = header.get(i);
                    out.print(aHeader);
                }
                out.println();
            }
            for (int row = 0; row < rowCount(); row++) {
                LinkedList<String> r = getRow(row);
                if (r != null) {
                    for (int i = 0; i < r.size(); i++) {
                        if (i > 0) out.print(delimiter);
                        String aR = r.get(i);

                        if (aR == null || aR.equals("null")) {
                            aR = nullValue;
                        }
                        out.print(aR);
                    }
                    out.println();
                }
            }
        } finally {
            out.flush();
            out.close();
        }
    }

    public void save() throws IOException {
        saveAs(new File(dataFileName));
    }

    public void read(File file) {
        read(file, 0);
    }

    public void readZipFile(File file, int startLine) {
        readZipFile(file, startLine, null);
    }

    public void readZipFile(File file, int startLine, String fName) {
        BufferedReader in = null;
        ZipEntry entry;

        if (!file.exists()) return;

        try {
            FileZiper fileZiper = new FileZiper();

            ZipInputStream zipIn = fileZiper.getZipInputStream(file);
            while ((entry = zipIn.getNextEntry()) != null) {
                String f = entry.getName();
//                if(f.indexOf(".") > 0){
//                    f = f.substring(0,f.indexOf("."));
//                }
                if (fName == null || f.equalsIgnoreCase(fName)) {
                    if (charsetName == null) {
                        in = new BufferedReader(new InputStreamReader(zipIn));
                    } else {
                        in = new BufferedReader(new InputStreamReader(zipIn, charsetName));
                    }
                    read(in, startLine);
                    setDirty(true);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reSort();
            setDirty(false);
        }
    }

    public void read(String txt) {
        read(txt, 0);
    }

    public void read(String txt, int startLine) {
        if (txt == null) return;
        BufferedReader buff;
        try {
            if (charsetName == null) {
                buff = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(txt.getBytes())));
            } else {
                buff = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(txt.getBytes()), charsetName));
            }
            setDirty(true);
            read(buff, startLine);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            setDirty(false);
        }
        reSort();
    }

    public void read(BufferedReader in, int startLine) throws IOException {
        String l;
        int counter = 0;
        int skipLines = startLine;

        while ((l = in.readLine()) != null) {
            if (skipLines > 0) {
                skipLines--;
                continue;
            }

            if (counter == 0) {
                l = l.replace("\uFEFF", "");//UTF-8 with sing
            }
            if (counter == 0 && this.isHasHeader()) {
                if (!this.headerFilled()) {
                    StringTokenizer tok = new StringTokenizer(l, delimiter);
                    while (tok.hasMoreTokens()) {
                        this.setColumnName(tok.nextToken());
                    }
                }
            } else {
                if (noTrim) {
                    String e[] = l.split(delimiter);

                    if (filter != null && !filter.accept(e)) continue;

                    LinkedList<String> row = this.appendRow();
                    for (String anE : e) {
                        row.add(anE);
                    }
                } else if (l.trim().length() > 0) {
                    String e[] = l.split(delimiter);

                    if (filter != null && !filter.accept(e)) continue;

                    LinkedList<String> row = this.appendRow();
                    for (String anE : e) {
                        row.add(anE.trim());
                    }
                }
            }
            counter++;
            if (this.readLines > 0 && this.rowCount() >= this.readLines) {
                break;
            }
        }
    }

    public void read(File file, int startLine) {
        BufferedReader in = null;

        if (!file.exists()) return;
//        if (file.getName().toLowerCase().endsWith(".zip")){
//            readZipFile(file, startLine, file.getName());
//        }

        dataFileName = file.getAbsolutePath();
        setDirty(true);

        try {
            FileInputStream fin = new FileInputStream(file);
            if (charsetName == null) {
                in = new BufferedReader(new InputStreamReader(fin));
            } else {
                in = new BufferedReader(new InputStreamReader(fin, charsetName));
            }
            read(in, startLine);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reSort();
            setDirty(false);
        }
    }

    public String getNullValue() {
        return nullValue;
    }

    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    public void setFilter(RowFilter filter) {
        this.filter = filter;
    }

    public void setFilter() {
        this.filter = null;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    public void setReadLines(int readLines) {
        this.readLines = readLines;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public TableModel getTableModel() {
        if (tableModel == null) {
            tableModel = new MatrixTableModel();
        }
        return tableModel;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public void setNoTrim(boolean noTrim) {
        this.noTrim = noTrim;
    }

    //
    class MyComparator implements Comparator {
        int revese = 1;
        MyDate sortDate1 = null;
        MyDate sortDate2 = null;

        public void reverseOrder() {
            revese = -1;
        }

        public int compare(Object o1, Object o2) {
            int c = 0;
            LinkedList<String> row1 = (LinkedList<String>) o1;
            LinkedList<String> row2 = (LinkedList<String>) o2;

            switch (sortColumnDataType) {
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

    class MatrixTableModel implements TableModel {

        public int getRowCount() {
            return rowCount();
        }

        public int getColumnCount() {
            return colCount();
        }

        public String getColumnName(int columnIndex) {
            return header.get(columnIndex);  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Class<?> getColumnClass(int columnIndex) {
            return header.get(columnIndex).getClass();  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return get(rowIndex, columnIndex);
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            setData(rowIndex, columnIndex, aValue);
        }

        public void addTableModelListener(TableModelListener l) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void removeTableModelListener(TableModelListener l) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }


}
