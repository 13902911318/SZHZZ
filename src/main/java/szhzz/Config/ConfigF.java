package szhzz.Config;

import szhzz.App.MessageAbstract;
import szhzz.Utils.DawLogger;
import szhzz.Utils.Utilities;

import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-1-25
 * Time: 9:45:22
 * To change this template use File | Settings | File Templates.
 */
public class ConfigF extends Config {
    private static DawLogger logger = DawLogger.getLogger(ConfigF.class);
    private String configFileName = null;
    private String cs = "UTF-8";

    @Override
    public boolean save() {
        if (configFileName != null) {
            if(!cs.equals("UTF-8")){
                logger.info(configFileName + " change encode from " + cs + " to UTF-8");
                new File(configFileName).delete();
                cs = "UTF-8";
            }
            return saveAs(new File(configFileName));
        }
        return false;
    }

    @Override
    public void load(String file) {
        if(file.contains("\\\\")){
            logger.error(new Exception("Error path format :" + file));
            file = file.replace("\\\\", "\\");
        }

        configFileName = file;
        try {
            configID = new File(file).getName();
            configID = configID.substring(0, configID.lastIndexOf("."));
        } catch (Exception ignored) {

        }
//        if (!Utilities.isFileClosed(file, 100)) return;

        datas = new Hashtable<String, item>();
        index = new LinkedList<item>();

        FileInputStream in = null;
        BufferedReader buff = null;
        try {
            cs = Utilities.detectCharset(configFileName);
            if (cs == null) {
                cs = System.getProperty("file.encoding");
            }
            in = new FileInputStream(configFileName);
            buff = new BufferedReader(new InputStreamReader(in, cs));
            loadDataVal(buff);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            checkCfg();
        }
    }


    @Override
    public void reLoad() {
        if (!isReloadProtect()) {
            load(configFileName);
        }
    }

    @Override
    public String getConfigUrl() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
        configID = new File(configFileName).getName();
        configID = configID.substring(0, configID.lastIndexOf("."));
    }

    public void setConfigFile(File configFile) {
        this.configFileName = configFile.getAbsolutePath();
        configID = configFile.getName();
        configID = configID.substring(0, configID.lastIndexOf("."));
    }
    public String getConfigFolder() {
        return new File(configFileName).getParent();
    }

    public static void main(String[] args ){
        ConfigF cfg = new ConfigF();
        cfg.load("D:\\tbd\\第一创业王宁C.ini");
        cfg.save();
        cfg = null;
    }
}
