package szhzz.sql.database;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-12-9
 * Time: 下午2:10
 * To change this template use File | Settings | File Templates.
 */
public class NoSorter extends TableSorter {
    public NoSorter() {

    }

    @Override
    public int modelRowIndex(int viewIndex) {
        return viewIndex;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int tableRow(int dataIndex) {
        return dataIndex;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getRowCount() {
        return (tableModel == null) ? 0 : tableModel.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return (tableModel == null) ? 0 : tableModel.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            return tableModel.getValueAt(modelRowIndex(rowIndex), columnIndex);
        } catch (Exception ignored) {

        }
        return null;
    }

    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
        }
    }
}
