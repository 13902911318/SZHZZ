package szhzz.BatchRun;



import szhzz.App.AppManager;

import java.util.Vector;

/**
 * Created by Administrator on 2015/10/27.
 */
public abstract class JobProvider {
    static AppManager App = AppManager.getApp();
    private final Vector<Runnable> jobs = new Vector<>();

    public abstract Runnable getJob();

    void clare() {
        while (jobs.size() > 0) {
            Runnable o = jobs.remove(0);
            destrol(o);
        }
    }

    public void destrol(Object o) {

    }

    public void oneJobDone(Runnable job) {
        jobs.add(job);
        App.logit(job.toString() + " returned ");
    }

    protected Runnable nextJob() {
        Runnable r = null;
        try {
            r = jobs.remove(0);
        } catch (Exception ignored) {
            App.logit("no more laber in pool ");
        }
        return r;
    }
}
