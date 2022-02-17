package szhzz.Config;

import szhzz.App.AppManager;
import szhzz.App.MessageAbstract;
import szhzz.App.MessageCode;
import szhzz.Calendar.MyDate;
import szhzz.Files.TextTransfer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.Utilities;
import szhzz.sql.database.DBException;
import szhzz.sql.database.DataStore;
import szhzz.sql.database.Database;
import szhzz.sql.gui.*;
import szhzz.sql.jdbcpool.DbStack;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-4-5
 * Time: 下午1:31
 * To change this template use File | Settings | File Templates.
 */
public class ConfigUI {
    static DawLogger logger = DawLogger.getLogger(ConfigUI.class);
    protected Config cfg = null;
    protected DwPanel cfgEditor;
    protected DataStore ds = null;
    protected Vector<String> enabledItems = null;
    protected DataWindow dw = null;
    JButton buttonCfgFile;
    JButton buttonLock;
    JButton compareClipboard;
    JButton copyCfg;
    JButton pastCfg;
    private JButton newConfig;
    //    JButton compareCfg;
    ImageIcon lookIcon;
    ImageIcon unLookIcon;
    ImageIcon compareClipboardIcon;
    ImageIcon copyIcon;
    ImageIcon pastIcon;
    ImageIcon newCfgIcon;
    private static TextTransfer ClipboardReader = null;
    private boolean clearBeforeSave = false;
    QuickPinYinComboBox dbCfgComboBox;

    public void setEnabledItems(Vector<String> enabledItems) {
        this.enabledItems = enabledItems;
    }

    public void setCfgEditor(DwPanel cfgEditor) {
        this.cfgEditor = cfgEditor;
        setUiTable();
        initDw();
    }

    public void setCfgFile(String file) {
        if (file == null || "".equals(file) || !new File(file).exists()) return;

        if (cfg == null) {
            this.cfg = new ConfigF();
        }
        cfg.load(file);
        onRetrieve();
    }


    public void setCfgFile(Config cfg) {
        if (cfg != null) {
            this.cfg = cfg;
            onRetrieve();

            dbCfgComboBox.setVisible(cfg instanceof DbConfig);
            newConfig.setVisible(cfg instanceof DbConfig);

            if (cfg instanceof DbConfig) {
                if (dbCfgComboBox.getItemCount() == 0) {
                    LinkedList<String> list = DbCfgProvider.getInstance().getCfgIDs();
                    Hashtable<String, String> elements = new Hashtable<>();
                    for (String e : list) {
                        elements.put(e, e);
                    }
                    if (elements.get(cfg.configID) == null) {
                        elements.put(cfg.configID, cfg.configID);
                    }
                    try {
                        dbCfgComboBox.setValues(elements.keys());
                    } catch (Exception e) {
                        int a = 0;
                    }
                }
//                boolean found = false;
//                for (int i = 0; i < dbCfgComboBox.getItemCount(); i++) {
//                    if(dbCfgComboBox.getItemAt(i).equals(cfg.getConfigID())){
//                        dbCfgComboBox.setSelectedIndex(i);
//                        found = true;
//                        break;
//                    }
//                }
//                if(!found){
//                    dbCfgComboBox.addItem(cfg.configID);
//                }
            }
        }
    }

    public void setUiEnable(boolean enabled) {
        //nothing to do
    }

    protected void initDw() {
        lookIcon = AppManager.createImageIcon("/resources/Lock.gif");
        unLookIcon = AppManager.createImageIcon("/resources/key.gif");
        compareClipboardIcon = AppManager.createImageIcon("/resources/equal.gif");
        copyIcon = AppManager.createImageIcon("/resources/Clipboard Copy.gif");
        pastIcon = AppManager.createImageIcon("/resources/Clipboard Paste.gif");
        newCfgIcon = AppManager.createImageIcon("/resources/Wizard.gif");

        cfgEditor.addToolbarEvent(new triggerRetrive());
        cfgEditor.getToolBar().setFloatable(false);
        cfgEditor.setTitle("");
//        cfgEditor.setCanAdd(false);
//        cfgEditor.setCanDele(false);
        cfgEditor.setCanRetrieve(true);
//        cfgEditor.setCanSave(false);
        dw = cfgEditor.getDataWindow();

        buttonCfgFile = new JButton();
        buttonCfgFile.setIcon(new ImageIcon(getClass().getResource("/resources/Doc-Edit.gif")));
        buttonCfgFile.setToolTipText("打开文件");
        buttonCfgFile.setEnabled(false);
        cfgEditor.getToolBar().addSeparator();
        cfgEditor.getToolBar().add(buttonCfgFile);

        copyCfg = new JButton();
        copyCfg.setIcon(copyIcon);
        copyCfg.setToolTipText("拷贝文件到剪贴板");
        copyCfg.setEnabled(true);
        cfgEditor.getToolBar().add(copyCfg);

        pastCfg = new JButton();
        pastCfg.setIcon(pastIcon);
        pastCfg.setToolTipText("粘帖文本");
        pastCfg.setEnabled(true);
        cfgEditor.getToolBar().add(pastCfg);

        compareClipboard = new JButton();
        compareClipboard.setIcon(compareClipboardIcon);
        compareClipboard.setToolTipText("与剪贴板比较");
        compareClipboard.setEnabled(true);
        cfgEditor.getToolBar().add(compareClipboard);

        buttonLock = new JButton();
        buttonLock.setIcon(lookIcon);
        buttonLock.setToolTipText("锁定");
        buttonLock.setEnabled(false);
        cfgEditor.getToolBar().add(buttonLock);


        dbCfgComboBox = new QuickPinYinComboBox();
        cfgEditor.getToolBar().add(dbCfgComboBox);
        dbCfgComboBox.setMaximumSize(new Dimension(200, 25));
        dbCfgComboBox.setVisible(false);

        newConfig = new JButton();
        newConfig.setIcon(newCfgIcon);
        newConfig.setToolTipText("新建配置文件");
        newConfig.setEnabled(true);
        cfgEditor.getToolBar().add(newConfig);
        newConfig.setVisible(false);

        dbCfgComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxEdited")) {
//                if(isDirty()){
//                    onWrite();
//                }
                    String cfgID = dbCfgComboBox.getCurrentValue(0).toString();
                    setCfgFile(DbCfgProvider.getInstance().getCfg(cfgID, false));
                }
            }
        });

        copyCfg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ClipboardReader == null) {
                    ClipboardReader = new TextTransfer();
                }
                ClipboardReader.setClipboardContents(cfg.getTxt());

            }
        });

        pastCfg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ClipboardReader == null) {
                    ClipboardReader = new TextTransfer();
                }
                String data = ClipboardReader.getClipboardContents();
                cfg.loadDataVal(data);
                onRetrieve(false);
            }
        });

        compareClipboard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                compareWithClipboard();
            }
        });

        newConfig.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newName = (String) JOptionPane.showInputDialog(
                        null,
                        "Create a Config, Name:",
                        "Create a Config",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        null);

                if (newName == null || newName.length() == 0) return;

                int row = find(newName);
                if(row > 0){
                    dbCfgComboBox.setSelectedIndex(row);
                }else{
                    Vector<String> newItem= new Vector<>();
                    newItem.add(newName);
                    dbCfgComboBox.addNewItem(newItem, 0);
                    cfg = DbCfgProvider.getInstance().getCfg(newName, true);
                    cfg.setProperty("简述", "在此添加简述");
                    onRetrieve(false);
                }
            }
        });


        buttonCfgFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (cfg != null) {
                        if (cfg instanceof DbConfig) {
                            AppManager.MessageBox("存储于数据库的配置文件,\n请用拷贝粘帖的方式进行文本编辑", 10);
                            return;
                        }
                        String s = Utilities.programX86Dir();
                        String url = cfg.getConfigUrl();
                        Runtime.getRuntime().exec(s + "\\EmEditor\\EmEditor.exe " + url);
                    }
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null,
                            "Error attempting to launch web browser\n" + e1.toString());
                }
            }
        });


        buttonLock.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (cfg != null) {
                        boolean looked = cfg.getBooleanVal("锁定", false);
                        cfg.setProperty("锁定", !looked, looked ? "缺省为false, 锁定后文件不可修改." : "锁定日期 " + MyDate.getToday().getDateTime());
                        cfg.save();
                        onRetrieve();
                    }
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null,
                            "Error attempting to launch web browser\n" + e1.toString());
                }
            }
        });


    }


    public void setUiTable() {
        RowsCellRenderer numberRenderer = new RowsCellRenderer();
        dw = cfgEditor.getDataWindow();

        if (ds == null) {
            int col = 0;
            ds = new DataStore();
            ds.setColName("项目", col);
            ds.setColTypeName("String", col);
            ds.setColLength(col, 10);
            dw.addCellRenderer(col, numberRenderer);

            col++;
            ds.setColName("数值", col);
            ds.setColTypeName("String", col);
            ds.setColLength(col, 10);
            dw.addCellRenderer(col, numberRenderer);

            col++;
            ds.setColName("备注", col);
            ds.setColTypeName("String", col);
            ds.setColLength(col, 100);
            ds.setReadOnly(false);
            dw.addCellRenderer(col, numberRenderer);

            col++;
            ds.setColName("对比", col);
            ds.setColTypeName("String", col);
            ds.setColLength(col, 100);
            ds.setReadOnly(false);
//            dw.addCellRenderer(col, numberRenderer);


//            dm = new dbTableModel();
//            dm.setDataStore(ds);
        }
        try {
            cfgEditor.getDataWindow().shareData(ds, false);
        } catch (DBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void compareWithClipboard() {
        if (ClipboardReader == null) {
            ClipboardReader = new TextTransfer();
        }

        String text = ClipboardReader.getClipboardContents();
        ConfigF cfg2 = new ConfigF();
        cfg2.loadDataVal(text);
        compare(cfg2);
    }

    public void compare(Config otherCfg) {
//        dw.removeEditor();
        boolean isReadOnly = false;
        try {


            onRetrieve();
            dw.resetVisibleCols(new int[]{0, 1, 2, 3});

            isReadOnly = dw.isDataWindowReadOnly();
            if (isReadOnly) {
                dw.setDataWindowReadOnly(false);
            }
            Vector<String> checkedKey = new Vector<String>();
            for (int row = 0; row < dw.getRowCount(); row++) {
                Object key = dw.getValueAt(row, "项目");
                if (key != null) {
                    checkedKey.add(key.toString());
                    Object o = dw.getValueAt(row, "数值");
                    if (o == null || "".equals(o) || "''".equals(o) || "\"\"".equals(o)) {
                        o = "NULL";
                    }

                    String v = otherCfg.getProperty(key.toString(), "NULL");
                    if (o.equals(v)) {
                        dw.setValueAt("==", row, "对比");
                    } else {
                        dw.setValueAt("!= " + v, row, "对比");
                    }
                }
            }


            for (Enumeration e = otherCfg.getEnumeration(); e.hasMoreElements(); ) {
                Object key = e.nextElement();
                if (checkedKey.contains(key.toString())) continue;

                String v = otherCfg.getProperty(key.toString(), "");
                int row = dw.find("项目", key);
                if (row < 0) {
                    row = dw.appendRow();
                    dw.setValueAt("+ " + key + " = " + v, row, "对比");
                } else {
                    Object o = dw.getValueAt(row, "数值");
                    if (o == null || !v.equals(o.toString())) {
                        dw.setValueAt("!= " + v, row, "对比");
                    }
                }
            }
        } finally {
            if (isReadOnly) {
                dw.setDataWindowReadOnly(isReadOnly);
            }
        }
    }

    protected void onRetrieve() {
        onRetrieve(true);
    }

    protected void onRetrieve(boolean relaod) {
        dw.removeEditor();
        boolean looked = false;
        if (cfg != null) {//&& dm != null
            if (relaod) cfg.reLoad();
            ds.clear();

            int r;

            LinkedList<Config.item> list = cfg.getIndex();

            for (Config.item i : list) {
                if ("Protect".equals(i.getComment())) continue;

                r = ds.appendRow();

                ds.setValueAt(i.name, r, "项目");
                ds.setValueAt(i.getValue(), r, "数值");
                ds.setValueAt(i.getComment(), r, "备注");
            }
            cfgEditor.getDataWindow().repaintDataWindow();
            buttonCfgFile.setEnabled(true);
            buttonLock.setEnabled(true);
            looked = cfg.getBooleanVal("锁定", false);
        }

        dw.resetVisibleCols(new int[]{0, 1, 2});
        dw.setDataWindowReadOnly(looked);

        if (looked) {
            buttonLock.setIcon(unLookIcon);
            buttonLock.setToolTipText("解锁");
        } else {
            buttonLock.setIcon(lookIcon);
            buttonLock.setToolTipText("锁定");
        }

    }


    public boolean isPropertyChanged(String prop) {
        DataWindow dw = cfgEditor.getDataWindow();
        dw.removeEditor();
        int row = dw.find("项目", prop);
        if (row >= 0) {
            Object v = dw.getValueAt(row, "数值");
            if (cfg != null) {
                String p = cfg.getProperty(prop, "");
                return !p.equals(v);
            }
        }
        return false;
    }

    public void onWrite() {
        if (!(cfg instanceof DbConfig) && cfgEditor.getDataWindow().getRowCount() == 0) return;

        cfgEditor.getDataWindow().removeEditor();
        if (cfg != null) {
            if (clearBeforeSave) {
                cfg.clear();
            }
            DataStore deletedRows = ds.getDeleteRows();
            for (int i = 0; deletedRows != null && i < deletedRows.getRowCount(); i++) {
                Object c1 = deletedRows.peekObject(i, 0);
                if (c1 != null) {
                    cfg.removeProperty(c1.toString());
                }
            }
            for (int i = 0; i < ds.getRowCount(); i++) {
                Object c1 = ds.peekObject(i, 0);
                Object c2 = ds.peekObject(i, 1);
                Object c3 = ds.peekObject(i, 2);
                if (c1 != null && c2 != null) {
                    if (c3 == null) {
                        cfg.setProperty(c1.toString(), c2.toString(), null);
                    } else {
                        if (!c3.toString().contains("# LastUpdate")) {
                            cfg.setProperty(c1.toString(), c2.toString(), c3.toString());
                        }
                    }
                }
            }
            cfg.save();
            MessageAbstract.getInstance().sendMessage(MessageCode.ConfigFileChanged, cfg);
        }
    }

    boolean onDelete() {
        DataWindow dw = cfgEditor.getDataWindow();
        dw.removeEditor();
        int row = dw.getSelectedRow();

        Object itemName = dw.getValueAt(row, "项目");
        if (itemName != null) {
            cfg.removeProperty(itemName.toString());
        }
        return true;
    }

    public Config getCfg() {
        return cfg;
    }

    public void setUiEnabled(boolean enable) {
        cfgEditor.setCanSave(enable && (cfg != null && !cfg.getBooleanVal("锁定", false)));
//        buttonCfgFile.setEnabled(true);
//        buttonLook.setEnabled(enable && (cfg != null && !cfg.getBooleanVal("锁定", false)));
        cfgEditor.setCanAdd(enable);
        cfgEditor.setCanDele(enable);
        cfgEditor.setCanRetrieve(enable);
    }

    public boolean isDirty() {
        return ds.isDirty();
    }

    public void setClearBeforeSave(boolean clearBeforeSave) {
        this.clearBeforeSave = clearBeforeSave;
    }

    Number getNumber(String v) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            try {
                return Float.parseFloat(v);
            } catch (Exception e1) {

            }
        }
        return null;
    }

    Object getInt(String v) {
        return null;
    }

    private class triggerRetrive implements DwToobar_Event {
        public boolean toolbarClicked(int key, DwToolBar tbar, Object parms) {
            if (DwToolBar.B_UPDATE == key) {
                onWrite();
            } else if (DwToolBar.B_RETRIEVE == key) {
                onRetrieve();
            } else if (DwToolBar.B_DELETE == key) {
                return onDelete();
            } else {
                return true;
            }

            return false;
        }
    }

    public class RowsCellRenderer extends DefaultTableCellRenderer {
        DecimalFormat floatFormat = new DecimalFormat("#,##0.00");
        DecimalFormat floatFormat2 = new DecimalFormat("#,##0.0000");
        DecimalFormat intFormat = new DecimalFormat("#,###");
        DecimalFormat defaultFormat = null;
        Color LIGHT_GRAY = new Color(230, 230, 230);

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, column);
            Number num = 0;
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                String itemName = (String) dw.getValueAt(row, 0);
//                if (value != null) {
//                    String note = value.toString();
//                    Object v = getNumber(note);
//
//                    if (v != null && v instanceof Number) {
//                        DecimalFormat ft = floatFormat;
////                        if (ft == null) ft = floatFormat;
//                        if (v instanceof Long || v instanceof Integer || v instanceof Short) {
//                            ft = intFormat;
//                        }
//                        if("印花税".equals(itemName) || "交易所费用".equals(itemName) || "固定止盈".equals(itemName) || "固定止损".equals(itemName)){
//                            ft = floatFormat2;
//                        }
//                        num = (Number) v;
//                        String text = ft.format(num);
//                        label.setText(text);
//                        label.setHorizontalAlignment(JLabel.LEFT);
//                    } else {
//                        label.setText(note);
//                        label.setHorizontalAlignment(JLabel.LEFT);
//                    }
//
//                    if (enabledItems != null) {
//                        if (!enabledItems.contains(itemName)) {
//                            label.setForeground(Color.LIGHT_GRAY);
//                        } else {
//                            label.setForeground(Color.BLACK);
//                        }
//                    } else {
//                        label.setForeground(Color.BLACK);
//                    }
//                }
                if (enabledItems != null) {
                    if (!enabledItems.contains(itemName)) {
                        label.setForeground(Color.LIGHT_GRAY);
                    } else {
                        label.setForeground(Color.BLACK);
                    }
                } else {
                    label.setForeground(Color.BLACK);
                }
            }

            return c;
        }
    }

    int find(String cfgID) {
        int i = 0;
        int found = -1;
        for (; i < dbCfgComboBox.getItemCount(); i++) {
            if (dbCfgComboBox.getItemAt(i).equals(cfgID)) {
                found = i;
                break;
            }
        }
        return found;
    }


}
