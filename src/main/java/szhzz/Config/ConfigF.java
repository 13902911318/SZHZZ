package szhzz.Config;

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
    private String configFileName = null;

    @Override
    public boolean save() {
        if (configFileName != null) {
            return saveAs(new File(configFileName));
        }
        return false;
    }

    @Override
    public void load(String file) {
        configFileName = file;
        try {
            configID = new File(file).getName();
            configID = configID.substring(0, configID.lastIndexOf("."));
        } catch (Exception ignored) {

        }
        if(!Utilities.isFileClosed(file, 100)) return;

        datas = new Hashtable<String, item>();
        index = new LinkedList<item>();

        FileInputStream in = null;
        BufferedReader buff = null;
        try {

            in = new FileInputStream(configFileName);
            buff = new BufferedReader(new InputStreamReader(in, "UTF-8"));

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

    public String getConfigFolder() {
        return new File(configFileName).getParent();
    }
}
