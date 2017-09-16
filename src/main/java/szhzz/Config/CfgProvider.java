package szhzz.Config;


import szhzz.Calendar.MyDate;
import szhzz.Utils.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: HuangFang
 * Date: 13-11-19
 * Time: 下午7:16
 * To change this template use File | Settings | File Templates.
 */
public class CfgProvider {
    private static Hashtable<String, CfgProvider> provider = new Hashtable<String, CfgProvider>();
    private Config cfg = null;
    private Hashtable<Object, Config> allCfgs;
    private LinkedList<String> cfgNames = new LinkedList<String>();

    private String cfgFolder = null;
    private String groupName = null;
    private static String configFolder = null;

    public static CfgProvider getInstance(String groupName) {
        CfgProvider onlyOne = provider.get(groupName);
        if (onlyOne == null) {
            onlyOne = new CfgProvider();
            onlyOne.laodCfgs(groupName);
            provider.put(groupName, onlyOne);
        }
        return onlyOne;
    }

    public static String getRootFolder() {
        if (configFolder == null) {
            configFolder = System.getProperty("user.dir") + "\\configs";
        }
        return configFolder;
    }

    public static void setConfigFolder(String configFolder) {
        CfgProvider.configFolder = configFolder;
    }

    public Config getNewCfg(String asName) {
        Config c = null;
        String cfgName = asName;

        if (hasCfg(cfgName)) {
            for (int suf = 0; suf < 1000; suf++) {
                cfgName = asName + "_" + suf;
                if (!hasCfg(cfgName)) break;
            }
        }

        c = getCfg(cfgName);
        c.setProperty("FileName", cfgName);

        return c;
    }

    public void addCfg(Config c) {
        String fileName = cfgFolder + c.getConfigID() + ".ini";
        cfgNames.add(c.getConfigID());
        allCfgs.put(c.getConfigID(), c);  //.toLowerCase()
    }

    public void save() {
        for (Config c : allCfgs.values()) {
            if (c != null) {
                c.save();
            }
        }
    }

    public void deleteCfg(Config cfg) {
        allCfgs.remove(cfg);
        cfg.clear();
    }

    public void loadCfg(String configID, boolean savePrev) {
        if (savePrev) {
            save();
        }
        cfg = getCfg(configID);
    }

    public LinkedList<String> getCfgIDs() {
        return (LinkedList<String>) cfgNames.clone();
    }

    public void reloadCfgs() {
        if (groupName != null) {
            laodCfgs(groupName);
        }
    }


    public String getDir() {
        return  getRootFolder() + "/" + groupName + "/";
    }

    protected void laodCfgs(String name) {
        this.groupName = name;
        cfgFolder = getDir();
        (new File(cfgFolder)).mkdirs();

        allCfgs = new Hashtable<Object, Config>();
        cfgNames.clear();

        File[] file = (new File(cfgFolder)).listFiles();
        if (file != null) {
            for (File aFile : file) {
                if (aFile.getName().toLowerCase().endsWith(".ini")) {
                    Config c = new ConfigF();
                    try {
                        c.load(aFile.getCanonicalPath());
                        cfgNames.add(c.getConfigID());
                        allCfgs.put(c.getConfigID(), c);  //.toLowerCase()
                        changeCfg(c);
                    } catch (IOException e) {

                    }
                }
            }
        }
    }

    /**
     * 用于手工调整，修改
     *
     * @param c
     */
    private void changeCfg(Config c) {
//        //

//        c.insertVal("融资比例", "允许透支", "false", "缺省为 false");
//        if (c.getIntVal("单只股票最大买入金额", -200) > 0) {
//            c.setProperty("允许透支", true);
//            System.out.println(c.getConfigID() + " 允许透支 = true");
//        }else{
//            c.setProperty("允许透支", false);
//            System.out.println(c.getConfigID() + " 允许透支 = false");
//        }
//        c.save();

//        c.removeProperty("提取股票数量");
//        c.removeProperty("随机挑选买入的股票");
//        c.renameProperty("模型名称", "交易策略模型");
//        c.renameProperty("数据类型", "量化分析模型");
//        c.renameProperty("板块优化", "板块联动");

//          if(!c.hasProperty("仓位控制模型")){
//              c.insertProperty("附加买入控制", "仓位控制模型", "", "");   //单只股票最大买入金额
//              c.save();
//          }

//        c.renameProperty("卖出时点", "超时卖出时点");
//

//        c.setProperty("锁定", "false", "缺省为false, 锁定后,该定义文件将不可修改");
//        c.setProperty("应用排除定义", "");
        //c.setProperty("最小交易金额", "1000");
//        if (c.getDoubleVal("最小交易金额", 0) == 0d) {
        //c.insertProperty("印花税", "最小交易金额", "1000", "单只股票最小交易金额");   //单只股票最大买入金额
//            c.renameProperty("最小交易金额", "单只股票最小买入金额");
//            c.save();
//        }


    }

    /**
     * 保证安全调用
     *
     * @param cfgID
     * @return
     */
    public Config getCfg(String cfgID, boolean createNew) {
        if (cfgID == null) return cfg;

        if (cfgID.endsWith(".ini")) {
            cfgID = cfgID.replace(".ini", "");
        }

        Config cfg = allCfgs.get(cfgID);
        if (cfg == null) {
            String fileName = cfgFolder + cfgID + ".ini";
            if (!new File(fileName).exists() && createNew) {
                try {
                    Utilities.String2File("// Create " + MyDate.getToday().getDateTime(), fileName, false);
                } catch (IOException e) {

                }
            }
            ConfigF cfgF = new ConfigF();
            cfgF.setConfigFileName(fileName);
            addCfg(cfgF);
            cfg = cfgF;
        }

//        if (cfg == null) {
//            cfg = new ConfigF();
//        }
        return cfg;
    }

    public Config getCfg(String cfgID) {
        return getCfg(cfgID, true);
    }

    public boolean hasCfg(String cfgID) {
        if (cfgID == null) return false;
        if (cfgID.endsWith(".ini")) {
            cfgID = cfgID.replace(".ini", "");
        }
        return allCfgs.get(cfgID) != null;
    }
}
