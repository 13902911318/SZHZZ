package szhzz.sql.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-6
 * Time: 17:34:46
 * To change this template use File | Settings | File Templates.
 */
public class MutiColCombBox extends JComboBox {
    private int dataColmn = 0;


    public MutiColCombBox() {
        super();
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MutiColCombBox(MutiColComboBoxEditoer editor) {
        this();
        this.setEditor(editor);
    }

    private void jbInit() throws Exception {
        addItemListener(new itemEditAdapter());
        this.setRenderer(new StringRenderer());
        //this.setEditor(new MutiColComboBoxEditoer());
        //setEditable(false);
    }

    public Object getSelectedValue() {
        return getSelectedValue(dataColmn);
    }

    /**
     * TODO
     * 注意！当 ComboBox 处于选择状态的时候， getSelectedItem 从数据集里返回数据
     * 反之，getSelectedItem 将从editor那里返回数据。
     * 多字段的 ComboBox 要注意 getSelectedItem 返回的类型。
     *
     * @param col
     * @return Object
     */
    public Object getSelectedValue(int col) {
        Object o = this.getSelectedItem();
        if (o instanceof Vector) {
            Vector row = (Vector) o;
            if (col < row.size()) {
                o = row.get(col);
            } else {
                return null;
            }
        } else {
            o = this.getSelectedItem();
        }
        return o;
    }

    public Object getCurrentValue(int row, int col) {
        return null;
    }

    public int getDataColmn() {
        return dataColmn;
    }

    public void setDataColmn(int dataColmn) {
        this.dataColmn = dataColmn;

        Object editor;
        editor = this.getEditor();
        if (editor instanceof MutiColComboBoxEditoer)
            ((MutiColComboBoxEditoer) editor).setDataCol(dataColmn);
    }

    public void triggerEvent() {
        fireActionEvent();
    }

    public void TrggerItemChanged(Object oldData, Object newData) {
    }

    /**
     * TODO
     * 如果直接设 Combobox.setEditable(false), Combobox 将不使用
     * 用户定义的 editor, 这将无法实现在 Combobox 的编辑字段里显
     * 示单个字段值的功能。因此总是设 MutiColComboBox.setEditable(true)
     * 需要阻止编辑功能时，转用((MutiColComboBoxEditoer)editor).setEditable(aFlag)。
     *
     * @param aFlag
     */
    public void setEditable(boolean aFlag) {
        Object editor;
        editor = this.getEditor();
        if (editor instanceof MutiColComboBoxEditoer) {
            ((MutiColComboBoxEditoer) editor).setEditable(aFlag);
            if (!isEditable) {
                isEditable = true;
                firePropertyChange("editable", false, isEditable);
            }
        } else {
            boolean oldFlag = isEditable;
            isEditable = aFlag;
            firePropertyChange("editable", oldFlag, isEditable);
        }
    }

    public int find(String val) {
        int i = -1;
        MutableComboBoxModel m = (MutableComboBoxModel) getModel();
        Object o;
        while ((o = m.getElementAt(++i)) != null) {
            if (o instanceof Vector) {
                Vector v = (Vector) o;
                if (v.contains(val)) return i;
            }
        }

        return -1;
    }

    public Object getCurrentValue(int col) {
        Object o = this.getModel().getSelectedItem();
        if (o != null && o instanceof Vector) {
            return ((Vector) o).get(col);
        }
        return null;
    }

    class HTMLRenderer extends DefaultListCellRenderer {
        private static final String START = "<html><table><tr>";
        private static final String END = "</tr></table></html>";

        public Component getListCellRendererComponent(
                JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            StringBuffer sb = new StringBuffer();

            super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            if (value == null)
                setText("");
            else {
                Vector item = (Vector) value;
                sb.append(START);
                for (int i = 0; i < item.size(); i++) {
                    sb.append("<td>");
                    sb.append((item.get(i) == null ? "" : item.get(i).toString()));
                    sb.append("</td>");
                }
                sb.append(END);
                setText(sb.toString());
            }
            return this;
        }
    }

    class StringRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(
                JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            StringBuffer sb = new StringBuffer();

            super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            if (value == null)
                setText("");
            else {
                Vector item = (Vector) value;
                for (int i = 0; i < item.size(); i++) {
                    sb.append(" ");
                    sb.append((item.get(i) == null ? "" : item.get(i).toString()));
                }
                setText(sb.toString());
            }
            return this;
        }
    }

    class itemEditAdapter implements ItemListener {
        Object oldValue;
        Object newValue;

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                oldValue = e.getItem();
            }
            if (e.getStateChange() == ItemEvent.SELECTED) {
                newValue = e.getItem();
                TrggerItemChanged(oldValue, newValue);
            }
        }
    }
}
