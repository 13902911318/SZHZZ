package szhzz.Utils;

import javax.swing.*;
import java.awt.event.*;

public class TimeOutMessageBox extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel promptLable;
    private JLabel TimeLeft;
    Timer countDown;
    int step = 60;
    private static int X = 0;
    private static int Y = 0;

    public TimeOutMessageBox() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        X += 50;
        Y += 50;
        this.setLocation(X, Y);

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
        // add your code here
        if(countDown!=null){
            countDown.stop();
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        if(countDown!=null){
            countDown.stop();
        }
        dispose();
    }

    public static void main(String[] args) {
        TimeOutMessageBox dialog = new TimeOutMessageBox();
        dialog.showMessage("Auto close messagebox");
        dialog.pack();
        dialog.setVisible(true);

        System.exit(0);
    }

    public void showMessage(String msg) {
        showMessage(msg, 0) ;
    }

    public void showMessage(String msg, int seconds) {
        if(seconds > 0) {
            step = seconds;
        }
        TimeLeft.setText("" + step);
        promptLable.setText(msg);

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
}
