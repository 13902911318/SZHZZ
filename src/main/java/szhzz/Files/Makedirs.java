package szhzz.Files;

import java.io.File;
import java.io.IOException;
import java.util.Vector;


/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-1-1
 * Time: 14:00:15
 * To change this template use File | Settings | File Templates.
 */
public class Makedirs {
    public static final int NEW_ONLY = 1;
    public static final int ADD_ONLY = 2;
    public static final int OVEWRITE = 3;
    public static String sourceDir = "";
    public static String distDir = "";
    Vector<String> finishedDir = new Vector<String>();

    public static void main(String args[]) throws IOException {


        String url1 = args[0].trim();
        String url2 = args[1].trim();
        sourceDir = url1;
        distDir = url2;
        new Makedirs().MakeDir(url1);
    }

    void MakeDir(String file1) throws IOException {
        DrillDirectiory(file1);
    }

    private void DrillDirectiory(String file1) {

        File[] file = (new File(file1)).listFiles();
        for (File aFile : file) {
            if (aFile.isDirectory()) {
                String currentFile = file1 + "\\" + aFile.getName();

                // System.out.println("DIR " + currentFile);
                DrillDirectiory(file1 + "\\" + aFile.getName());
            }
        }
        String newDir = file1.replace(sourceDir, distDir);
        if (!new File(newDir).exists()) {
            (new File(newDir)).mkdirs();
        }
    }
}