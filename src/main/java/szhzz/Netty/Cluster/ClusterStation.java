package szhzz.Netty.Cluster;


import szhzz.App.DialogManager;
import szhzz.Utils.Internet;
import szhzz.sql.database.DBException;
import szhzz.sql.database.DataStore;
import szhzz.sql.gui.DataWindow;
import szhzz.sql.gui.DwPanel;
import szhzz.App.AppManager;
import szhzz.Calendar.MyDate;
import szhzz.Config.CfgProvider;
import szhzz.Config.Config;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.CaptureScreen;
import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ClusterStation extends JDialog {
    private static DawLogger logger = DawLogger.getLogger(ClusterStation.class);
    JCheckBox startAllCheckBox;
    JCheckBox forceTakeover;
    MyDate dumDate = new MyDate();
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonSave;
    private DwPanel stationViewPanel;
    private JCheckBox offLineButtom;
    private JComboBox comboBoxGroup;

    private JLabel text1;
    private DataWindow dw;
    private DataStore ds;
    JCheckBox silent = null;

//    private JButton broadcast;
    //    private static boolean independent = false;
    StatusReportMail statusReportMail;

    public ClusterStation(Frame frame) {
        super(frame);
        this.setTitle("集群监控中心");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        initDw();

        if (statusReportMail == null) {
            statusReportMail = new StatusReportMail();
            statusReportMail.setCircleTime(60000);
        }

        stationViewPanel.getToolBar().addSeparator();
//        ImageIcon openFile = AppManager.createImageIcon("/resources/Rss1.gif");
//        broadcast = new JButton();
//        broadcast.setIcon(openFile);
//        broadcast.setToolTipText("发布数据");
//        broadcast.setEnabled(true);
//        stationViewPanel.getToolBar().add(broadcast);

        Config cfg = CfgProvider.getInstance("Schedule").getCfg("System");

        comboBoxGroup.setSelectedIndex(Cluster.getInstance().getGroup());

        startAllCheckBox = new JCheckBox("自动接管交易");
        startAllCheckBox.setSelected(cfg.getBooleanVal("自动接管交易", true));
        stationViewPanel.getToolBar().addSeparator();
        stationViewPanel.getToolBar().add(startAllCheckBox);

        forceTakeover = new JCheckBox("强制接管交易");
        forceTakeover.setSelected(Cluster.getInstance().isForceTakeover());
        stationViewPanel.getToolBar().add(forceTakeover);

        silent = new JCheckBox("不再提示");
        silent.setToolTipText("不再弹出提示窗口");
        silent.setSelected(false);
        stationViewPanel.getToolBar().add(silent);

        silent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogManager.setClusterStationSilent(silent.isSelected());
            }
        });

//        broadcast.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                AccountsManager.getInstance().broadcastReckoning();
//                AccountsManager.getInstance().synchronizeSubAccount();
//                BusinessRuse.getInstance().broadCastUpdateFiles();
//            }
//        });

        startAllCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Cluster.getInstance().setAutoStartTrade(startAllCheckBox.isSelected());
                Config cfg = CfgProvider.getInstance("Schedule").getCfg("System");
                cfg.setProperty("自动接管交易", startAllCheckBox.isSelected());
            }
        });

        forceTakeover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Cluster.getInstance().setForceTakeover(forceTakeover.isSelected());
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Config cfg = CfgProvider.getInstance("Schedule").getCfg("System");
                cfg.setProperty("自动接管交易", startAllCheckBox.isSelected());
                cfg.setProperty("强制接管交易", forceTakeover.isSelected());
                cfg.setProperty("离线", Cluster.getInstance().isOffLine());
//                cfg.setProperty("设为交易代理", Cluster.connectToProxy());
                cfg.save();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        offLineButtom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Cluster.getInstance().setOffLine(offLineButtom.isSelected());
            }
        });
        offLineButtom.setSelected(Cluster.getInstance().isOffLine());

        comboBoxGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Cluster.getInstance().changeGroup(NU.parseInt(comboBoxGroup.getSelectedItem(), 0));
            }
        });

//        if (Cluster.connectToProxy()) {
//            proxyCheckBox.setEnabled(false);
//            proxyCheckBox.setSelected(false);
//        } else {
//            proxyCheckBox.setSelected(Cluster.connectToProxy());
//        }
//        proxyCheckBox.addActionListener(new ActionListener() {
//            @Override
////            public void actionPerformed(ActionEvent e) {
////                Cluster.setProxy(proxyCheckBox.isSelected());
////            }
//        });

        Cluster.getInstance().setClusterStationUI(this);
    }

    public static void main(String[] args) {
        ClusterStation dialog = new ClusterStation(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void set7522Model(){
        startAllCheckBox.setVisible(false);
        forceTakeover.setVisible(false);
        offLineButtom.setVisible(false);
        comboBoxGroup.setVisible(false);
//        proxyCheckBox.setVisible(false);
        text1.setVisible(false);
    }

    public void setSilentCheckBoxVisible(boolean visibale){
        silent.setVisible(visibale);
    }

    void initDw() {
        dw = stationViewPanel.getDataWindow();
        ds = new DataStore();
        int col = 0;
        RowsCellRenderer numberRenderer = new RowsCellRenderer();

        ds.setColName("名称", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 1);
        ds.setDefaltValues(col, "");
        dw.addCellRenderer(col, numberRenderer);

        col++;
        ds.setColName("IP", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 10);
        ds.setDefaltValues(col, "");
        dw.addCellRenderer(col, numberRenderer);

        col++;
        ds.setColName("InterNet", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 10);
        ds.setDefaltValues(col, "");
        dw.addCellRenderer(col, numberRenderer);

        col++;
        ds.setColName("级别", col);
        ds.setColTypeName("Integer", col);
        ds.setColLength(col, 10);
        ds.setDefaltValues(col, -1);
        dw.addCellRenderer(col, numberRenderer);

        col++;
        ds.setColName("分组", col);
        ds.setColTypeName("Integer", col);
        ds.setColLength(col, 10);
        ds.setDefaltValues(col, -1);

        col++;
        ds.setColName("已连接", col);
        ds.setColTypeName("Boolean", col);
        ds.setColLength(col, 10);
        ds.setDefaltValues(col, false);

        col++;
        ds.setColName("收盘日期", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 10);
        ds.setDefaltValues(col, "");

        col++;
        ds.setColName("离线", col);
        ds.setColTypeName("Boolean", col);
        ds.setColLength(col, 10);
        ds.setDefaltValues(col, true);

        col++;
        ds.setColName("交易中", col);
        ds.setColTypeName("Boolean", col);
        ds.setColLength(col, 10);
        ds.setDefaltValues(col, false);
//        dw.addCellRenderer(col, numberRenderer);

//        col++;
//        ds.setColName("代理", col);
//        ds.setColTypeName("String", col);
//        ds.setColLength(col, 10);
//        ds.setDefaltValues(col, "");

        col++;
        ds.setColName("远程关机", col);
        ds.setColTypeName("Boolean", col);
        ds.setColLength(col, 5);
        ds.setDefaltValues(col, false);

        col++;
        ds.setColName("错误", col);
        ds.setColTypeName("Integer", col);
        ds.setColLength(col, 10);
        ds.setDefaltValues(col, 0);

        col++;
        ds.setColName("最近更新", col);
        ds.setColTypeName("String", col);
        ds.setColLength(col, 10);
        dw.addCellRenderer(col, numberRenderer);

        try {
            dw.shareData(ds, false);
        } catch (DBException e) {
            logger.error(e);
        }
        dw.setSortingStatus(4, 1);
        dw.setSortingStatus(3, -1);

        loadPref();
    }

    private void onOK() {
        savePref();
        dispose();
    }

    private void onCancel() {
        savePref();
        dispose();
    }

    public synchronized void dataChanged(String stationName) {
        String stationName_ = stationName.toUpperCase();
        int row = ds.find("名称", stationName_);
        if (row < 0) {
            row = ds.appendRow();
            ds.setValueAt(stationName_, row, "名称");
        }
        ds.setValueAt(AppManager.getApp().getIP(null), row, "IP");
        ds.setValueAt(Cluster.getInstance().getLocalLevel(), row, "级别");
        ds.setValueAt(true, row, "已连接");
        ds.setValueAt(BusinessRuse.getInstance().getCloseDate(), row, "收盘日期");
        ds.setValueAt(Cluster.getInstance().getGroup(), row, "分组");
        ds.setValueAt(Cluster.getInstance().isOnTrade(), row, "交易中");
//        ds.setValueAt(Cluster.getTradeProxyHost(), row, "代理");
        ds.setValueAt(Cluster.getInstance().isOffLine(), row, "离线");
        ds.setValueAt(AppManager.getApp().canRemoteShutdown(), row, "远程关机");
        ds.setValueAt(BusinessRuse.getInstance().getErrorCode(), row, "错误");
        ds.setValueAt(Internet.getPublicIp(), row, "InterNet");

        ds.setValueAt(MyDate.getToday().getDateTime(), row, "最近更新");

        dw.repaint(100);
    }

    public synchronized void dataChanged(ClusterProperty ss) {
        String stationName = ss.stationName.toUpperCase();
        int row = ds.find("名称", stationName);
        if (row < 0) {
            row = ds.appendRow();
            ds.setValueAt(stationName, row, "名称");
        }
        ds.setValueAt(ss.ipAddress, row, "IP");
        ds.setValueAt(ss.connected, row, "已连接");
        ds.setValueAt(ss.closeDate, row, "收盘日期");
        ds.setValueAt(ss.level, row, "级别");
        ds.setValueAt(ss.onTrade, row, "交易中");
//        ds.setValueAt(ss.tradeProxy, row, "代理");
        ds.setValueAt(ss.group, row, "分组");
        ds.setValueAt(ss.offline, row, "离线");
        ds.setValueAt(ss.canShutdown, row, "远程关机");
        ds.setValueAt(ss.errorCode, row, "错误");
        ds.setValueAt(ss.internetIP, row, "InterNet");

        //dumDate.setDateTime(ss.lastUpdate);

        if (ss.connected) {
//            String timeString = (MyDate.getToday().getSecond() - dumDate.getSecond()) + "." +
//                    (MyDate.getToday().getMillisOfSecond() - dumDate.getMillisOfSecond());
            if(ss.bypass){
                ds.setValueAt("(B)"+ss.lastUpdate + " (" + ss.timeLap + " Ms)", row, "最近更新");
            }else{
                ds.setValueAt(ss.lastUpdate + " (" + ss.timeLap + " Ms)", row, "最近更新");
            }
        }
        stationViewPanel.setStatus(Cluster.getInstance().getStatusMessage());
        dw.repaint(100);
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
        int cols = Math.min(dw.getColumnCount(),colWidth.length);
        for (int c = 0; c < cols; c++) {
            dw.setColumnWidth(c, NU.parseLong(colWidth[c], (long) dw.getColumnWidth(c)).intValue());
        }
    }

    private void savePref() {
        Config appCfg = AppManager.getApp().getPreferCfg(this.getClass());

        Rectangle p = this.getBounds();


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

    public DwPanel getStationViewPanel() {
        return stationViewPanel;
    }

//    public static boolean isIndependent() {
//        return independent;
//    }
//
//    public static void setIndependent(boolean independent) {
//        ClusterStation.independent = independent;
//    }

    public class RowsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                boolean marketCloseError = false;

                if (MyDate.getToday().isOpenDay() &&
                        (MyDate.IS_AFTER_TIME(15, 0, 0) || MyDate.IS_BEFORE_TIME(9, 15, 0))) {
                    String s = dw.getValueAt(row, "收盘日期").toString();
                    if (s != null && !s.isEmpty() && MyDate.getLastOpenDay().compareDays(s) != 0) {
                        marketCloseError = true;
                    }
                }
                if (!(boolean) dw.getValueAt(row, "已连接", false)) {
                    label.setForeground(Color.GRAY);
                } else if (marketCloseError || (Integer) dw.getValueAt(row, "错误", 0) > 0) {
                    label.setForeground(Color.RED);
                } else if ((boolean) dw.getValueAt(row, "交易中", false)) {
                    label.setForeground(Color.BLUE);
                } else if ((Integer) dw.getValueAt(row, "级别", -1) < 0) {
                    label.setForeground(Color.RED);
                } else {
                    label.setForeground(Color.BLACK);
                }
            }
            return c;
        }
    }

    public void sentMail() {
        this.setVisible(true);
        Rectangle p = this.getBounds();
        String file = "ClusterStation.png";

        try {
            File f = CaptureScreen.captureScreen(p, file);
            AppManager.getApp().sendMail("集群状态监测", f.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    class StatusReportMail extends CircleTimer {

        boolean synTime = false;


        @Override
        public void execTask() {
            if (MyDate.IS_AFTER_TIME(15, 15, 0)) {
                return;
            }
            if (Cluster.getInstance().isOnTrade()) {
                try {
                    sentMail();
                } finally {
                    if (!synTime) {
                        MyDate dt = new MyDate();
                        dt.now();
                        int m = dt.getMinute();
                        for (int t = 0; t <= 45; t += 15) {
                            if (m > t && m <= t + 15) {
                                m = t + 15;
                                break;
                            }
                        }
                        m = m - dt.getMinute();
                        setCircleTime(m * 60 * 1000);
                        synTime = true;

                    } else {
                        if (MyDate.IS_AFTER_TIME(15, 15, 0)) {
                            setCircleTime(15 * 60 * 1000); //15分钟
                        }
                    }
                }
            }
        }
    }
}
