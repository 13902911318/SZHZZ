package szhzz.Config;

import szhzz.App.AppManager;

/**
 * Created by HuangFang on 2015/4/5.
 * 12:43
 */
public class SharedCfgProvider extends CfgProvider {
    private static AppManager App = AppManager.getApp();
    private static SharedCfgProvider onlyOne = null;
    private String shareFolder = null;

    public static CfgProvider getInstance(String groupName) {
        CfgProvider onlyOne = provider.get(groupName);
        if (onlyOne == null) {
            onlyOne = new SharedCfgProvider();
            onlyOne.laodCfgs(groupName);
            provider.put(groupName, onlyOne);
        }
        return onlyOne;
    }


    public String getDir() {
        if (shareFolder == null) {
            shareFolder = getShareFolder() + "\\" + groupName;
        }
        return shareFolder;
    }

}
