package szhzz.sql.database;

import szhzz.sql.gui.DataWindow;
import szhzz.sql.gui.DwPanel;
import szhzz.sql.jdbcpool.ConnectionManager;

import javax.swing.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

public class DbConnectionsView extends JDialog {
    DataStore ds = null;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private DwPanel taskDwPanel;
    private DataWindow DW;

    public DbConnectionsView() {
        setContentPane(contentPane);
        setModal(true);

        DW = taskDwPanel.getDataWindow();

//        getRootPane().setDefaultButton(buttonOK);
        this.setTitle("Connection pool");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        init();
        refresh();
    }

    public static void main(String[] args) {
        DbConnectionsView dialog = new DbConnectionsView();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    void init() {
        setUiTable();
    }

    void refresh() {
        ds.clear();

        try {
            DW.refresh();

            Hashtable h = ConnectionManager.getConnectionCount();
            for (Object k : h.keySet()) {
                int r = ds.appendRow();
                ds.updateObject(0, k.toString());
                ds.updateObject(1, h.get(k).toString());
            }

            LinkedList<Vector> l = ConnectionManager.peekAll();
            for (Vector k : l) {
                int r = ds.appendRow();
                ds.updateObject(0, k.get(0));
                ds.updateObject(1, k.get(1));
            }


            Vector<Vector> rows = Database.getOpenDBList();
            for (Vector row : rows) {
                int r = ds.appendRow();
                ds.updateObject(0, row.get(0));
                ds.updateObject(1, row.get(1));
            }


            DW.refresh();
        } catch (DBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void setUiTable() {
        if (ds == null)
            ds = new DataStore();

        ds.setColName("调用", 0);
        ds.setColTypeName("String", 0);
        ds.setColLength(0, 20);

        ds.setColName("URL", 1);
        ds.setColTypeName("String", 1);
        ds.setColLength(1, 30);

//        ds.setName(Utilities.getTableName("Stock Analyze"));
        ds.setReadOnly(true);

//            dm = new dbTableModel();
//            dm.setDataStore(ds);

        try {
            DW.setFilterNewRow(true);
            DW.shareData(ds);

        } catch (DBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
//        dm.fireTableStructureChanged();
//        dm.fireTableDataChanged();
    }

}
