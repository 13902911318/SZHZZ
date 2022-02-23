package szhzz.Utils.QuickPinYin;


import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import szhzz.Utils.DawLogger;
import szhzz.sql.gui.MutiColCombBox;
import szhzz.sql.gui.PYConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;


/**
 * <p>Title: INFO2820</p>
 * <p/>
 * <p>Description: home work INFO2820</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author John
 * @version 1.0
 */
public class QuickPinYinComboBox extends MutiColCombBox {
    private static DawLogger logger = DawLogger.getLogger(QuickPinYinComboBox.class);
    private Comparator UdfComparator = null;
    private ObjectToStringConverter stringConverter = null;
    private boolean inited = false;

    public QuickPinYinComboBox() {
        super();
        this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxEdited")) {
                    Object o = getSelectedItem();
                    getEditor().setItem(getSelectedItem());
                }
            }
        });
        this.setPreferredSize(new Dimension(200, 25));
    }


    public void setValues(Enumeration names) {
        Vector bands = new Vector();
        try {
            if (!inited) {
                if (stringConverter == null) {
                    AutoCompleteDecorator.decorate(this, new PYConverter());
                } else {
                    AutoCompleteDecorator.decorate(this, stringConverter);
                }
                inited = true;
            }

            while (names.hasMoreElements()) {
                Object s = names.nextElement();
                Vector<String> e = PYConverter.getPyTable(s.toString());
                bands.add(e);
            }
            if (UdfComparator != null) {
                Collections.sort(bands, UdfComparator);
            } else {
                Collections.sort(bands, new PyComparator());
            }
            this.setModel(new DefaultComboBoxModel(bands));
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public Object getCurrentValue(int col) {
        Object o = this.getModel().getSelectedItem();
        if (o != null && o instanceof Vector) {
            return ((Vector) o).get(col);
        }
        return null;
    }

    public void setComparator(Comparator udfComparator) {
        UdfComparator = udfComparator;
    }

    public void setStringConverter(ObjectToStringConverter stringConverter) {
        this.stringConverter = stringConverter;
    }


    static class PyComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 == null) return -1;
            if (o2 == null) return 1;

            Vector<String> v1 = (Vector) o1;
            Vector<String> v2 = (Vector) o2;
            if (v1.size() < 2 || v1.get(1) == null) return 1;
            if (v2.size() < 2 || v2.get(1) == null) return -1;
            return v1.get(0).compareTo(v2.get(0));
        }
    }
}