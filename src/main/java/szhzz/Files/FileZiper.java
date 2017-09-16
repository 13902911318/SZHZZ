package szhzz.Files;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: wn
 * Date: 2009-2-6
 * Time: 18:31:14
 * To change this template use File | Settings | File Templates.
 */
public class FileZiper {
    public static int dataLen = 4096;
    ZipOutputStream zipout = null;
    Exception createException = null;
    ZipEntry currentEntry = null;

    public static void main(String args[]) {
        System.out.println(new File("I:\\Downloads\\操作系统\\MAC\\IBM.txt").getName());
    }

    public void setZipOut(String outputFileName) {
        setZipOut(outputFileName, new File(outputFileName).getName());
    }

    public void setEntryComment(String com) {
        currentEntry.setComment(com);
    }

    public void setZipOut(String outputFileName, String base) {
        try {
            if (zipout == null) {
                zipout = new ZipOutputStream(new FileOutputStream(outputFileName));
                zipout.setLevel(Deflater.BEST_COMPRESSION);
            }
//            zipout.setMethod(ZipOutputStream.STORED);
            currentEntry = new ZipEntry(base);
            zipout.putNextEntry(currentEntry);
        } catch (FileNotFoundException ignored) {
            createException = ignored;
        } catch (IOException e) {
            createException = e;
        }
    }

    public BufferedReader setZipInput(String file) {
        return setZipInput(new File(file));
    }

    public BufferedReader setZipInput(File file) {
        ZipEntry entry;
        try {
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream Zreader = new ZipInputStream(new BufferedInputStream(fis));
            while ((entry = Zreader.getNextEntry()) != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(Zreader));
                return in;
//                    String l;
//                    while ((l = in.readLine()) != null) {
//                        System.out.println(l);
//                    }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public ZipInputStream getZipInputStream(File file) throws FileNotFoundException {
        ZipInputStream Zreader = null;
        FileInputStream fis = new FileInputStream(file);
        Zreader = new ZipInputStream(new BufferedInputStream(fis));
        return Zreader;
    }

    public ZipInputStream getZipInputStream(String file) throws FileNotFoundException {
        return getZipInputStream(new File(file));
    }

    public void out(String o) throws Exception {
        if (createException != null) throw createException;

        InputStream in = new ByteArrayInputStream(o.getBytes());
        int b;
        while ((b = in.read()) != -1) {
            zipout.write(b);
        }
        in.close();
    }

    public void addFile(File file) throws Exception {
        if (createException != null) throw createException;
        FileInputStream in = null;

        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = in.read(bytes)) >= 0) {
                zipout.write(bytes, 0, length);
            }

//        int b;
//        while ((b = in.read()) != -1) {
//            zipout.write(b);
//        }
        } finally {
            if (in != null)
                in.close();
        }

    }

    public void closeEntry() throws IOException {

        if (zipout != null) {
            zipout.closeEntry();
        }

    }

    public void close() throws IOException {

        if (zipout != null) {
            zipout.close();
        }

    }

}
