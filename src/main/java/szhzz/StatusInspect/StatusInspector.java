package szhzz.StatusInspect;

import org.jdesktop.swingx.JXTable;
import szhzz.App.DialogManager;
import szhzz.sql.database.DBException;
import szhzz.sql.database.DataStore;
import szhzz.sql.gui.DataWindow;
import szhzz.sql.gui.DwPanel;
import szhzz.sql.gui.DwToobar_Event;
import szhzz.sql.gui.DwToolBar;
import szhzz.App.AppManager;
import szhzz.App.MessageAbstract;
import szhzz.App.MessageCode;
import szhzz.Calendar.MyDate;
import szhzz.Config.CfgProvider;
import szhzz.Config.Config;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.Utilities;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Vector;

/**
 * Created by Administrator on 2015/1/26.
 */
public class StatusInspector {
    private static DawLogger logger = DawLogger.getLogger(StatusInspector.class);
    private static StatusInspector onlyOne = null;
    private static AppManager App = AppManager.getApp();
    ImageIcon openFile;
    ImageIcon defaultIco;
    ImageIcon starB;
    ImageIcon starG;
    ImageIcon starR;
    ImageIcon exclamation;
    ImageIcon forbidden;
    DataStore ds = null;
    private IdleTimer idleTimer = null;
    private DwPanel StatusDw;
    private Message message = new Message();
    private String[] titles = new String[]{"名称", "必须", "状态", "说明", "代码位置"};
    private DataWindow dw = null;
    private Config cfg = null;
    private boolean inited = false;
    private int secondsToCheck = 60;
    private MyDate checkTime = new MyDate();
    private boolean sendMail = false;
    private int sendMailCount = 0;
    private String errorMsg = null;
    private int errorCount = -1;

    private StatusInspector() {
        App.logEvent("状态监控已经启动");
        loadResource();
        initDw();
        loadDefault();
    }

    public static StatusInspector getInstance() {
        if (onlyOne == null) {
            onlyOne = new StatusInspector();
        }
        return onlyOne;
    }

    public boolean checkRelate(String relate, boolean needed) {

        for (int row = 0; row < ds.getRowCount(); row++) {
            String r = (String) ds.getValueAt(row, "关联", "");// "集群节点"
            if (relate.matches(r)) {
                Object o = ds.getValueAt(row, "status", "true");
                boolean status = Boolean.valueOf(o.toString());
                if (!status) {
                    if (needed) {
                        o = ds.getValueAt(row, "必须", "true");
                        if (!Boolean.valueOf(o.toString())) {
                            continue;
                        }
                    }
                    String name = (String) ds.getValueAt(row, "名称", "");
                    errorMsg = name + " 状态不良";
                    App.logit(errorMsg);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkState(String itemName) {
        boolean status = false;
        int row = ds.find("名称", itemName);
        if(row >= 0){
            Object o = ds.getValueAt(row, "status", "true");
            status = Boolean.valueOf(o.toString());
        }
        return status;
    }

    private void loadDefault() {
        Vector<String> keys = cfg.getKeys();
        for (String key : keys) {
            String comment = cfg.getComment(key);
            if ("必须".equals(comment)) {
                //titles = new String[]{"名称", "必须", "状态", "说明", "代码位置", "必须"};
                acceptReport(new StatusData(key));
            }
        }
        inited = true;
    }

    public void setUI(DwPanel StatusDw) {
        if (this.StatusDw != null) return;

        this.StatusDw = StatusDw;
        StatusDw.addToolbarEvent(new TriggerRetrieve());
        StatusDw.getToolBar().setCanAdd(false);
        StatusDw.getToolBar().setCanDele(false);
//        StatusDw.getToolBar().setCanSave(false);
        StatusDw.getToolBar().setFloatable(false);
        StatusDw.setTitle(null);

        dw = StatusDw.getDataWindow();
        try {
            dw.addCellEditor(1, new JXTable.BooleanEditor());
            dw.addCellRenderer(2, new ImageRenderer());

            dw.shareData(ds, false);
            dw.setSortingStatus(1, -1);
            dw.setSortingStatus(2, 1);

            dw.repaint();
        } catch (DBException e) {
            logger.error(e);
        }
    }

    private void initDw() {

        //    private String[] titles = new String[]{"名称","必须","状态","关联","说明","代码位置"};

        ds = new DataStore();
        int col = 0;
        ds.setColName("名称", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 30);
        ds.setReadOnlyCol(col, true);


        col++;
        ds.setColName("必须", col);
        ds.setColTypeName("Boolean", col);
        ds.setColLength(col, 30);
        if (dw != null) {
            dw.addCellEditor(col, new JXTable.BooleanEditor());
        }

        col++;
        ds.setColName("状态", col);
        ds.setColTypeName("Object", col);
        ds.setColLength(col, 10);
        ds.setReadOnlyCol(col, true);
        if (dw != null) {
            dw.addCellRenderer(col, new ImageRenderer());
        }

        col++;
        ds.setColName("关联", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 50);
        ds.setReadOnlyCol(col, true);
        if (dw != null) {
            dw.addCellRenderer(col, new ImageRenderer());
        }

        col++;
        ds.setColName("监控开始", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 10);


        col++;
        ds.setColName("监控结束", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 10);


        col++;
        ds.setColName("更新时间", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 10);
        ds.setReadOnlyCol(col, true);

        col++;
        ds.setColName("说明", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 1);
        ds.setReadOnlyCol(col, true);

        col++;
        ds.setColName("源", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 1);
        ds.setReadOnlyCol(col, true);

        col++;
        ds.setColName("status", col);
        ds.setColTypeName("Boolean", col);
        ds.setColLength(col, 1);
        ds.setReadOnlyCol(col, true);

        try {
            ds.setReadOnly(false);
            if (dw != null) {
                dw.shareData(ds, false);
            }

        } catch (DBException e) {
            logger.error(e);
        }
    }

    private void loadResource() {
        if (idleTimer != null) return;

        try {
            openFile = AppManager.createImageIcon("/resources/Doc-Edit.gif");
            defaultIco = AppManager.createImageIcon("/resources/refresh.gif");
            starB = AppManager.createImageIcon("/resources/StarB.gif");
            starG = AppManager.createImageIcon("/resources/StarG.gif");
            starR = AppManager.createImageIcon("/resources/StarR.gif");
            exclamation = AppManager.createImageIcon("/resources/Exclamation.gif");
            forbidden = AppManager.createImageIcon("/resources/ForbiddenR.gif");
        } catch (Error e) {
            logger.error(e);
        }
        cfg = CfgProvider.getInstance("Status").getCfg("statusDef");
        idleTimer = new IdleTimer();
        idleTimer.setCircleTime(cfg.getIntVal("CircleTime", secondsToCheck * 1000));
    }

    public int getErrorCount() {
        if (errorCount >= 0) return errorCount;
        errorCount = 0;
        for (int row = 0; row < ds.getRowCount(); row++) {
            Object o = ds.getValueAt(row, "status");
            boolean health = !(o != null && !(boolean) o);
            if (!health) errorCount++;
        }
        return errorCount;
    }

    private void acceptReport(StatusData report) {
        if (report == null) return;

        boolean needed = true;
//        String[] vals = report.toString().split("\t");
//        String name = vals[0];
        Object startTime = null;
        Object endTime = null;
        String comment = "";
        int row = ds.find("名称", report.name);
//        if("招商证券.慧网宁灼1号".equals(report.name)){
//            int a = 0;
//        }
        if (row < 0) {
            row = ds.appendRow();
            ds.setValueAt_s(report.name, row, "名称");

            comment = cfg.getComment(report.name);
            needed = ("必须".equals(comment));

            String[] times = cfg.getProperty(report.name, "").split("-");

            if (times.length > 0 && checkTime.setTime(times[0])) {
                startTime = times[0];
                ds.setValueAt_s(startTime, row, "监控开始");
                if (times.length > 1 && checkTime.setTime(times[1])) {
                    endTime = times[1];
                    ds.setValueAt_s(endTime, row, "监控结束");
                }
            }

            ds.setValueAt_s(needed, row, "必须");
        } else {
            needed = (boolean) ds.getValueAt(row, "必须");
            startTime = ds.getValueAt(row, "监控开始");
            endTime = ds.getValueAt(row, "监控结束");
        }
        ds.setValueAt_s(report.relate, row, "关联");

        ds.setValueAt(MyDate.getToday().getTime(), row, "更新时间");

        boolean ok = true;

        if (!report.status && report.forceAlarm) {
            needed = true;
            ok = false;
        } else {
            ok = Boolean.valueOf(report.status);
        }


        ds.setValueAt(ok ? starG : (needed && isWithinTime(startTime, endTime) ? exclamation : starB), row, "状态");
        boolean health = ok ? true : (needed && isWithinTime(startTime, endTime) ? false : true);
        ds.setValueAt(health, row, "status");

        ds.setValueAt_s(report.note, row, "说明");
        ds.setValueAt_s(report.locate, row, "源");

//        if("TickSnap数据".equals(report.name)){
//                App.SendMail(report.toMessage(), null);
//        }
        //SendMail = SendMail || report.SendMail;
        if (inited && !ok && needed) {
            if (!isWithinTime(startTime, endTime)) return;

            DialogManager.getInstance().openStatuesView();
            try {
                if (MyDate.getToday().isOpenTime(300)) {
                    if ("集群节点".equals(report.relate)) {
                        sendMail = true;
                        Utilities.playSound("\\resources\\AlertSound\\alert.wav");
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }

        if (dw != null) {
            dw.repaint(100);
        }
        errorCount = -1;
    }

    private boolean isWithinTime(Object startTime, Object endTime) {
        if (startTime != null && MyDate.IS_BEFORE_TIME(startTime.toString())) return false;
        if (endTime != null && MyDate.IS_AFTER_TIME(endTime.toString())) return false;
        return true;
    }

    private void onUpdate() {
        for (int row = 0; row < ds.getRowCount(); row++) {
            String name = (String) ds.getValueAt(row, "名称");
            Object startTime = ds.getValueAt(row, "监控开始");
            Object endTime = ds.getValueAt(row, "监控结束");
            Object egnal = ds.getValueAt(row, "必须");
            String comment = "";
            if (egnal != null && (boolean) egnal) {
                comment = "必须";
            }
            if (startTime == null) {
                cfg.setProperty(name, "", comment);
            } else {
                if (endTime == null) {
                    cfg.setProperty(name, startTime.toString(), comment);
                } else {
                    cfg.setProperty(name, startTime.toString() + "-" + endTime.toString(), comment);
                }
            }
        }
        cfg.save();
    }

    private void onRetrieve() {
        message.sendMessage(MessageCode.QueryStatus, this);
        logErrorStatus();
    }

    void logErrorStatus() {
        StatusData d = new StatusData("Errors");
        d.status = (DawLogger.getErrorCount() == 0);
        d.note = "程序累计错误数 " + DawLogger.getErrorCount();
        d.locate = "DawLogger";
        acceptReport(d);
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    private class TriggerRetrieve implements DwToobar_Event {
        public boolean toolbarClicked(int key, DwToolBar tbar, Object parms) {
            if (DwToolBar.B_RETRIEVE == key) {
                onRetrieve();
            } else if (DwToolBar.B_UPDATE == key) {
                onUpdate();
            }
            return false;
        }
    }

    private class Message extends MessageAbstract {
        @Override
        public boolean acceptMessage(MessageCode messageID, Object caller, Object message) {
            if (caller == this) return false;


            switch (messageID) {
                case ReportStatus:
                    if (message instanceof StatusData) {
                        acceptReport((StatusData) message);
                    } else {
                        App.logEvent("错误! " + message.toString());
                    }
                    return true;

            }

            return false;
        }
    }

    class ImageRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            setIcon((ImageIcon) value);
            setHorizontalAlignment(JLabel.CENTER);
            setText("");
            return this;
        }
    }

    public void sendPicMail() {
//        StatusView statusView = DialogManager.getInstance().getStatuesView();
//        if (statusView != null) {
//            statusView.sentMail();
//        }
    }

    class IdleTimer extends CircleTimer {
        IdleTimer() {
            setTitle("Status inspector");
        }

        @Override
        public void execTask() {
            if (++sendMailCount > 5) {
                if (sendMail) {
                    sendPicMail();
                }
                sendMailCount = 0;
            }
            sendMail = false;
            circleTime();
            cfg.reLoad();
            onRetrieve();
        }
    }


}
