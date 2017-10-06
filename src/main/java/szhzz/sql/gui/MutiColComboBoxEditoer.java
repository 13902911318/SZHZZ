package szhzz.sql.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-6
 * Time: 22:53:27
 * To change this template use File | Settings | File Templates.
 */
public class MutiColComboBoxEditoer extends BasicComboBoxEditor {
    Object value;
    int dataCol = 0;
    private Object oldValue;

    public MutiColComboBoxEditoer() {
        super();
    }

    public void setDataCol(int dataCol) {
        this.dataCol = dataCol;
    }

    /**
     * TODO
     * 如果直接设 Combobox.setEditable(false), Combobox 将不使用
     * 用户定义的 editor, 这将无法实现在 Combobox 的编辑字段里显
     * 示单个字段值的功能。因此总是设 MutiColComboBox.setEditable(true)
     * 需要阻止编辑功能时，转用该方法。
     *
     * @param aFlag
     */
    public void setEditable(boolean aFlag) {
        ((JTextField) getEditorComponent()).setEditable(aFlag);
    }

    /**
     * TODO
     * 注意！当 ComboBox 处于选择状态的时候， getSelectedItem 从数据集里返回数据
     * 反之，getSelectedItem 将从该函数返回数据。多字段的 ComboBox 要注意 getSelectedItem
     * 返回的类型。
     *
     * @return Object
     */
    public Object getItem() {
        Object newValue = editor.getText();

        if (oldValue != null && !(oldValue instanceof String)) {
            // The original value is not a string. Should return the value in it's
            // original type.
            if (newValue.equals(oldValue.toString())) {
                return oldValue;
            } else {
                // Must take the value from the editor and get the value and cast it to the new type.
                Class cls = oldValue.getClass();
                try {
                    Method method = cls.getMethod("valueOf", new Class[]{String.class});
                    newValue = method.invoke(oldValue, editor.getText());
                } catch (Exception ex) {
                    // Fail silently and return the newValue (a String object)
                }
            }
        }
        return newValue;
    }

    /**
     * Sets the item that should be edited.
     *
     * @param anObject the displayed value of the editor
     */
    public void setItem(Object anObject) {
        if (anObject instanceof Vector) {
            if (anObject != null) {
                oldValue = ((Vector) anObject).get(dataCol);
                editor.setText(oldValue.toString());
            } else
                editor.setText("");
            return;
        }
        if (anObject != null) {
            editor.setText(anObject.toString());

            oldValue = anObject;
        } else {
            editor.setText("");
        }
    }

}
