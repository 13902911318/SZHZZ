package szhzz.Files;

import szhzz.Utils.Utilities;

import java.io.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-1-1
 * Time: 14:00:15
 * To change this template use File | Settings | File Templates.
 */
public class Copydir {
    public static final int NEW_ONLY = 1;
    public static final int ADD_ONLY = 2;
    public static final int OVEWRITE = 4;

    Vector<String> finishedDir = new Vector<String>();
    String logFile = "./copydir.log";
    String finishedFolder = "./finishedFolder.txt";
    int writeModle = NEW_ONLY;


    public static void main(String args[]) throws IOException {
        int writeModle = 3;

        String url1 = args[0].trim();
        String url2 = args[1].trim();
        if (args.length > 2) {
            writeModle = Integer.parseInt(args[2].trim());
        }
        new Copydir().copy(url1, url2, writeModle);
    }

    static boolean shutDown() {
        String Shutdown = File2String("Shutdown.txt");
        Shutdown = Shutdown.trim();
        return (Shutdown.trim().equalsIgnoreCase("yes"));
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

//    boolean isOverWrite() {
//        return (writeModle & OVEWRITE) == OVEWRITE;
//    }

    public void copy(String file1, String file2, int writeModle) throws IOException {
        String2File("no", "Shutdown.txt", false);
        this.writeModle = writeModle;
        File log = new File(logFile);
        log.delete();

        loadHistory();
        copyDirectiory(file1, file2);
    }

    private void copyDirectiory(String file1, String file2) {
        (new File(file2)).mkdirs();
        File[] file = (new File(file1)).listFiles();

        for (File aFile : file) {
            if (shutDown()) {
                System.exit(3);
            }
            if (aFile.isFile()) {
                copyFile(aFile, new File(file2 + "/" + aFile.getName()));
            } else if (aFile.isDirectory()) {
                String currentFile = file2 + "/" + aFile.getName();
                if (isFinished(currentFile)) continue;

                System.out.println("DIR " + currentFile);
                copyDirectiory(file1 + "/" + aFile.getName(), currentFile);
                addFinished(currentFile);
            }
        }
    }

    void addFinished(String dirName) {
        try {
            Utilities.String2File(dirName, finishedFolder, true);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    boolean isNewOnly() {
        return (writeModle & NEW_ONLY) == NEW_ONLY;
    }

    boolean isAddOnly() {
        return (writeModle & ADD_ONLY) == ADD_ONLY;
    }

    boolean copyTextFile(File fSource, File fDest) {
        try {
            Utilities.String2File(Utilities.File2String(fSource), fDest, false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    void copyFile(File fSource, File fDest) {
        String f = fSource.getName().toLowerCase();
        if ("TableFormater.java".equals(fSource.getName())) {
            int a = 0;
        }
        if (f.endsWith(".java") || f.endsWith(".ini") || f.endsWith(".txt")) {
            if (copyTextFile(fSource, fDest))
                return;
        }

        if (fDest.exists()) {
            if (isAddOnly()) {
                System.out.println("Skip " + fDest.getAbsolutePath());
                return;
            } else if (fSource.lastModified() <= fDest.lastModified()) // ont newer
                return;
        }

        try {
            System.out.println("Copy To " + fDest);
            FileInputStream input = new FileInputStream(fSource);
            FileOutputStream output = new FileOutputStream(fDest);

            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = input.read(b)) != -1) {
                output.write(b, 0, len);
            }
            output.flush();
            output.close();
            input.close();

            if (!fSource.canWrite())
                fDest.setReadOnly();

            fDest.setLastModified(fSource.lastModified());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            try {
                Utilities.String2File("Copy Error\t" + fDest.getAbsolutePath(), logFile, true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    void loadHistory() {
        FileInputStream fin;
        try {
            fin = new FileInputStream(finishedFolder);
            BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
            String input;
            while ((input = myInput.readLine()) != null) {
                finishedDir.add(input.trim());
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    boolean isFinished(String f) {
        return finishedDir.contains(f);
    }
}


