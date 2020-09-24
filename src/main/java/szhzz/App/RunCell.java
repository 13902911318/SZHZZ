package szhzz.App;

import szhzz.Calendar.MyDate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Vector;

/**
 * Created by HuangFang on 2015/3/8.
 * 20:07
 */
public class RunCell implements Runnable {
    public static final HashSet<String> past = new HashSet<>();
    //private static final Set<RunCell> pooledTasks = Collections.synchronizedSet(new HashSet<RunCell>());
    private static final Vector<RunCell> pooledTasks = new Vector<>();
    private static int managedNo = 0;
    private static int sysTaskNo = 0;


    private String startTime = "";
    private String threadName = "";
    private final boolean managed;
    private final String stackTrace;
    private Runnable task;


    RunCell(Runnable task, boolean managed) {
        String stackTrace1;
        this.managed = managed;
        this.task = task;

        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        new Exception().printStackTrace(printWriter);
        stackTrace1 = result.toString();
        stackTrace = stackTrace1.replace("java.lang.Exception", "").replace("\t","");
    }

    public static Vector<RunCell> getPooledTasks() {
        return pooledTasks;
    }

    public static int getManagedNo() {
        return managedNo;
    }

    public static int getSysTaskNo() {
        return sysTaskNo;
    }

    @Override
    public void run() {
        synchronized (pooledTasks) {
            pooledTasks.add(this);
            startTime = MyDate.getToday().getTime();
            threadName = Thread.currentThread().getName();
            if (managed) {
                managedNo++;
            } else {
                sysTaskNo++;
            }
        }
        try {
            task.run();
        } finally {
            synchronized (pooledTasks) {
                pooledTasks.remove(this);
                past.add(task.toString());
                if (managed) {
                    managedNo--;
                } else {
                    sysTaskNo--;
                }
            }
            task = null;
        }
    }

    public String toString() {
        return startTime + " " + task.toString();
    }

    public Runnable getTask() {
        return task;
    }

    public String printStackTrace() {
//        Writer result = new StringWriter();
//        PrintWriter printWriter = new PrintWriter(result);
//        new Exception().printStackTrace(printWriter);
//        return result.toString();
        return stackTrace;
    }

    public String getStartTime() {
        return startTime;
    }

    public boolean isManaged() {
        return managed;
    }

    public String getThreadName() {
        return threadName;
    }
}
