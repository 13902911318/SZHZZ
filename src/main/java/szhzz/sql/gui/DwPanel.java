package szhzz.sql.gui;


import org.apache.commons.io.FileUtils;
import szhzz.Java_infor.Light;
import szhzz.sql.database.DBException;
import szhzz.sql.database.Database;
import szhzz.sql.database.TableFormaterWriter;
import szhzz.Utils.Utilities;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Vector;

/**
 * <p>Title: INFO2820</p>
 * <p/>
 * <p>Description: home work INFO2820</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author John
 * @version 1.0
 * @see LinkedDwPanel
 */
public class DwPanel extends JPanel implements DwPanel_interface {
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    DataWindow dataWindow1 = null;
    DwToolBar dwToolBar1 = new DwToolBar();
    JLabel jLabelStatus = new JLabel();
    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JLabel jLabelTitle = new JLabel();
    TitledBorder titledBorder1 = new TitledBorder("");
    StringBuilder stateString = null;
    Light light = null;
    private TableModel stateData = null;

    public DwPanel() {
        super();
        try {
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public DwPanel(DataWindow dw) {
        dataWindow1 = dw;
        try {
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public DwToolBar getToolBar() {
        return dwToolBar1;
    }

    /**
     * @throws Exception
     */
    protected void jbInit() throws Exception {
        if (null == dataWindow1)
            dataWindow1 = new DataWindow();

        this.setLayout(borderLayout1);
        jPanel1.setLayout(borderLayout2);
        jLabelTitle.setFont(new java.awt.Font("Default", Font.BOLD, 14)); //
        jLabelTitle.setForeground(new Color(102, 102, 102));
        jLabelTitle.setBorder(null);
        jLabelTitle.setToolTipText("");
        jLabelTitle.setText("DW");
        jLabelStatus.setFont(new java.awt.Font("Default", Font.PLAIN, 12));
        jLabelStatus.setBorder(BorderFactory.createEtchedBorder());
//        jLabelStatus.setMaximumSize(new Dimension(0, 23));
//        jLabelStatus.setPreferredSize(new Dimension(0, 23));
        jScrollPane1.setBorder(null);
        jScrollPane1.getViewport().add(dataWindow1);
        jPanel1.add(dwToolBar1, java.awt.BorderLayout.CENTER);
        jPanel1.add(jLabelTitle, java.awt.BorderLayout.WEST);
        this.add(jPanel1, java.awt.BorderLayout.NORTH);
        this.add(jLabelStatus, java.awt.BorderLayout.SOUTH);
        this.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        dwToolBar1.setDataWindow(dataWindow1);
        dataWindow1.setDataWindowReadOnly(false);
        jLabelStatus.setVisible(false);
//        dataWindow1.setStatusBar(jLabelStatus);

//        light = new Light();
//        light.setLightColor(Color.RED);
//        light.setPreferredSize(new Dimension(20, 20));
//        light.setMaximumSize(new Dimension(20, 20));
//
//        light.setLightOn(true);
//        this.add(jLabelStatus, java.awt.BorderLayout.SOUTH);
    }

    /**
     * for overide
     */
    public void init() {
        dwToolBar1.setFloatable(false);
        jLabelTitle.setVisible(false);
        jLabelStatus.setVisible(false);
    }

    public void setTitle(String title) {
        if (title == null) {
            jLabelTitle.setVisible(false);
        } else {
            jLabelTitle.setVisible(true);
            jLabelTitle.setText(title);
        }
    }

    public void setStatus(Vector cols) {
        if (cols == null) {
            jLabelStatus.setVisible(false);
            return;
        }
        jLabelStatus.setVisible(true);
        jLabelStatus.setText(HTMLWraper(cols));
    }

    public void setStatusMultiLines(Vector lines) {
        setStatusMultiLines(lines, 0);
    }

    public void setStatusMultiLines(Vector lines, int border) {
        jLabelStatus.setVisible(true);
        jLabelStatus.setText(HTMLWraperMutyLines(lines, border));
    }

    public void setStatusMultiLines(TableModel tm) {
        setStatusMultiLines(tm, 0);
    }

    public void setStatusMultiLines(TableModel tm, int border) {
        jLabelStatus.setVisible(true);
        jLabelStatus.setText(HTMLWraperMutyLines(tm, border));
    }


    public void setStatus(String test) {
        if (test == null) {
            jLabelStatus.setVisible(false);
            return;
        }
        jLabelStatus.setVisible(true);
        jLabelStatus.setText(test);
        stateString = new StringBuilder(test);
    }

    public void setCanSave(boolean canSave) {
        dwToolBar1.setCanSave(canSave);
    }

    public void setCanRetrieve(boolean can) {
        dwToolBar1.setCanRetrieve(can);
    }

    public void setCanAdd(boolean canAdd) {
        dwToolBar1.setCanAdd(canAdd);
    }

    public void setCanScroll(boolean canScroll) {
        dwToolBar1.setCanScroll(canScroll);
    }


    public void setCanDele(boolean canDele) {
        dwToolBar1.setCanDele(canDele);
    }

    public void addToolbarEvent(DwToobar_Event event) {
        dwToolBar1.addEvent(event);
    }


    public int retrive(boolean repaint) throws DBException {
        int rows;
        dwToolBar1.setEnableAll(false);
        try {
            rows = dataWindow1.retrive(repaint);
        } finally {
            dwToolBar1.setEnableAll(true);
        }

        return rows;

    }

    public int retrive() throws DBException {
        return retrive(true);
    }

    public void repaintDataWindow() {
        dataWindow1.repaintDataWindow();
    }

    public void setSQL(String sql) {
        dataWindow1.setSQL(sql);
    }

    public DataWindow getDataWindow() {
        return dataWindow1;

    }


    public void setTranscatObject(Database db) {
        dataWindow1.setTranscatObject(db);
    }

    public void reset() {

    }

    public void setToolBarEnable(boolean on) {
        dwToolBar1.setEnabled(on);
    }

    public String HTMLWraper(Vector item) {
        String START = "<html><table border=1><tr>";
        String END = "</tr></table></html>";
        StringBuffer sb = new StringBuffer();
        stateString = new StringBuilder();

        sb.append(START);
        for (int i = 0; i < item.size(); i++) {
            sb.append("<td>"); //<strong>
            sb.append(item.get(i).toString());
            stateString.append("\t").append(item.get(i).toString());
            sb.append("</td>");   //</strong>
        }
        sb.append(END);
        return sb.toString();
    }


    public String HTMLWraperMutyLines(TableModel tm) {
        return HTMLWraperMutyLines(tm, 0);
    }

    public String HTMLWraperMutyLines(TableModel tm, int border) {
        String START = "<html><table border=" + border + ">";
        String END = "</table></html>";
        StringBuilder sb = new StringBuilder();
        stateString = new StringBuilder();

        stateData = tm;

        sb.append(START);
        int cols = tm.getColumnCount();
        sb.append("<tr>"); //<strong>
        for (int c = 0; c < cols; c++) {
            sb.append("<td>"); //<strong>
            sb.append(tm.getColumnName(c));
            sb.append("</td>"); //<strong>

            stateString.append("\t").append(tm.getColumnName(c));
        }
        sb.append("</tr>");

        int rows = tm.getRowCount();
        for (int r = 0; r < rows; r++) {
            sb.append("<tr>"); //<strong>
            for (int c = 0; c < cols; c++) {
                sb.append("<td>"); //<strong>
                sb.append(tm.getValueAt(r, c));
                sb.append("</td>"); //<strong>

                stateString.append("\t").append(tm.getValueAt(r, c));
            }
            sb.append("</tr>");
            stateString.append("\r\n");
        }

        sb.append(END);
        return sb.toString();
    }

    public String HTMLWraperMutyLines(Vector item) {
        return HTMLWraperMutyLines(item, 0);
    }

    public String HTMLWraperMutyLines(Vector item, int border) {
        String START = "<html><table border=" + border + ">";
        String END = "</table></html>";
        StringBuilder sb = new StringBuilder();
        stateString = new StringBuilder();

        sb.append(START);
        for (Object anItem : item) {
            sb.append("<tr>"); //<strong>
            if (anItem instanceof Vector) {
                Vector line = (Vector) anItem;
                for (Object aLine : line) {
                    sb.append("<td>"); //<strong>
                    sb.append(aLine.toString());
                    stateString.append("\t").append(aLine.toString());
                    sb.append("</td>"); //<strong>
                }
            } else {
                sb.append("<td>"); //<strong>
                sb.append(anItem.toString());
                stateString.append("\t").append(anItem.toString());
                sb.append("</td>");
            }
            sb.append("</tr>");
            stateString.append("\r\n");
        }
        sb.append(END);
        return sb.toString();
    }


    public String saveAsTextReport(String title, String fileName) throws IOException {
        return saveAsTextReport(title, fileName, false);
    }

    public String saveAsTextReport(String title, String fileName, boolean append) throws IOException {
        String selectedFile = null;
        if (fileName == null) {
            fileName = "Untitled" + File.separator + "txt";
            JFileChooser chooser = new JFileChooser(new File(fileName));
            chooser.showSaveDialog(this);
            File f = chooser.getSelectedFile();
            if (f != null) selectedFile = f.getAbsolutePath();
        } else {
            selectedFile = fileName;
        }

        if (selectedFile != null) {
            Utilities.String2File(getReportString(title).toString(), selectedFile, append);
        }
        return selectedFile;
    }

    public StringBuilder getReportString(String title) {
        StringBuilder sb = new StringBuilder(dataWindow1.getTextReport(title));
        sb.append(getStatuReportString(""));
        return sb;
    }

    public StringBuilder getStatuReportString(String title) {
        StringBuilder sb = new StringBuilder();
        if (stateData != null) {
            sb.append(new TableFormaterWriter(title, stateData).getString());
        } else if (stateString != null) {
            sb.append("\r\n").append(stateString.toString());
        }
        return sb;
    }

    public String saveAs(String colSeperator, String fileName, boolean append) throws IOException {
        return saveAs(colSeperator, fileName, append, Charset.forName("UTF8"));
    }

    public String saveAs(String colSeperator, String fileName, boolean append, Charset cs) throws IOException {
        String selectedFile = null;
        if (colSeperator == null) {
            colSeperator = "\t";
        }
        if (fileName == null) {
            fileName = "Untitled" + File.separator + "txt";
            JFileChooser chooser = new JFileChooser(new File(fileName));
            chooser.showSaveDialog(this);
            // Get the selected file
            File f = chooser.getSelectedFile();
            if (f != null) selectedFile = f.getAbsolutePath();
        } else {
            selectedFile = fileName;
        }

        if (selectedFile != null) {
            StringBuilder sb = new StringBuilder(dataWindow1.getDataString(colSeperator));

            sb.append("\r\n");
            sb.append("\r\n");
            if (stateData != null) {
                sb.append(getStatuString(colSeperator));
            } else if (stateString != null) {
                sb.append(stateString.toString());
            }
            String2File(sb.toString(), selectedFile, false,  cs);
        }
        return selectedFile;
    }


    public String saveAsXls(String title, String fileName, boolean append) throws IOException {
        String selectedFile = null;

        if (fileName == null) {
            fileName = "Untitled" + File.separator + "txt";
            JFileChooser chooser = new JFileChooser(new File(fileName));
            chooser.showSaveDialog(this);
            File f = chooser.getSelectedFile();
            if (f != null) selectedFile = f.getAbsolutePath();
        } else {
            selectedFile = fileName;
        }

        if (selectedFile != null) {
            StringBuilder sb = new StringBuilder(dataWindow1.getCsvReport(title, ","));

            sb.append("\r\n");
            sb.append("\r\n");
            if (stateData != null) {
                sb.append(new TableFormaterWriter("", stateData).getCsvString());
            } else if (stateString != null) {
                sb.append(stateString.toString());
            }

            String2File(sb.toString(), selectedFile, false,  Charset.forName("GB2312"));
        }
        return selectedFile;
    }


    private StringBuilder getStatuString(String colSeperator) {
        StringBuilder sb = new StringBuilder();
        TableModel model = stateData;
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (i > 0) sb.append(colSeperator);
            sb.append(model.getColumnName(i));
        }
        sb.append('\n');
        for (int r = 0; r < model.getRowCount(); r++) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (i > 0) sb.append(colSeperator);
                sb.append(model.getValueAt(r, i));
            }
            sb.append('\n');
        }
        return sb;
    }


    public String saveAs(String colSeperator, String fileName) throws IOException {
        return saveAs(colSeperator, fileName, false);
    }

    private static void String2File(String s, String fileName, boolean ab_append, Charset cs) throws IOException {
        FileUtils.writeStringToFile(new File(fileName), s, cs, ab_append);
    }

}

