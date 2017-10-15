package szhzz.App;

/**
 * Created by Administrator on 2015/4/18.
 */
public abstract class ClareBuffer {
    public ClareBuffer() {
        AppManager.registerClareBuffer(this);
    }

    public abstract void clare();

    protected void logIt(Class parent) {
        AppManager.logit("Clare Dirty Data for " + parent.getName());
    }
}
