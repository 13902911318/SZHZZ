package szhzz.Utils;



import szhzz.App.AppManager;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

public class Shutdown extends JDialog {
    AppManager App = AppManager.getApp();
    Timer countDown;
    int step = 60;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel TimeLeft;
    private JLabel promptLable;
    private String command = "-R";
    private static boolean canceled = false;

    public Shutdown() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);


        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AppManager.logit("buttonCancel active, Shutdown canceled");
                canceled = true;
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

        if(AppManager.getApp().getMainFram() != null){
            this.setTitle(AppManager.getApp().getMainFram().getTitle() + " 退出程序");
        }
        startup();
    }

    public static void main(String[] args) {
        Shutdown dialog = new Shutdown();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void startup() {
        countDown = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (--step < 0) {
                    onOK();
                    return;
                } else {
                    TimeLeft.setText("" + step);
                    countDown.start();
                }
            }
        });
        countDown.start();
    }

    private void onOK() {
        countDown.stop();

        if (canceled) {
            AppManager.logit("Shutdown canceled");
            return;
        }

        try {
            if (!command.equalsIgnoreCase("-Q")) {
                if (!AppManager.getApp().canRemoteShutdown()) {
                    command = "-R";
                }
                AppManager.logit("Shutdown invoked");
                Runtime.getRuntime().exec("shutdown " + command + " -t 60");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dispose();

//        if (App.Quit()) {
        System.exit(0);
//        }
    }

    private void onCancel() {
// add your code here if necessary
        countDown.stop();
        dispose();
    }

    public void setReboot() {
        setCommand("-R");
    }


//    public void setShutdown() {
//        setCommand("-S");
//    }
//
//    public void setShutdown(int step) {
//        setCommand("-S");
//        this.step = step;
//    }

    public void setQuit() {
        setCommand("-Q");
    }

    public void setQuit(int step) {
        setCommand("-Q");
        this.step = step;
    }

    public void setNotCancelable(){
        buttonCancel.setEnabled(false);
    }
    public void setLogout() {
        setCommand("-L");
    }

    public void setCommand(String command) {
        this.command = command;

        if (command.equalsIgnoreCase("-R")) {
            promptLable.setText("计算机将在" + step + "秒内重新启动。");
        } else if (command.equalsIgnoreCase("-S")) {
            promptLable.setText("计算机将在" + step + "秒内关闭。");
        } else if (command.equalsIgnoreCase("-L")) {
            promptLable.setText("计算机将在" + step + "秒内注销。");
        } else if (command.equalsIgnoreCase("-Q")) {
            promptLable.setText("程序将在" + step + "秒内退出。");
        }
    }
}
