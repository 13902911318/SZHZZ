package szhzz.StatusInspect;

import szhzz.sql.gui.DataWindow;
import szhzz.sql.gui.DwPanel;
import szhzz.App.AppManager;
import szhzz.Config.Config;
import szhzz.Utils.CaptureScreen;
import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class StatusView extends JDialog {
    private static DawLogger logger = DawLogger.getLogger(StatusView.class);
    private JPanel contentPane;
    private DwPanel StatusDw;
    private DataWindow dw = null;
    private String colWidth = null;

    public StatusView(Frame frame) {
        super(frame);
        setContentPane(contentPane);
        setModalityType(ModalityType.MODELESS);
        setAlwaysOnTop(false);
//        getRootPane().setDefaultButton(buttonOK);
        this.setTitle("系统状态监测");

        initDw();

//        buttonOK.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onOK();
//            }
//        });
//
//        buttonCancel.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onCancel();
//            }
//        });

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

    public static void main(String[] args) {
        StatusView dialog = new StatusView(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void initDw() {
        dw = StatusDw.getDataWindow();
        StatusInspector.getInstance().setUI(StatusDw);
    }

    private void onOK() {
        savePref();
        dispose();
        setAlwaysOnTop(false);
    }

    private void onCancel() {
        savePref();
        dispose();
        setAlwaysOnTop(false);
    }

    public void loadPref() {
        AppManager App = AppManager.getApp();
        Config appCfg = App.getPreferCfg(this.getClass());

        Point p = this.getLocation();
        this.setBounds(
                appCfg.getIntVal("StatusViewDefinX", (int) p.getX()),
                appCfg.getIntVal("StatusViewDefinY", (int) p.getY()),
                appCfg.getIntVal("StatusViewDefinW", this.getWidth()),
                appCfg.getIntVal("StatusViewDefinH", this.getHeight())
        );

        String[] colWidth = appCfg.getProperty("StatusViewColWidth", "").split("\t");
        if (dw.getColumnCount() == 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {

            }
        }
        if (dw.getColumnCount() > 0) {
            for (int c = 0; c < colWidth.length; c++) {
                dw.setColumnWidth(c, NU.parseLong(colWidth[c], (long) dw.getColumnWidth(c)).intValue());
            }
        }
    }

    void savePref() {
        AppManager App = AppManager.getApp();
        Config appCfg = App.getPreferCfg(this.getClass());
        Rectangle p = this.getBounds();

        appCfg.setProperty("StatusViewDefinProp", "yes");
        int x = (int) p.getX();
        if (x > 1920) x = 1;
        appCfg.setProperty("StatusViewDefinX", "" + x);
        appCfg.setProperty("StatusViewDefinY", "" + (int) p.getY());
        appCfg.setProperty("StatusViewDefinW", "" + (int) p.getWidth());
        appCfg.setProperty("StatusViewDefinH", "" + (int) p.getHeight());

        StringBuffer sb = new StringBuffer();
        for (int c = 0; c < dw.getColumnCount(); c++) {
            if (c > 0) sb.append("\t");
            sb.append(dw.getColumnWidth(c));
        }
        appCfg.setProperty("StatusViewColWidth", "" + sb.toString());

        appCfg.save();
    }

    public void setTitle(String title) {
        super.setTitle(title + ": 系统状态监测");
    }

    public void sentMail() {
        this.setVisible(true);
        Rectangle p = this.getBounds();
        String file = "StatusView.png";

        try {
            File f = CaptureScreen.captureScreen(p, file);
            AppManager.getApp().sendMail("系统状态监测", f.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
