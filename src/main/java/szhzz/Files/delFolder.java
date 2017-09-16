package szhzz.Files;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-2-15
 * Time: 12:51:15
 * To change this template use File | Settings | File Templates.
 */
public class delFolder {
    public static void main(String args[]) throws IOException {
        String2File("no", "Shutdown.txt", false);
        String url1 = args[0].trim();
        new delFolder().delDir(url1);
    }

    static boolean shutDown() {
        String Shutdown = File2String("Shutdown.txt");
        Shutdown = Shutdown.trim();
        return (Shutdown.trim().equalsIgnoreCase("yes"));
    }

    static String File2String(String fileName) {
        String Line;
        FileInputStream fin = null;
        StringBuffer strb = new StringBuffer("");
        try {
            fin = new FileInputStream(fileName);
            BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
            while ((Line = myInput.readLine()) != null) {
                strb.append(Line).append("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if (fin != null) fin.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return strb.toString();
    }

    static void String2File(String S, String fileName, boolean ab_append) throws IOException {
        PrintWriter f = null;
        try {
            f = new PrintWriter(new FileWriter(fileName, ab_append));
            f.println(S);
        } finally {
            if (f != null) f.close();
        }
    }

    public void delDir(String folder) {
        File f = new File(folder);
        if (f.exists() && f.isDirectory()) {
            delDir(f);
        }
    }

    public void delDir(File folder) {
        File[] file = folder.listFiles();
        if (file != null) {
            for (File aFile : file) {
                if (shutDown()) {
                    System.exit(3);
                }
                if (aFile.isDirectory()) {
                    delDir(aFile);
                    System.out.println("Delete " + aFile.getAbsolutePath());
                }
                aFile.delete();
            }
            System.gc();
        }
    }
}
