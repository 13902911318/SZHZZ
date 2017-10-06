package szhzz.sql.gui;


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Event SelectionListener
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-3
 * Time: 17:23:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class DWRowChanged implements ListSelectionListener {
    public String debugId = "DWRowChanged";
    DataWindow dw;

    public final void setDataWindow(DataWindow dw) {
        this.dw = dw;
    }

    public final void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (lsm.isSelectionEmpty()) {
            //rowChanged(-1, dw.getRowCount());
        } else {
            rowChanged(dw.getSelectedRow(), dw.getRowCount());
        }
    }

    public void rowChanged(int currentRow, int rowCount) {
    }

    ;

}
