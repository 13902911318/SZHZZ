package org.loon.simple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Copyright 2008 - 2009
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @author chenpeng
 * @version 0.1
 * @project loonframework
 * @email：ceponline@yahoo.com.cn
 */
public class Conversion extends JFrame implements ActionListener {

    final static public String encoding = "UTF-8";
    final static public String LS = System.getProperty("line.separator", "\n");
    final static public String FS = System.getProperty("file.separator", "\\");
    /**
     *
     */
    private static final long serialVersionUID = 495673522805861982L;
    private static JFrame staticThis;

    private boolean converted;

    private File outFile;

    private Properties properties;

    private JCheckBox jCheckBox1;

    private JButton jButton1;

    private JButton jButton2;

    private JButton jButton3;

    private JButton jButton4;

    private JComboBox jComboBox1;

    private JComboBox jComboBox2;

    private JLabel jLabel1;

    private JLabel jLabel2;

    private JLabel jLabel3;

    private JLabel jLabel4;

    private JLabel jLabel5;

    private javax.swing.JLabel jLabel6;

    private JSeparator jSeparator1;

    private JTextField jTextField1;

    private JTextField jTextField2;

    private JTextField jTextField3;

    public Conversion(String title) {
        super(title);

        outFile = new File(System.getProperty("user.dir") + FS
                + "conversion.cfg");

        if (!outFile.exists()) {
            try {
                outFile.createNewFile();
            } catch (IOException e) {
            }
        }

        properties = Conversion.loadProperties(outFile);

        staticThis = this;

        jCheckBox1 = new JCheckBox();
        jLabel1 = new JLabel();
        jTextField1 = new JTextField();
        jLabel2 = new JLabel();
        jTextField2 = new JTextField();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jComboBox1 = new JComboBox();
        jComboBox2 = new JComboBox();
        jButton1 = new JButton();
        jButton2 = new JButton();
        jTextField3 = new JTextField();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        jSeparator1 = new JSeparator();
        jButton3 = new JButton();
        jButton4 = new JButton();

        getContentPane().setLayout(null);

        jCheckBox1.setText("覆盖原始文件");
        getContentPane().add(jCheckBox1);
        jCheckBox1.setBounds(420, 130, 110, 23);
        jCheckBox1.addActionListener(this);

        jLabel1.setText("输出路径：");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(10, 50, 70, 30);

        jTextField1.setText(properties.getProperty("out") == null ? System
                .getProperty("user.dir") : properties.getProperty("out"));
        getContentPane().add(jTextField1);
        jTextField1.setBounds(80, 50, 330, 30);

        if (jTextField1.getText().trim().length() == 0) {
            System.out.println("!!!");
            jTextField1.setEditable(false);
            jCheckBox1.setSelected(true);
        }

        jLabel2.setText("目标选择：");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(10, 10, 70, 30);

        jTextField2.setText(properties.getProperty("in") == null ? System
                .getProperty("user.dir") : properties.getProperty("in"));
        getContentPane().add(jTextField2);
        jTextField2.setBounds(80, 10, 330, 30);

        jLabel3.setText("输出编码：");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(180, 90, 70, 30);

        jLabel4.setText("过滤文件：");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(10, 130, 70, 30);

        jComboBox1.setModel(new DefaultComboBoxModel(new String[]{"UTF-8",
                "GB18030", "GB2312", "GBK", "Big5", "EUC-JP", "ISO-2022-JP",
                "Shift-JIS"}));
        getContentPane().add(jComboBox1);
        jComboBox1.setBounds(250, 90, 80, 30);

        jComboBox2.setModel(new DefaultComboBoxModel(new String[]{
                "Shift-JIS", "ISO-2022-JP", "EUC-JP", "Big5", "GBK", "GB2312",
                "GB18030", "UTF-8"}));
        getContentPane().add(jComboBox2);
        jComboBox2.setBounds(80, 90, 90, 30);

        jButton1.setText("选择目录");
        getContentPane().add(jButton1);
        jButton1.setBounds(420, 50, 110, 30);

        jButton2.setText("选择目录");
        getContentPane().add(jButton2);
        jButton2.setBounds(420, 10, 110, 30);

        jTextField3
                .setText(properties.getProperty("split") == null ? "java,lua,ks,tjs,txt"
                        : properties.getProperty("split"));
        getContentPane().add(jTextField3);
        jTextField3.setBounds(80, 130, 330, 30);

        jLabel5.setText("目标编码：");
        getContentPane().add(jLabel5);
        jLabel5.setBounds(10, 90, 70, 30);

        jLabel6.setText("");
        getContentPane().add(jLabel6);
        jLabel6.setBounds(10, 200, 310, 30);

        getContentPane().add(jSeparator1);
        jSeparator1.setBounds(0, 180, 540, 10);

        jButton3.setText("退出");
        getContentPane().add(jButton3);
        jButton3.setBounds(440, 200, 90, 30);

        jButton4.setText("开始转换");
        getContentPane().add(jButton4);
        jButton4.setBounds(340, 200, 90, 30);

        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(this);
        jButton4.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                save();
                System.exit(0);
            }
        });

        pack();
        setSize(new Dimension(545, 270));
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private static final boolean copyFile(final String sSource,
                                          final String sDest, final String srcEncoding,
                                          final String destEncoding) {
        try {
            File src = new File(sSource);
            if (!src.exists()) {
                return false;
            }
            File dest = new File(sDest);
            String reuslt = readFile(src, srcEncoding);
            write(dest, reuslt, destEncoding);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static void write(File file, String context, String coding)
            throws IOException {
        write(file, context.getBytes(coding), false);
    }

    private static void write(File file, byte[] bytes, boolean append)
            throws IOException {
        write(file, new ByteArrayInputStream(bytes), append);
    }

    private static void checkFile(File file) throws IOException {
        boolean exists = file.exists();
        if (exists && !file.isFile()) {
            throw new IOException("File " + file.getPath()
                    + " is actually not a file.");
        }
    }

    private static void makedirs(File file) throws IOException {
        checkFile(file);
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new IOException("Creating directories "
                        + parentFile.getPath() + " failed.");
            }
        }
    }

    private static void write(File file, InputStream input, boolean append)
            throws IOException {
        makedirs(file);
        BufferedOutputStream output = null;
        try {
            int contentLength = input.available();
            output = new BufferedOutputStream(
                    new FileOutputStream(file, append));
            while (contentLength-- > 0) {
                output.write(input.read());
            }
        } finally {
            close(input, file);
            close(output, file);
        }
    }

    private static void close(InputStream input, File file) {
        if (input != null) {
            try {
                input.close();
                input = null;
            } catch (IOException e) {

            }
        }
    }

    private static void close(OutputStream output, File file) {
        if (output != null) {
            try {
                output.close();
                output = null;
            } catch (IOException e) {
            }
        }
    }

    private static void close(Reader reader, File file) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

    public static String readFile(File file, String encode) throws IOException {
        BufferedReader reader = null;
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            InputStreamReader isr = new InputStreamReader(in, encode);
            reader = new BufferedReader(isr);
            StringBuffer sbr = new StringBuffer();
            for (String line = ""; (line = reader.readLine()) != null; ) {
                sbr.append(line + LS);
            }
            return sbr.toString();
        } finally {
            close(in, file);
            close(reader, file);
        }
    }

    public static ArrayList getAllFiles(String path, String ext)
            throws IOException {
        File file = new File(path);
        ArrayList ret = new ArrayList(20);
        String[] exts = ext.split(",");
        String[] listFile = file.list();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                File tempfile = new File(path + FS + listFile[i]);
                if (tempfile.isDirectory()) {
                    ArrayList arr = getAllFiles(tempfile.getPath(), ext);
                    ret.addAll(arr);
                    arr.clear();
                    arr = null;
                } else {
                    for (int j = 0; j < exts.length; j++) {
                        if (getExtension(tempfile.getAbsolutePath())
                                .equalsIgnoreCase(exts[j])) {
                            ret.add(tempfile.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static String getExtension(String name) {
        if (name == null) {
            return "";
        }
        int index = name.lastIndexOf(".");
        if (index == -1) {
            return "";
        } else {
            return name.substring(index + 1);
        }
    }

    public static String getSuperDirectory(String name) {
        if (name == null) {
            return "";
        }
        int length = name.length();
        int size = name.lastIndexOf(FS) + 1;
        if (size < length) {
            return name.substring(size, length);
        } else {
            return "";
        }
    }

    private static String getSpecialString(String context, String encoding) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(context
                    .getBytes());
            InputStreamReader isr = new InputStreamReader(in, encoding);
            BufferedReader reader = new BufferedReader(isr);
            StringBuffer buffer = new StringBuffer();
            String result;
            while ((result = reader.readLine()) != null) {
                buffer.append(result);
            }
            return buffer.toString();
        } catch (Exception ex) {
            return context;
        }
    }

    private static Properties loadProperties(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        Properties properties = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            properties.load(in);
            close(in, file);
        } catch (IOException e) {
        }
        return properties;
    }

    private static boolean isChinaLanguage(String str) {
        char[] chars = str.toCharArray();
        int[] ints = new int[2];
        boolean isChinese = false;
        int length = chars.length;
        byte[] bytes = null;
        for (int i = 0; i < length; i++) {
            bytes = ("" + chars[i]).getBytes();
            if (bytes.length == 2) {
                ints[0] = bytes[0] & 0xff;
                ints[1] = bytes[1] & 0xff;
                if (ints[0] >= 0x81 && ints[0] <= 0xFE && ints[1] >= 0x40
                        && ints[1] <= 0xFE) {
                    isChinese = true;
                }
            } else {
                return false;
            }
        }
        return isChinese;
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                }
                new Conversion("文件编码转换工具-0.1.0");
            }
        });
    }

    private void start() {
        jTextField1.setEditable(false);
        jTextField2.setEditable(false);
        jTextField3.setEditable(false);
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);
        jButton3.setEnabled(false);
        jButton4.setEnabled(false);
        jCheckBox1.setEnabled(false);
        if (!converted) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    converted = true;
                    String src = jTextField2.getText().trim();
                    String dest = jTextField1.getText().trim();
                    if (!dest.endsWith(FS)) {
                        dest += FS;
                    }
                    boolean select = jCheckBox1.isSelected();
                    String fileName;
                    String srcEncoding = (String) jComboBox2.getSelectedItem();
                    String destEncoding = (String) jComboBox1.getSelectedItem();
                    String ext = jTextField3.getText().trim();
                    try {
                        List list = getAllFiles(src, ext);
                        for (Iterator it = list.iterator(); it.hasNext(); ) {
                            File file = new File((String) it.next());
                            if (file.exists()) {
                                fileName = file.getAbsolutePath();
                                if (!select) {
                                    copyFile(fileName, dest
                                                    + getSuperDirectory(file
                                                    .getParent()) + FS
                                                    + file.getName(), srcEncoding,
                                            destEncoding);
                                } else {
                                    String result = fileName;
                                    String[] fileNames = result
                                            .split("\\" + FS);
                                    StringBuffer buffer = new StringBuffer();
                                    for (int i = 0; i < fileNames.length; i++) {
                                        if (isChinaLanguage(new String(
                                                fileNames[i].getBytes(),
                                                "GB2312"))) {
                                            buffer.append(fileNames[i]);
                                        } else {
                                            buffer.append(getSpecialString(
                                                    fileNames[i], srcEncoding));
                                        }
                                        buffer.append(FS);
                                    }
                                    result = buffer.delete(buffer.length() - 1,
                                            buffer.length()).toString();
                                    copyFile(fileName, result, srcEncoding,
                                            destEncoding);
                                }
                                jLabel6.setText("输出文件 " + fileName);
                            }
                        }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(staticThis, e
                                .getMessage());
                    }
                    jTextField1.setEditable(true);
                    jTextField2.setEditable(true);
                    jTextField3.setEditable(true);
                    jButton1.setEnabled(true);
                    jButton2.setEnabled(true);
                    jButton3.setEnabled(true);
                    jButton4.setEnabled(true);
                    jCheckBox1.setEnabled(true);
                    jCheckBox1.setSelected(false);
                    jLabel6.setText("");
                    converted = false;
                }
            });
            thread.start();
        }
    }

    private void save() {
        try {
            properties.setProperty("split", jTextField3.getText().trim());
            properties.setProperty("in", jTextField2.getText().trim());
            properties.setProperty("out", jTextField1.getText().trim());
            properties.store(new FileOutputStream(outFile), "SAVE");
        } catch (Exception ex) {

        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int ret = 0;
        if (source instanceof JButton) {
            if (source == jButton1) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                ret = chooser.showOpenDialog(this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    jTextField1.setText(chooser.getSelectedFile()
                            .getAbsolutePath());
                }
            } else if (source == jButton2) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                ret = chooser.showOpenDialog(this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    jTextField2.setText(chooser.getSelectedFile()
                            .getAbsolutePath());
                }
            } else if (source == jButton3) {
                save();
                System.exit(0);
            } else if (source == jButton4) {
                File f = new File(jTextField2.getText().trim());
                if (!f.exists()) {
                    JOptionPane.showMessageDialog(this, "指定路径不存在，数据无法导出 !");
                }
                start();
            }
        } else if (source instanceof JCheckBox) {
            if (source == jCheckBox1) {

                if (jCheckBox1.isSelected()) {
                    jTextField1.setText("");
                    jTextField1.setEditable(false);
                    jButton1.setEnabled(false);
                } else {
                    jTextField1.setEditable(true);
                    jButton1.setEnabled(true);
                }
            }
        }
    }

}
