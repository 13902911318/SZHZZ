package szhzz.Utils;


import szhzz.App.AppManager;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-1-13
 * Time: 15:22:01
 * To change this template use File | Settings | File Templates.
 */
public abstract class CanStop {
    boolean stop = false;

    protected boolean calledStop() {
        if (AppManager.getApp().isStopProcess()) {
            return true;
        }
        return ShutdownManager.STATE_SHUTDOWN || stop;
    }

    public void stopIt() {
        stop = true;
    }

    public void reset() {
        stop = false;
    }
}
