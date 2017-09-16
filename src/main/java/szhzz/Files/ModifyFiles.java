package szhzz.Files;

import szhzz.Utils.Utilities;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-9-9
 * Time: 0:48:02
 * To change this template use File | Settings | File Templates.
 */
public class ModifyFiles {
    public static void main(String[] args) throws IOException {
        String folder = ".\\src\\Adaptor\\";
        File[] fs = new File(folder).listFiles();

        for (File f : fs) {
            FileInputStream fin = new FileInputStream(f);
            BufferedReader in = new BufferedReader(new InputStreamReader(fin));
            String l;
            String fileName = f.getName();
            if (!"AquaLnFPopupLocationFix.code".equals(fileName)) continue;
            fileName = fileName.substring(0, fileName.indexOf(".")) + ".java";
            while ((l = in.readLine()) != null) {
                if (l.length() > 3) {
                    l = l.substring(4);
                    Utilities.String2File(l, folder + fileName, true);
                }
            }
        }
    }
}
