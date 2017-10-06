package szhzz.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-4-9
 * Time: 下午8:42
 * To change this template use File | Settings | File Templates.
 */
public class ComboBoxUndoManager {
    JComboBox comboBox;
    JButton leftButton;
    JButton rightButton;
    Vector<Integer> history;
    int index = 0;
    int undoLength = 100;
    boolean internalEvent = false;

    public ComboBoxUndoManager(JComboBox comboBox, JButton leftButton, JButton rightButton) {
        this.comboBox = comboBox;
        this.leftButton = leftButton;
        this.rightButton = rightButton;
        history = new Vector<Integer>();


        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ("comboBoxEdited".equals(e.getActionCommand())) {
                    onAction();
                }
            }
        });


        leftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onLeft();
            }
        });

        rightButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRight();
            }
        });

    }

    void onLeft() {
        if (index > 0) {
            index--;
            int itemIndex = history.get(index);

            internalEvent = true;
            comboBox.setSelectedIndex(itemIndex);
            comboBox.actionPerformed(null);
//            comboBox.setActionCommand("comboBoxEdited");
//            comboBox.fireActionEvent();

//            comboBox.firePropertyChange();
            internalEvent = false;
        }
        setButtomState();
    }

    void onRight() {
        if (index < (history.size() - 1)) {
            index++;
            int itemIndex = history.get(index);

            internalEvent = true;
            comboBox.setSelectedIndex(itemIndex);
            comboBox.actionPerformed(null);
            internalEvent = false;
        }
        setButtomState();
    }

    void onAction() {
        if (!internalEvent) {

            if (history.size() == 0) {
                history.add(comboBox.getSelectedIndex());
                index = 0;
            } else {
                if (comboBox.getSelectedIndex() != history.get(index)) {
                    index++;
                    if (index >= (history.size())) {
                        history.add(comboBox.getSelectedIndex());
                    } else {
                        history.insertElementAt(comboBox.getSelectedIndex(), index);
                    }

                }
            }

            if (history.size() >= undoLength) {
                history.remove(0);
            }
            if (index >= (history.size())) {
                index = (history.size() - 1);
            }
            setButtomState();
        }

    }

    void setButtomState() {
        rightButton.setEnabled(index < (history.size() - 1));
        leftButton.setEnabled(index > 0);
    }
}
