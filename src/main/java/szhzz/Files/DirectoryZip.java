package szhzz.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2007-12-25
 * Time: 22:08:53
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryZip {
    public static void main(String[] args) {
        DirectoryZip m_zip = new DirectoryZip();
        StringBuffer sb;

        try {
            m_zip.zip(args[0], "release\\2005.zip");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param inputFileName, file or directory waiting for zipping ,outputFileName output file name
     */
    public void zip(String inputFileName, String outputFileName) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFileName));
        zip(out, new File(inputFileName), "");
        // System.out.println("zip done");
        out.close();
    }

    private void zip(ZipOutputStream out, File f, String base) throws Exception {
        if (f.isDirectory()) {
            File[] fl = f.listFiles();
            if (System.getProperty("os.name").startsWith("Windows")) {
                out.putNextEntry(new ZipEntry(base + "\\"));
                base = base.length() == 0 ? "" : base + "\\";
            } else {
                out.putNextEntry(new ZipEntry(base + "/"));
                base = base.length() == 0 ? "" : base + "/";
            }
            for (int i = 0; i < fl.length; i++) {
                zip(out, fl[i], base + fl[i].getName());
            }
        } else {
            out.putNextEntry(new ZipEntry(base));
            FileInputStream in = new FileInputStream(f);
            int b;
            // System.out.println(base);
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            in.close();
        }
    }
}

