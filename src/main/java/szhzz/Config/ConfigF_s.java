package szhzz.Config;

import szhzz.Utils.Chelper;
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
public class ConfigF_s extends ConfigF {
    private static DawLogger logger = DawLogger.getLogger(ConfigF_s.class);
    protected boolean isSafeModle(){
        return true;
    }

    public static void main(String[] args ){
        ConfigF cfg = new ConfigF_s();
        cfg.load("D:\\tbd\\第一创业王宁C.ini");
        cfg.save();
        cfg = null;
    }
}
