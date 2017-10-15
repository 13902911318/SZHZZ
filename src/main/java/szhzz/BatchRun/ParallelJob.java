package szhzz.BatchRun;


import szhzz.App.AppManager;
import szhzz.Utils.DawLogger;

import java.util.LinkedList;

/**
 * Created by Administrator on 2015/10/27.
 * <p>
 * 并行执行程序
 */
public class ParallelJob {
    static AppManager App = AppManager.getApp();
    private static DawLogger logger = DawLogger.getLogger(ParallelJob.class);

    private JobProvider jobProvider = null; //负责提供执行的程序，Runable 的实例
    private int laborNumber = 5;   //工作线程数

    private final LinkedList<JobWorker> laborPool = new LinkedList<>();  //线程壳
    private boolean running = false;
    private static final Object locker = new Object();

    public void setJobProvider(JobProvider provider) {
        this.jobProvider = provider;
    }

    static int shellID = 0;
    private int timeInterval = 0;

    /**
     * 设定线程数
     *
     * @param laborNumber
     */
    public void setLaborNumber(int laborNumber) {
        this.laborNumber = laborNumber;
        laborPool.clear();
        for (int i = 0; i < laborNumber; i++) {
            laborPool.add(new JobWorker());
        }
    }

    /**
     * 阻塞到所有任务完成
     *
     * @return
     */
    public boolean runAllJobs() {
        Runnable r = null;
        synchronized (locker) {
            if (running) return false; //不允许重复调用
            running = true;
        }

        JobWorker w = null;
        try {
            for (; ; ) {//获得任务实例
//                App.logit("for (; ; ).... ");
                synchronized (laborPool) {
                    while (laborPool.size() == 0) { //请求线程
                        try {
//                            App.logit("synchronized (laborPool) ");
//                            App.logit("Shell manager waiting for a laborPool");
                            laborPool.wait(1000); //等待其它任务完成返回线程
                        } catch (InterruptedException e) {
                            logger.error(e);
                        }
                    }
//                    App.logit("laborPool.removeFirst()");
                    w = laborPool.removeFirst();
                }
//                App.logit("Shell " + w.ID + " retrieaved.");

//                App.logit("Shell " + w.ID + " try to ge a job...");
                if (w.ID == 8) {
                    int a = 0;
                }
                r = jobProvider.getJob();
                if (r == null) { //没有剩余的任务了
                    laborPool.addLast(w);
//                            laborNumber = 0;
//                    App.logit("Shell " + w.ID + " got null job");
                    break;
                }
//                App.logit("Shell " + w.ID + " add job " + r);
                w.newTask(r);  //在新线程里运行实例
            }
            while (laborPool.size() < laborNumber) { //等待所有任务结束
                try {
                    App.logit("Shell manager waiting on finish");
                    synchronized (laborPool) {
                        laborPool.wait(1000);
                    }
                } catch (InterruptedException e) {
                    logger.error(e);
                }
            }
        } finally {
            jobProvider.clare();//清空任务池
            running = false;
            App.logit("Shell manager All Job done ");
        }

        return true;
    }


    private class JobWorker implements Runnable {
        Runnable job = null;
        int ID = 0;

        JobWorker() {
            ID = ++shellID;
        }

        void newTask(Runnable job) {
            this.job = job;
            try {
                if (AppManager.isStopProcess()) { //系统停止
                    jobDon();
                    return;
                }
                AppManager.executeInBack(this); //获取系统线程
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }

        void jobDon() {
//            App.logit("Shell " + ID + " job Done");
            jobProvider.oneJobDone(job);
            job = null;
            //System.out.println("pull back job laborPool");
            synchronized (laborPool) {
                laborPool.addLast(this);
                laborPool.notify();
                //System.out.println("job done");
//                App.logit("Shell " + ID + " put back");
            }
        }

        @Override
        public void run() {
//            App.logit("Shell " + ID + " run ...");
            try {
                job.run();//运行实例
            } catch (Exception e) {
                logger.error(e);
            }
            jobDon(); //返回线程壳
            //System.out.println("return a job");
        }
    }
}
