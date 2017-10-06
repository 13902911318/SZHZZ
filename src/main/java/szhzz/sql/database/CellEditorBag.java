package szhzz.sql.database;


import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-9
 * Time: 8:35:27
 * To change this template use File | Settings | File Templates.
 */
public class CellEditorBag {
    public CellEditorBag() {
    }


    public static DefaultCellEditor getNmumberEditor(Object data, Comparable min, Comparable max) {
        NumberFormat nFormater = null;

        if (data instanceof Number) {
            if (data instanceof Integer ||
                    data instanceof BigInteger ||
                    data instanceof Long ||
                    data instanceof Short) {
                nFormater = NumberFormat.getIntegerInstance();  // Locale.CHINA
                nFormater.setGroupingUsed(false);               // not format as 1,000
            } else {
                nFormater = NumberFormat.getNumberInstance();  // Locale.CHINA
                nFormater.setGroupingUsed(true);               // format as 1,000.00
            }
        }
        if (nFormater == null) return new DefaultCellEditor(new JTextField());
        return new NumberEditor(nFormater, min, max);
    }
}


class NumberEditor extends DefaultCellEditor { //
    JFormattedTextField ftf;
    NumberFormat nFormater;
    private Comparable minimum, maximum;
    private boolean DEBUG = false;

    public NumberEditor(NumberFormat nFormater, Comparable min, Comparable max) {
        super(new JFormattedTextField());
        this.nFormater = nFormater;
        ftf = (JFormattedTextField) getComponent();
        minimum = min;
        maximum = max;

        //Set up the editor for the integer cells.
        NumberFormatter NoFormatter = new NumberFormatter(nFormater);
        NoFormatter.setFormat(nFormater);
        NoFormatter.setMinimum(minimum);
        NoFormatter.setMaximum(maximum);

        ftf.setFormatterFactory(
                new DefaultFormatterFactory(NoFormatter));
        ftf.setValue(minimum);
        ftf.setHorizontalAlignment(JTextField.TRAILING);
        ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

        //React when the user presses Enter while the editor is
        //active.  (Tab is handled as specified by
        //JFormattedTextField's focusLostBehavior property.)
        ftf.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, 0),
                "check");
        ftf.getActionMap().put("check", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!ftf.isEditValid()) { //The text is invalid.
                    if (userSaysRevert()) { //reverted
                        ftf.postActionEvent(); //inform the editor
                    }
                } else try {              //The text is valid,
                    ftf.commitEdit();     //so use it.
                    ftf.postActionEvent(); //stop editing
                } catch (java.text.ParseException exc) {
                }
            }
        });
    }

    //Override to invoke addValue on the formatted text field.
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value, boolean isSelected,
                                                 int row, int column) {
        JFormattedTextField ftf =
                (JFormattedTextField) super.getTableCellEditorComponent(
                        table, value, isSelected, row, column);
        ftf.setValue(value);
        return ftf;
    }


    //Override to ensure that the value remains an Number.
    public Object getCellEditorValue() {
        JFormattedTextField ftf = (JFormattedTextField) getComponent();
        Object o = ftf.getValue();
        if (o instanceof Number) {
            return o;
        } else {
            if (DEBUG) {
                System.out.println("getCellEditorValue: o isn't a Number");
            }
            try {
                if (o != null) {
                    return nFormater.parseObject(o.toString());
                }
            } catch (ParseException exc) {
                //System.err.println("getCellEditorValue: can't parse o: " + o);
                return null;
            }
            return null;
        }
    }

    //Override to check whether the edit is valid,
    //setting the value if it is and complaining if
    //it isn't.  If it's OK for the editor to go
    //away, we need to invoke the superclass's version
    //of this method so that everything gets cleaned up.
    public boolean stopCellEditing() {
        JFormattedTextField ftf = (JFormattedTextField) getComponent();
        if (ftf.isEditValid()) {
            try {
                ftf.commitEdit();
            } catch (java.text.ParseException exc) {
            }

        } else { //text is invalid
            ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
            if (!userSaysRevert()) { //user wants to edit
                return false; //don't let the editor go away
            }
        }
        return super.stopCellEditing();
    }

    /**
     * Lets the user know that the text they entered is
     * bad. Returns true if the user elects to revert to
     * the last good value.  Otherwise, returns false,
     * indicating that the user wants to continue editing.
     */
    protected boolean userSaysRevert() {
        Toolkit.getDefaultToolkit().beep();
        ftf.selectAll();
        Object[] options = {"Edit",
                "Revert"};
        int answer = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(ftf),
                "The value must be an Number between "
                        + minimum + " and "
                        + maximum + ".\n"
                        + "You can either continue editing "
                        + "or revert to the last valid value.",
                "Invalid Text Entered",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[1]);

        if (answer == 1) { //Revert!
            ftf.setValue(ftf.getValue());
            return true;
        }
        return false;
    }


}


