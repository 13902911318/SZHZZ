package szhzz.Config;


import szhzz.App.AppManager;
import szhzz.Calendar.MyDate;
import szhzz.Utils.DawLogger;
import szhzz.Utils.HardwareIDs;
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
    static DawLogger logger = DawLogger.getLogger(CfgProvider.class);
    protected static Hashtable<String, CfgProvider> provider = new Hashtable<String, CfgProvider>();
    protected Config cfg = null;
    protected Hashtable<String, Config> allCfgs;
    //protected LinkedList<String> cfgNames = new LinkedList<String>();

    protected String cfgFolder = null;
    protected String groupName = null;
    protected static String appConfigFolder = null;
    protected static String appClass = "default";
    private static CfgEditor cfgEditor = null;
    private static String configRoot = "Quant";
    private boolean safeModel = false;

    protected void setSafeModel(boolean safeModle){
        this.safeModel = safeModle;
    }

    public boolean isSafeModel(){
        return safeModel;
    }
    public static CfgProvider getInstance(String groupName) {
        return getInstance(groupName, false) ;
    }
    public static CfgProvider getInstance(String groupName, boolean safeModel) {
        CfgProvider onlyOne = provider.get(groupName);
        if (onlyOne == null) {
            if(groupName.equals(DatabaseCfgProvider.gourpID)){
                onlyOne = new DatabaseCfgProvider();
            }else {
                onlyOne = new CfgProvider();
            }
            onlyOne.setSafeModel(safeModel);
            onlyOne.laodCfgs(groupName);
            provider.put(groupName, onlyOne);
        }else{
            if (safeModel && !onlyOne.isSafeModel()){
                onlyOne.setSafeModel(safeModel);
                onlyOne.reloadCfgs();
            }
        }
        return onlyOne;
    }

    public static String getRootFolder() {
        if (appConfigFolder == null) {
//            appConfigFolder = System.getProperty("user.dir") + "\\configs\\" + CfgProvider.appClass;
            appConfigFolder = HardwareIDs.getEnv("QuantHome") + "\\" + configRoot + "\\" + CfgProvider.appClass;
            new File(appConfigFolder).mkdirs();
        }
        return appConfigFolder;
    }

    public static String getShareFolder() {
        return HardwareIDs.getEnv("QuantHome") + "\\" + configRoot + "\\Share";
    }

    public static void setAppClass(Class appClass) {
        CfgProvider.appClass = appClass.getSimpleName();
        appConfigFolder = null;
        getRootFolder();
    }

    public static void setConfigRoot(String configRoot) {
        CfgProvider.configRoot = configRoot;
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
//        String fileName = cfgFolder + "\\" + c.getConfigID() + ".ini";
//        cfgNames.add(c.getConfigID());
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
        try {
            new File(cfg.getConfigUrl()).delete();
        } catch (Exception e) {

        }
        cfg.clear();
    }


    public Config loadCfg(String configID, boolean savePrev) {
        if (savePrev && cfg != null) {
            cfg.save();
        }
        cfg = getCfg(configID);
        return cfg;
    }

    public LinkedList<String> getCfgIDs() {
        LinkedList<String> list = new LinkedList<>();
        return (LinkedList<String>) cfgNames.clone();
    }

    public void reloadCfgs() {
        if (groupName != null) {
            laodCfgs(groupName);
        }
    }

    /**
     * 重读当前节点的 cfg
     */
    public void reLoadCurrentCfg() {
        if (cfg == null) {
            return;
        }
        cfg.reLoad();
    }

    public String getDir() {
        return getRootFolder() + "/" + groupName;
    }
    public String toString(){
        return getRootFolder() + "/" + groupName;
    }

    protected void laodCfgs(String name) {
        this.groupName = name;
        cfgFolder = getDir();
        (new File(cfgFolder)).mkdirs();

        allCfgs = new Hashtable<Object, Config>();
//        cfgNames.clear();

        File[] file = (new File(cfgFolder)).listFiles();
        if (file != null) {
            for (File aFile : file) {
                if (aFile.getName().toLowerCase().endsWith(".ini")) {
                    Config c;
//                    if(isSafeModel()){
//                        c = new ConfigF_s();
//                    }else{
                        c = new ConfigF();
//                    }
                    c.setSafe(isSafeModel());
                    try {
                        c.load(aFile.getCanonicalPath());
//                        cfgNames.add(c.getConfigID());
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
            String fileName = cfgFolder + "\\" + cfgID + ".ini";
            if (!new File(fileName).exists()) {
                if (createNew) {
                    try {
                        Utilities.String2File("// Create " + MyDate.getToday().getDateTime(), fileName, false);
                    } catch (IOException e) {

                    }
                } else {
                    return null;
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


    public static CfgEditor getCfgEditor() {
        if (cfgEditor == null) {
            cfgEditor = new CfgEditor(AppManager.getApp().getMainFram());
            cfgEditor.pack();
        }
        cfgEditor.setVisible(true);
        return cfgEditor;
    }

    public static void editCfg(String groupName, String cfgID) {
        Config cfg = CfgProvider.getInstance(groupName).getCfg(cfgID);
        if (cfg != null) {
            getCfgEditor();
            cfgEditor.setCfg(cfg);
        }
    }

    public static void editCfg(Config cfg) {
        if (cfg != null) {
            getCfgEditor();
            cfgEditor.setCfg(cfg);
        }
    }
}
