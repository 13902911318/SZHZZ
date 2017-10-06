package szhzz.App;

import szhzz.Config.Config;
import szhzz.Utils.NU;
import szhzz.sql.database.DBException;
import szhzz.sql.database.DataStore;
import szhzz.sql.gui.DataWindow;
import szhzz.sql.gui.DwPanel;
import szhzz.sql.gui.DwToobar_Event;
import szhzz.sql.gui.DwToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

public class TaskView extends JDialog {
    private static final String profile = "TaskView";
    DataStore ds = null;
    private JPanel contentPane;
    private DwPanel taskViewDwPanel;
    private DataWindow dw;

    public TaskView(Frame owner) {
        super(owner);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        setContentPane(contentPane);
        setModal(true);

        dw = taskViewDwPanel.getDataWindow();

        setUiTable();
    }

    public static void main(String[] args) {
        TaskView dialog = new TaskView(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void fresh() {
        ds.clear();
        RunCell r = null;
        Vector<RunCell> tasks = RunCell.getPooledTasks();
        for (int i = 0; i < tasks.size(); i++) {
            try {
                r = tasks.get(i);
            } catch (Exception e) {
                break;
            }
            if (r == null) break;
            int row = ds.appendRow();
            ds.setValueAt(r.getStartTime(), row, 0);
            ds.setValueAt(r.getTask().toString(), row, 1);
            ds.setValueAt(r.isManaged(), row, 2);
            ds.setValueAt(r.printStackTrace(), row, 3);
        }
        for (String s : RunCell.past) {
            int row = ds.appendRow();
            ds.setValueAt(" 已结束", row, 0);
            ds.setValueAt(s, row, 1);
        }
        taskViewDwPanel.setStatus(" Managed=" + RunCell.getManagedNo() + " System=" + RunCell.getSysTaskNo());
        dw.repaintDataWindow();
    }

    private void onOK() {
        savePref();
        dispose();
    }

    private void setUiTable() {
        taskViewDwPanel.getToolBar().setCanSave(false);
        taskViewDwPanel.getToolBar().setCanDele(false);
        taskViewDwPanel.getToolBar().setCanAdd(false);
        taskViewDwPanel.setTitle(null);
        taskViewDwPanel.addToolbarEvent(new TriggerRetrive());
        taskViewDwPanel.getToolBar().setFloatable(false);

        if (ds == null)
            ds = new DataStore();

        int c = 0;
        ds.setColName("开始时间", c);
        ds.setColTypeName("String", c);
        ds.setColLength(c, 20);

        c++;
        ds.setColName("运行任务", c);
        ds.setColTypeName("String", c);
        ds.setColLength(c, 100);

        c++;
        ds.setColName("监管", c);
        ds.setColTypeName("Boolean", c);
        ds.setColLength(c, 10);

        c++;
        ds.setColName("源", c);
        ds.setColTypeName("String", c);
        ds.setColLength(c, 100);


        ds.setReadOnly(true);

        try {
            dw.setFilterNewRow(true);
            dw.shareData(ds);
        } catch (DBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void loadPref() {
        Config appCfg = AppManager.getApp().getPreferCfg(this.getClass());


        Point p = this.getLocation();
        this.setBounds(
                appCfg.getIntVal("WindowX", (int) p.getX()),
                appCfg.getIntVal("WindowY", (int) p.getY()),
                appCfg.getIntVal("WindowW", this.getWidth()),
                appCfg.getIntVal("WindowH", this.getHeight())
        );

        String[] colWidth = appCfg.getProperty("ColWidths", "").split("\t");
        for (int c = 0; c < colWidth.length; c++) {
            dw.setColumnWidth(c, NU.parseLong(colWidth[c], (long) dw.getColumnWidth(c)).intValue());
        }
    }

    void savePref() {
        Config appCfg = AppManager.getApp().getPreferCfg(this.getClass());
        Rectangle p = this.getBounds();

        appCfg.setProperty("SubAccountView", "yes");
        int x = (int) p.getX();
        if (x > 1920) x = 1;
        appCfg.setProperty("WindowX", "" + x);
        appCfg.setProperty("WindowY", "" + (int) p.getY());
        appCfg.setProperty("WindowW", "" + (int) p.getWidth());
        appCfg.setProperty("WindowH", "" + (int) p.getHeight());

        StringBuffer sb = new StringBuffer();
        for (int c = 0; c < dw.getColumnCount(); c++) {
            if (c > 0) sb.append("\t");
            sb.append(dw.getColumnWidth(c));
        }
        appCfg.setProperty("ColWidths", "" + sb.toString());

        appCfg.save();
    }

    private class TriggerRetrive implements DwToobar_Event {
        public boolean toolbarClicked(int key, DwToolBar tbar, Object parms) {
            if (DwToolBar.B_RETRIEVE == key) {
                fresh();
            } else {
                return true;
            }
            return false;
        }
    }
}
