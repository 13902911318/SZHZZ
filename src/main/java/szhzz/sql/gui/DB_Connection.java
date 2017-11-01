package szhzz.sql.gui;

import szhzz.App.AppManager;
import szhzz.Files.ExtensionFileFilter;
import szhzz.Utils.DawLogger;
import szhzz.sql.database.DBException;
import szhzz.sql.database.DBProper;
import szhzz.sql.database.Database;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class DB_Connection extends JDialog {
    private static DawLogger logger = DawLogger.getLogger(DB_Connection.class);
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField_Driver;
    private JTextField textField_Host;
    private JTextField textField_User;
    private JTextField textField_Port;
    private JPasswordField passwordField1;
    private JTextField textField_Schema;
    private JLabel FileName;
    private JButton testConnectionButton;
    private JLabel connectionStatu;
    private JButton button1;
    private DBProper data;

    public DB_Connection() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
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
        testConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testConnection();
            }
        });
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File currentDir = new File(data.getFileName());
                String f = fileChoise("Open a source DB Proper", currentDir);
                if (f != null) {
                    try {
                        data.reload(f);
                        setData(data);
                    } catch (DBException e1) {
                        logger.error(e1);
                    }
                }
            }
        });
    }

    public static void main(String[] args) throws DBException {
        String file = AppManager.getConfigFolder() + "/RemoteSQL.txt";
        DBProper data;
        data = new DBProper(file);

        DB_Connection dialog = new DB_Connection();
        dialog.setData(data);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private String fileChoise(String title, File currentDir) {
        String fileName = null;
        JFileChooser fc = new JFileChooser(currentDir);
        fc.setDialogTitle(title);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter jpegFilter = new ExtensionFileFilter("TEXT Files", new String[]{"TXT", "Text"});
        fc.addChoosableFileFilter(jpegFilter);
        int returnValue = fc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            if (selectedFile.exists()) {
                fileName = selectedFile.getAbsolutePath();
            }
        }
        return fileName;
    }

    private void testConnection() {
        DBProper proper = null;
        try {
            String f = AppManager.getApp().getCurrentDBCfg();
            proper = new DBProper(f);
        } catch (DBException e) {
            logger.error(e);
            return;
        }
        getData(proper);
        Database dbLocal = Database.getInstance(proper, this.getClass());
        try {
            dbLocal.openDB();
        } catch (DBException ignored) {

        }
        if (dbLocal.isOpened()) {
            connectionStatu.setText("Connection Success!");
        } else {
            connectionStatu.setText("Connection False!");
        }
        dbLocal.close();
    }

    private void onOK() {
        if (isModified()) {
            getData(data);
            try {
                data.save();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

//    public void initStock() {
//        data.setDriverClass(textField_Driver.getText());
//        data.setHost(textField_Host.getText());
//        data.setPort(textField_Port.getText());
//        data.setUser(textField_User.getText());
//        data.setPassword(new String(passwordField1.getPassword()));
//        data.setSchema(textField_Schema.getText());
//        data.composeMySqlUrl();
//    }

    public void setData(DBProper data) {
        this.data = data;
        textField_Driver.setText(data.getDriverClass());
        textField_Host.setText(data.getHost());
        textField_Port.setText(data.getPort());
        textField_User.setText(data.getUser());
        textField_Schema.setText(data.getSchema());
        String pass = data.getPassword();
        passwordField1.setText(pass);
        FileName.setText(data.getFileName());
    }

    public void getData(DBProper proper) {
        if (proper == null) return;
        proper.setDriverClass(textField_Driver.getText());
        proper.setHost(textField_Host.getText());
        proper.setPort(textField_Port.getText());
        proper.setUser(textField_User.getText());
        proper.setPassword(new String(passwordField1.getPassword()));
        proper.setSchema(textField_Schema.getText());
        proper.composeMySqlUrl();
    }

// centers the bandManager within the parent container [1.1]

    public boolean isModified() {
        if (textField_Driver.getText() != null ? !textField_Driver.getText().equals(data.getDriverClass()) : data.getDriverClass() != null)
            return true;
        if (textField_Host.getText() != null ? !textField_Host.getText().equals(data.getHost()) : data.getHost() != null)
            return true;
        if (textField_Schema.getText() != null ? !textField_Schema.getText().equals(data.getSchema()) : data.getSchema() != null)
            return true;
        if (textField_User.getText() != null ? !textField_User.getText().equals(data.getUser()) : data.getUser() != null)
            return true;
        if (textField_Port.getText() != null ? !textField_Port.getText().equals(data.getPort()) : data.getPort() != null)
            return true;
        String pass = new String(passwordField1.getPassword());
        return passwordField1.getPassword() != null ? !pass.equals(data.getPassword()) : data.getPassword() != null;
    }

}
