package szhzz.Config;


import szhzz.App.AppManager;
import szhzz.Utils.EventInspector;
import szhzz.sql.gui.DwPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CfgEditor extends JDialog {
    ConfigUI configUI = null;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private DwPanel CfgDw;
    private EventInspector<Config> onOKInspector = null;
    private EventInspector<Config> onCancelInspector = null;

    public CfgEditor(JFrame frame) {
        super(frame);
        init();
    }

    public CfgEditor(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        init();
    }

    public static void main(String[] args) {
        CfgEditor dialog = new CfgEditor(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void init() {

        setContentPane(contentPane);
        setModal(false);
        getRootPane().setDefaultButton(buttonOK);

        configUI = new ConfigUI();
        configUI.setCfgEditor(CfgDw);
        configUI.setClearBeforeSave(true);


        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
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


    }

    private void onOK() {
        configUI.onWrite();
        savePref();
        dispose();
        if (onOKInspector != null) {
            onOKInspector.callBack(getCfg());
        }
    }

    private void onCancel() {
// add your code here if necessary
        getCfg().reLoad();
        savePref();
        if (onCancelInspector != null) {
            onCancelInspector.callBack(getCfg());
        }

        dispose();
    }

    public Config getCfg() {
        return configUI.getCfg();
    }

    public void setCfg(Config cfg) {
        configUI.setCfgFile(cfg);
        this.setTitle(cfg.getConfigUrl());
    }

    public void loadPref() {
        AppManager App = AppManager.getApp();
        Config appCfg = App.getPreferCfg(this.getClass());


        Point p = this.getLocation();
        this.setBounds(
                appCfg.getIntVal("SystemCfgDefinX", (int) p.getX()),
                appCfg.getIntVal("SystemCfgDefinY", (int) p.getY()),
                appCfg.getIntVal("SystemCfgDefinW", this.getWidth()),
                appCfg.getIntVal("SystemCfgDefinH", this.getHeight())
        );
    }

    void savePref() {
        AppManager App = AppManager.getApp();

        Config appCfg = App.getPreferCfg(this.getClass());
        Rectangle p = this.getBounds();

        appCfg.setProperty("SystemCfgDefinProp", "yes");
        int x = (int) p.getX();
        if (x > 1920) x = 1;
        appCfg.setProperty("SystemCfgDefinX", "" + x);
        appCfg.setProperty("SystemCfgDefinY", "" + (int) p.getY());
        appCfg.setProperty("SystemCfgDefinW", "" + (int) p.getWidth());
        appCfg.setProperty("SystemCfgDefinH", "" + (int) p.getHeight());

        appCfg.save();
    }

    public void setOnOKInspector(EventInspector<Config> onOKInspector) {
        this.onOKInspector = onOKInspector;
    }

    public void setOnCancelInspector(EventInspector<Config> onCancelInspector) {
        this.onCancelInspector = onCancelInspector;
    }
}
