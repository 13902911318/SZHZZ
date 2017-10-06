package szhzz.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: SZHZZ
 * Date: 13-6-10
 * Time: 上午9:48
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShowInputDialog {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Input Dialog Box Frame");
        JButton button = new JButton("Show Input Dialog BOX");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String str = JOptionPane.showInputDialog(null, "Enter some text : ",
                        "Roseindia.net", 1);
//                String str = JOptionPane.showInputDialog(null, "Enter some text : ");
                if (str != null)
                    JOptionPane.showMessageDialog(null, "You entered the text : " + str,
                            "Roseindia.net", 1);
                else
                    JOptionPane.showMessageDialog(null, "You pressed cancel button.",
                            "Roseindia.net", 1);
            }
        });
        JPanel panel = new JPanel();
        panel.add(button);
        frame.add(panel);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}