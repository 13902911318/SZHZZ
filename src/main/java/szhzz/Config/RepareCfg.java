package szhzz.Config;

import szhzz.Utils.DawLogger;

import java.io.File;
import java.util.Set;
import java.util.Vector;

/**
 * Created by Administrator on 2019/8/1.
 */
public class RepareCfg {
    private static DawLogger logger = DawLogger.getLogger(RepareCfg.class);

    static String compCfgDir = "G:\\";

    public static void main(String[] args) {
//        new RepareCfg().check("C:\\Users\\Administrator\\Quant");

        //检查 ini 文件是否含有密码
        Config.checkPassword = true;
        String f = "C:\\Users\\Administrator\\Quant";
//        String f = "D:\\ControlCenter";
        new RepareCfg().scan(new File(f));

    }

    private void check(String file1) {
        File[] file = (new File(file1)).listFiles();

        for (File aFile : file) {
            if (aFile.isFile()) {
                checkCfg(aFile);
            } else if (aFile.isDirectory()) {
                check(aFile.getAbsolutePath());
            }
        }
    }

    void checkCfg(File f) {
        String cfgFile = f.getAbsolutePath();
        if (!cfgFile.toLowerCase().endsWith(".ini")) return;
//        System.out.println(cfgFile);
        Config compCfg = null;

        Config cfg = new ConfigF();
        cfg.hideProtect = false;
        cfg.load(cfgFile);

        Vector<String> keys = cfg.getAllKeys();
        boolean isDirty = false;
        if ("C:\\Users\\Administrator\\Quant\\StockWin\\Broker\\九派宁灼1号.ini".equals(cfgFile)) {
            int a = 0;
        }
        String compDir = compCfgDir + cfgFile.substring(cfgFile.indexOf("Quant"));
        if (new File(compDir).exists()) {
            compCfg = new ConfigF();
            compCfg.load(compDir);
        }

        for (String key : keys) {
            String comment = null;

            if ("[Password!]".equals(cfg.getComment(key))) {
                if (compCfg != null) {
                    comment = compCfg.getComment(key);
                }
                if (comment == null) {
                    comment = "";
                }
                cfg.setComment(key, comment);
                if (!isDirty) {
                    System.out.println(cfgFile + "\t" + key + " comment=" + (comment.length() == 0 ? "\"\"" : comment));
                    isDirty = true;
                }
            }
        }

        Set<String> children = cfg.getChildrenNames();
        if (children != null) {
            for (String child : children) {
                Config compChildCfg = null;

                Config childCfg = cfg.getChild(child);

                if (compCfg != null) {
                    compChildCfg = compCfg.getChild(child);
                }
                keys = childCfg.getAllKeys();
                for (String key : keys) {
                    String comment = null;
                    if ("[Password!]".equals(childCfg.getComment(key))) {
                        if (compChildCfg != null)
                            comment = compChildCfg.getComment(key);

                        if (comment == null) {
                            comment = "";
                        }
                        childCfg.setComment(key, comment);
                        if (!isDirty) {
                            System.out.println(cfgFile + "\tChild=" + child + "\t" + key + " comment=" + (comment.length() == 0 ? "\"\"" : comment));
                            isDirty = true;
                        }
                    }
                }
            }
        }

        if (isDirty) {
            cfg.save();
        }
    }

    private void scan(File file1) {

        File[] file = (file1).listFiles();

        for (File aFile : file) {
            if (aFile.isFile()) {
                if (aFile.getAbsolutePath().toLowerCase().endsWith(".ini")) {
                    Config cfg = new ConfigF();
                    cfg.load(aFile.getAbsolutePath());
                }
            } else if (aFile.isDirectory()) {
                scan(aFile);
            }
        }
    }

}
