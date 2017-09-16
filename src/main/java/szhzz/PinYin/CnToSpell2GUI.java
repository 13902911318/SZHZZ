package szhzz.PinYin;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2009-6-22
 * Time: 23:38:20
 * To change this template use File | Settings | File Templates.
 *
 * @(#)CnToSpellGUI.java kindani
 * 2004-10-25??
 * @(#)CnToSpellGUI.java kindani
 * 2004-10-25??
 * @(#)CnToSpellGUI.java kindani
 * 2004-10-25??
 * @(#)CnToSpellGUI.java kindani
 * 2004-10-25??
 * @(#)CnToSpellGUI.java kindani
 * 2004-10-25??
 */
/**
 * @(#)CnToSpellGUI.java
 * kindani
 * 2004-10-25??
 * */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * JDK版本
 * <p/>
 * 1.4
 *
 * @author KIN
 * @version 1.0
 * @see
 * @since 1.0
 */
public class CnToSpell2GUI extends JFrame {

    private CnToSpell2GUI c = null;

    public CnToSpell2GUI() {
        super("Cn to Spell");
        setSize(800, 100);
        getContentPane().setLayout(new FlowLayout());
// component layout
        JTextArea from = new JTextArea(5, 20);
        JTextArea to = new JTextArea(5, 20);
        JButton b = new JButton("cn to pinyin");
        getContentPane().add(new JLabel("From:"));
        getContentPane().add(from);
        getContentPane().add(b);
        getContentPane().add(new JLabel("To:"));
        getContentPane().add(to);
// action handle
        b.addActionListener(new Cn2PinyinActionListener(from, to));
        setVisible(true);
// set this for pack
        c = this;
    }

    public static void main(String[] args) {
        CnToSpell2GUI g = new CnToSpell2GUI();
    }

    /**
     * button action listener to convert text to pinyin from one textbox to another textbox
     */
    class Cn2PinyinActionListener implements ActionListener {

        private JTextArea from = null;
        private JTextArea to = null;

        public Cn2PinyinActionListener(JTextArea from, JTextArea to) {
            this.from = from;
            this.to = to;
        }

        public void actionPerformed(ActionEvent e) {
            if (from.getText().length() == 0) {
                JOptionPane.showMessageDialog(from, "From text is empty!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            String text = from.getText();
            to.setText(CnToSpell.getFullSpell(text));
            c.pack();
        }
    }
}


