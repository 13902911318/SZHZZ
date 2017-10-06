package szhzz.sql.gui;


import szhzz.sql.database.DBException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Vector;


/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-3
 * Time: 22:38:56
 * To change this template use File | Settings | File Templates.
 */
public class DwToolBar extends JToolBar {
    public static final int B_RETRIEVE = 1;
    public static final int B_UPDATE = 2;
    public static final int B_FIRST = 3;
    public static final int B_PREVIOUS = 4;
    public static final int B_NEXT = 5;
    public static final int B_TAIL = 6;
    public static final int B_DELETE = 7;
    public static final int B_ADD = 8;


    private static ImageIcon imageFirst = null;
    private static ImageIcon imagePrev = null;
    private static ImageIcon imageNext = null;
    private static ImageIcon imageLast = null;
    private static ImageIcon imageDelete = null;
    private static ImageIcon imageAdd = null;
    private static ImageIcon imageRetrieve = null;
    private static ImageIcon imageUpdate = null;

    DataWindow dw;
    Vector Events = new Vector();


    JButton jButton_Last = new JButton();
    JButton jButton_Next = new JButton();
    JButton jButton_Tail = new JButton();
    JTextField jTextField_gotoRow = new JTextField();
    JButton jButton_Add = new JButton();
    JButton jButton_Delet = new JButton();
    JButton jButtonRtrieve = new JButton();
    JButton jButton_Update = new JButton();
    JButton jButton_First = new JButton();

    public DwToolBar() {
        super();
        try {
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = DwToolBar.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            return null;
        }
    }

    public void setEnableAll(boolean onOf) {
        jButton_Last.setEnabled(onOf);
        jButton_Next.setEnabled(onOf);
        jButton_Tail.setEnabled(onOf);
        jTextField_gotoRow.setEnabled(onOf);
        jButton_Add.setEnabled(onOf);
        jButton_Delet.setEnabled(onOf);
        jButtonRtrieve.setEnabled(onOf);
        jButton_Update.setEnabled(onOf);
        jButton_First.setEnabled(onOf);
    }

    public void setVisibleAll(boolean onOf) {
        jButton_Last.setVisible(onOf);
        jButton_Next.setVisible(onOf);
        jButton_Tail.setVisible(onOf);
        jTextField_gotoRow.setVisible(onOf);
        jButton_Add.setVisible(onOf);
        jButton_Delet.setVisible(onOf);
        jButtonRtrieve.setVisible(onOf);
        jButton_Update.setVisible(onOf);
        jButton_First.setVisible(onOf);
    }

    public void jbInit() throws Exception {
        if (imageFirst == null) {
            imageFirst = createImageIcon("/resources/First.gif");
            imagePrev = createImageIcon("/resources/Prev.gif");
            imageNext = createImageIcon("/resources/Next.gif");
            imageLast = createImageIcon("/resources/Last.gif");
            imageDelete = createImageIcon("/resources/Delete.gif");
            imageAdd = createImageIcon("/resources/Add.gif");
            imageRetrieve = createImageIcon("/resources/Retrieve.gif");
            imageUpdate = createImageIcon("/resources/UpdateDB.gif");

        }
        //this.addSeparator();
        add(jButtonRtrieve);
        add(jButton_First);
        add(jButton_Last);
        add(jTextField_gotoRow);
        add(jButton_Next);
        add(jButton_Tail);
        add(jButton_Add);
        add(jButton_Delet);
        add(jButton_Update);

        jButton_Last.setToolTipText("Scoll Up");
        jButton_Last.setIcon(imagePrev);

        jButton_Next.setToolTipText("Scoll Down");
        jButton_Next.setIcon(imageNext);

        jButton_Tail.setToolTipText("Scoll to Last");
        jButton_Tail.setIcon(imageLast);


        jTextField_gotoRow.setMaximumSize(new Dimension(120, 30));
        jTextField_gotoRow.setMinimumSize(new Dimension(80, 30));
        jTextField_gotoRow.setPreferredSize(new Dimension(120, 30));
        jTextField_gotoRow.setToolTipText("Quick goto currentRow number");
        jTextField_gotoRow.setSelectionEnd(100);
        jTextField_gotoRow.setText("0");
        jTextField_gotoRow.setHorizontalAlignment(SwingConstants.CENTER);


        jButton_Add.setToolTipText("Append New Row");
        jButton_Add.setIcon(imageAdd);

        jButton_Delet.setToolTipText("Delete Selected Row");
        jButton_Delet.setIcon(imageDelete);

        jButtonRtrieve.setToolTipText("Retrieve");
        jButtonRtrieve.setIcon(imageRetrieve);

        jButton_Update.setToolTipText("Save");
        jButton_Update.setIcon(imageUpdate);

        jButton_First.setToolTipText("Scoll to first rowe");
        jButton_First.setIcon(imageFirst);

    }

    public DataWindow getDataWindow() {
        return dw;
    }

    public void setDataWindow(DataWindow dw) {
        this.dw = dw;
        jButton_Last.addActionListener(new dwToolBar_jButton_Last_actionAdapter(this));
        jButton_Next.addActionListener(new dwToolBar_jButton_Next_actionAdapter(this));
        jButton_Tail.addActionListener(new dwToolBar_jButton_Tail_actionAdapter(this));
        jButton_First.addActionListener(new dwToolBar_jButton_First_actionAdapter(this));
        jTextField_gotoRow.addFocusListener(new dwToolBar_jTextField_gotoRow_focusAdapter(this));
        jTextField_gotoRow.addActionListener(new dwToolBar_jTextField_gotoRow_actionAdapter(this));
        jButton_Add.addActionListener(new dwToolBar_jButton_Add_actionAdapter(this));
        jButton_Delet.addActionListener(new dwToolBar_jButton_Delet_actionAdapter(this));
        jButtonRtrieve.addActionListener(new dwToolBar_jButtonRtrieve_actionAdapter(this));
        jButton_Update.addActionListener(new dwToolBar_jButton_Update_actionAdapter(this));
        dw.addsRowChangedListener(new Rowchanged_());
    }

    public void addEvent(DwToobar_Event event) {
        Events.add(event);
    }

    boolean triggerEvent(int key) {
        for (int i = 0; i < Events.size(); i++) {
            DwToobar_Event dte = (DwToobar_Event) Events.get(i);
            if (!dte.toolbarClicked(key, this, null)) return false;
        }
        return true;
    }

    public void setCanNavigate(boolean canSave) {
        jButton_First.setVisible(canSave);
        jButton_Last.setVisible(canSave);
        jButton_Next.setVisible(canSave);
        jButton_Tail.setVisible(canSave);
        jTextField_gotoRow.setVisible(canSave);

    }

    public void setCanSave(boolean canSave) {
        jButton_Update.setVisible(canSave);
    }

    public void setCanRetrieve(boolean can) {
        jButtonRtrieve.setVisible(can);
    }

    public void setCanAdd(boolean canAdd) {
        jButton_Add.setVisible(canAdd);
    }

    public void setCanScroll(boolean canScroll) {
        jButton_Last.setVisible(canScroll);
        jButton_Next.setVisible(canScroll);
        jButton_Tail.setVisible(canScroll);
        jButton_First.setVisible(canScroll);
    }

    public void setCanDele(boolean canDele) {
        jButton_Delet.setVisible(canDele);
    }

    public void triggerRetrive(ActionEvent e) {
        if (!triggerEvent(B_RETRIEVE)) return;
        try {
            dw.retrive();
        } catch (DBException ex) {
            //TODO Unmark
            //loger.getLoger().showError(ex);
        }
    }

    public void trigger_Update(ActionEvent e) {
        //if (dw.isDataWindowReadOnly()) return;
        if (!triggerEvent(B_UPDATE)) return;
        try {
            dw.update();
        } catch (DBException ex) {
            ex.printStackTrace();
        }
    }

    public void trigger_FirstRow(ActionEvent e) {
        if (!triggerEvent(B_FIRST)) return;
        // Cancel Editor if editting
        dw.removeEditor();
        dw.scrollToRow(0);
    }

    public void trigger_DeletRow(ActionEvent e) {
        if (!triggerEvent(B_DELETE)) return;
        int confirm = 0;

        //if (dw.isDataWindowReadOnly()) return;

        // Cancel Editor if editting
        dw.removeEditor();

        int row = dw.getSelectedRow();

        if (row == -1) {
            //TODO Unmark
//            loger.getLoger().messgeBox("Note","No currentRow selected for delete.");
        } else {
            try {
                if (dw.isNewRow(row)) {  // && ! dw.isModified(currentRow)
                    dw.deleteRow(row);
                } else {
                    confirm = JOptionPane.showConfirmDialog(null,
                            "Delete the selected Row " + (row + 1) + " ?");
                    if (confirm == JOptionPane.YES_OPTION) {
                        dw.deleteRow(row);
                    }
                }
                if (row > 0)
                    row -= 1;
                else
                    row = 0;

                dw.Filte();
                dw.scrollToRow(row);
            } catch (DBException ex) {
                //TODO Unmark
//                loger.getLoger().showError(ex);
            }
        }
    }

    public void trigger_AddRow(ActionEvent e) {
        if (!triggerEvent(B_ADD)) return;
        // Cancel Editor if editting
        dw.removeEditor();
        dw.appendRow();
        dw.scrollToRow(dw.getRowCount() - 1);
    }

    public void trigger_LastRow(ActionEvent e) {
        if (!triggerEvent(B_NEXT)) return;
        // Cancel Editor if editting
        dw.removeEditor();
        dw.scrollToRow(dw.getRowCount() - 1);
    }

    public void trigger_PreRow(ActionEvent e) {
        if (!triggerEvent(B_PREVIOUS)) return;
        if (dw.getSelectedRow() > 0) {
            // Cancel Editor if editting
            dw.removeEditor();
            dw.scrollToRow(dw.getSelectedRow() - 1);
        }
    }

    public void trigger_NextRow(ActionEvent e) {
        if (!triggerEvent(B_NEXT)) return;
        if (dw.getSelectedRow() + 1 < (dw.getRowCount())) {
            // Cancel Editor if editting
            dw.removeEditor();
            dw.scrollToRow(dw.getSelectedRow() + 1);
        }
    }

    public void trigger_gotoRow(ActionEvent e) {
        try {
            int r = (new Integer(jTextField_gotoRow.getText())).intValue() - 1;
            if (r >= 0 && r < dw.getRowCount() - 1) {
                // Cancel Editor if editting
                dw.removeEditor();
                dw.scrollToRow(r);
            }
        } catch (Exception ex) {
        }
    }

    //public void addsRowChangedListener(Rowchanged RowChangedListener)

    public void gotoRow_focusGained(FocusEvent e) {
        jTextField_gotoRow.selectAll();
    }

//// Listeners class
//

    class Rowchanged_ extends DWRowChanged {
        public Rowchanged_() {
            debugId = "DwToolBar.Rowchanged";
        }

        public void rowChanged(int currentRow, int rowCount) {
            if (currentRow >= 0) jTextField_gotoRow.setText("Row " + (currentRow + 1) + "/" + rowCount);
        }
    }

    class dwToolBar_jTextField_gotoRow_actionAdapter implements ActionListener {
        private DwToolBar adaptee;

        dwToolBar_jTextField_gotoRow_actionAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.trigger_gotoRow(e);
        }
    }

    class dwToolBar_jTextField_gotoRow_focusAdapter extends FocusAdapter {
        private DwToolBar adaptee;

        dwToolBar_jTextField_gotoRow_focusAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void focusGained(FocusEvent e) {
            adaptee.gotoRow_focusGained(e);
        }
    }

    class dwToolBar_jButton_Next_actionAdapter implements ActionListener {
        private DwToolBar adaptee;

        dwToolBar_jButton_Next_actionAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.trigger_NextRow(e);
        }
    }

    class dwToolBar_jButton_Last_actionAdapter implements ActionListener {
        private DwToolBar adaptee;

        dwToolBar_jButton_Last_actionAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.trigger_PreRow(e);
        }
    }

    class dwToolBar_jButton_Tail_actionAdapter implements ActionListener {
        private DwToolBar adaptee;

        dwToolBar_jButton_Tail_actionAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.trigger_LastRow(e);
        }
    }

    class dwToolBar_jButton_Delet_actionAdapter implements ActionListener {
        private DwToolBar adaptee;

        dwToolBar_jButton_Delet_actionAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.trigger_DeletRow(e);
        }
    }

    class dwToolBar_jButton_Update_actionAdapter implements ActionListener {
        private DwToolBar adaptee;

        dwToolBar_jButton_Update_actionAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.trigger_Update(e);
        }
    }

    class dwToolBar_jButton_Add_actionAdapter implements ActionListener {
        private DwToolBar adaptee;

        dwToolBar_jButton_Add_actionAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.trigger_AddRow(e);
        }
    }

    class dwToolBar_jButton_First_actionAdapter implements ActionListener {
        private DwToolBar adaptee;

        dwToolBar_jButton_First_actionAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.trigger_FirstRow(e);
        }
    }

    class dwToolBar_jButtonRtrieve_actionAdapter implements ActionListener {
        private DwToolBar adaptee;

        dwToolBar_jButtonRtrieve_actionAdapter(DwToolBar adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.triggerRetrive(e);
        }
    }
}

