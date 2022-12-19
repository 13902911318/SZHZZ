package szhzz.Utils;

import EDU.oswego.cs.dl.util.concurrent.BoundedLinkedQueue;


/**
 * Created by IntelliJ IDEA.
 * User: HuangFang
 * Date: 2007-11-30
 * Time: 19:58:48
 * To change this template use File | Settings | File Templates.
 */
public class PooledTask {
    private static DawLogger logger = DawLogger.getLogger(PooledTask.class);
    private static int ThreadsCount = 0; // Current running thread number
    final Object offerGuard_ = new Object();
    private final BoundedLinkedQueue idlePool; // 实例缓冲池 we can have multy thread for
    //private static int taskID = 0;
    //private int runerID = 0;
    private int maxTasks = 250; // max thread, adjustable
    private int minTasks = 5;  // min thread
    private int HistoryMax = 0;  // History Max Tasks, recorder
    private int TasksInHandle = 0; // Current useing thread number
    private int OvertakenCount = 0; //
    private long pollOutTimeout = 10 * 60 * 1000;  // 10 minits
    private long OfferTimeout = 1000;
    private long keepInstance = 60 * 1000;
    private boolean tearingdown = false;

    /**
     * Create a new pool with all default settings
     */
    public PooledTask() {
        idlePool = new BoundedLinkedQueue(5);
    }

    public static int getTaskRunning() {
        return PooledTask.ThreadsCount;
    }

    // in task instance back to task pool when it finifshed run
    private boolean offer(PooledTask.taskShell task, long msecs) {
        PooledTask.logger.debug("Task offer back: TaskInPool=" + idlePool.size() +
                " minTasks=" + minTasks + " TaskRunning=" + TasksInHandle);

        if (task.job != null) {
            task.job = null;
        }
        try {
            if (idlePool.offer(task, msecs)) {
                PooledTask.logger.debug("Task offer back success");
                if (idlePool.size() == HistoryMax) {
                    PooledTask.logger.debug("stationEvent.allTaskSleeping()....");
                    //stationEvent.allTaskSleeping();       // trigger persystLoader
                    //startIdleCountdown();
                }
                return true;
            } else {
                // 在此设计中, 不允许这种情况出现! 发出警告
                PooledTask.logger.error(new Exception("idlePool is full " + task));
                // then command will be discated
                //DiscartCommand(task.getCommand());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            try {
                idlePool.offer(task, msecs);
            } catch (InterruptedException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } finally {
            synchronized (offerGuard_) {
                offerGuard_.notify();
            }
        }
        return false;
    }

    public void waittingFinish() {
        synchronized (offerGuard_) {
            long waitTime = 10000;
            for (; ; ) {
                try {
                    offerGuard_.wait(waitTime);
                    //if (idlePool.size() == HistoryMax) {
                    if (ThreadsCount == 0) {
                        break;
                    }
                    // System.out.println("Waitting On Finish ...");
                } catch (InterruptedException ex) {
                    // System.out.println("Interrupted ");
                }
            }
        }
    }

    /**
     * 从实例池请求一个闲置的实例，
     * 如果失败，产生一个新的实例 Runnable command，包装到任务壳（taskShell）里
     * 然后把任务壳（taskShell）放入 taskPool 暂存，等待使用。
     * <p/>
     * 该函数返回实例 command，让该函数的调用者（重新）初始化实例
     * 该函数的调用者将调用本 Class 的 Execute，把 command 重新
     * 包装到任务壳（taskShell）里然后获取线程并运行。
     * 通过这个函数，实例和任务壳将分离。
     * 把实例和任务壳分离的目的是避免在 Execute 时查询配对的麻烦
     * synchronized ？
     */
    private Object poll(long msecs) throws InterruptedException {
        Object task = null;
        PooledTask.logger.debug("maxTasks=" + maxTasks + " minTasks=" + minTasks + " TaskRunning=" + TasksInHandle +
                " TaskInPool=" + idlePool.size() + " poll timer=" + msecs);
        task = idlePool.poll(msecs);
        return task;
    }


    public PooledTask.taskShell nextTaskShell() throws InterruptedException {
        Object task = null;
        while (task == null) {
            if (idlePool.peek() != null) task = poll(getPollOutTimeout());
            if (task == null) task = newTaskShell();
            if (task == null) {
                task = poll(getPollOutTimeout());
            }
        }
        return (PooledTask.taskShell) task;
    }

    private void teardown() {
        tearingdown = true;       // this may be set false by handleover
        PooledTask.taskShell task = null;
        while (idlePool.size() > minTasks && tearingdown) {
            if (idlePool.peek() == null) break;
            try {
                task = (PooledTask.taskShell) poll(getPollOutTimeout());
            } catch (InterruptedException e) {
                break;
            }
            //DiscartCommand(task.getCommand());
        }
    }

    /**
     * 在 TasksInHandle < maxTasks 时产生一个新实例
     * 成功会通知 stationEvent PostConstructor(command) 进行相应处理构建处理
     * 失败会通知 stationEvent.RunInExeption(command, e) 进行相应处理
     * <p/>
     * 在 TasksInHandle >= maxTasks 返回 null
     */
    private synchronized PooledTask.taskShell newTaskShell() {
        //logger.debug("Enter newTask for " + calssName);
        PooledTask.taskShell task = null;
        try {
            if (TasksInHandle < idlePool.capacity()) {
                //logger.debug(commandFactory.getClass().getName() + " Befor Create, Taskinhandle=" + TasksInHandle);
                try {
                    task = new PooledTask.taskShell();
                    PooledTask.logger.debug("commandFactory.getInstance return:" + task);
                } catch (Exception e) {
                    PooledTask.logger.error(e);
                }
            }
        } finally {
            if (task != null) {
                TasksInHandle++;
                PooledTask.logger.debug("Post Create Taskinhandle=" + (TasksInHandle + 1));
            }
            if (TasksInHandle > HistoryMax) HistoryMax = TasksInHandle;
            if (TasksInHandle > maxTasks)
                PooledTask.logger.error(new Exception(" TasksInHandle > maxTasks"));
        }
        return task;
    }

    /**
     * 用于生成task实例的厂类名
     * 每一个新的任务线程启动前，需要建立任务实例
     * 我们的设计要求任务实例均由一个指定的工厂
     * 来完成建立新实例的工作，这不但是通用性和
     * 可扩充性的要求，同时也方便实例的初始化
     *
     * @param commandFactory
     */
//    public void setCommandFactory(DawObjectFactory commandFactory) throws IllegalArgumentException {

//        if (commandFactory == null) throw new IllegalArgumentException("commandFactory can not be null");

//        logger.debug("Set commandFactory=" + commandFactory);

//        this.commandFactory = commandFactory;

//    }

    /**
     * 设置最大和最小实例缓存数目
     * 最小实例数也是实例缓存池的大小：空闲时，总是有最小实例
     * 数量的实例保存在实例池，以方便迅速调用
     * 繁忙时，允许最大实例数量的实实例线程运行
     * 但超过最小实例数的部分，实例完成后不会被
     * 放入缓存池
     *
     * @param maxTasks 最大实例数
     * @param minTasks 最小实例数
     */
    public void setTaskNumber(int maxTasks, int minTasks) {
        minTasks = minTasks < 1 ? 1 : minTasks;
        this.minTasks = minTasks;

        this.maxTasks = maxTasks > minTasks ? maxTasks : minTasks;
        if (this.maxTasks != idlePool.capacity())
            idlePool.setCapacity(this.maxTasks);
        PooledTask.logger.debug("Set maxTasks=" + this.maxTasks + " minTasks=" + this.minTasks);
    }

// Help functions

//////////////////////////////////////////////

    public int getMaxTask() {
        return maxTasks;
    }

    public int getMinTask() {
        return minTasks;
    }

    public int getTaskCount() {
        return TasksInHandle;
    }


    public int getAliveCommands() {
        return idlePool.size();
    }

    public int getHistoryMax() {
        return HistoryMax;  //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * When a task offerback is blocked
     * 运行完毕的实例无法放回实例池时
     * (当实例内的实例数达到最小实例数时)
     * 我们可能要抛弃该实例，之前我们需要针对
     * command 进行某些清理工作
     * 回收实例代码 ID
     */
//    private void DiscartCommand(StockWindow command) {
//        if (command == null) return;
//
//        //logger.debug("command ID=" + command.getTableName());
//
//        TasksInHandle--;
//        if (TasksInHandle < 0)
//            PooledTask.logger.error(" TasksInHandle < 0");
//    }
    public long KeepInstance() {
        return keepInstance;
    }

    public void setKeepInstance(long timeup) {
        this.keepInstance = timeup;
    }

    public long getPollOutTimeout() {
        return pollOutTimeout;
    }

    public void setPollOutTimeout(long pollOutTimeout) {
        this.pollOutTimeout = pollOutTimeout;
    }

    public int getOvertakenCount() {
        return OvertakenCount;
    }

    /**
     * taskShell 用于包装实例,以便实例运行结束后把自己以及包装的实例
     * 返回实例池
     * taskShell 将被线程池的轻量对象(task)包装
     * <p/>
     * taskShell will be discarted after run
     * so, any class varible also will be discarted
     */
    public class taskShell implements Runnable {  // extends ShutdownContrlor ?!!
        Runnable job = null;

        taskShell() {
        }

        protected Runnable getCommand() {
            return job;
        }

        public void setCommand(Runnable job) {
            this.job = job;
        }

        public void run() {
            PooledTask.logger.debug("Enter taskShell run(" + job.getClass() + ")");


            try {
                PooledTask.ThreadsCount++;
                if (job == null) {
                    return; // Note!, goto finally with handOver = false;
                }
                Runnable job_ = job;
                job = null;
                job_.run();
            } finally {
                PooledTask.ThreadsCount--;
                //this.postRun(handOver, envelop);
                //command = null;
//                Thread.yield();
                this.offerBackORClean();
            }
        }

//        private void postRun(boolean handOver, Object envelop) {
//            try {
//                if (!handOver) {
//                    debugInfor += MiscDate.todaysDate() + "!! Error !if (!handOver) \r\n";
//                    HandoverFalse(envelop);
//                }
//
//                // Call Parent's method
//                PostRun(command, envelop);
//            } catch (Exception igno) {
//                debugInfor += MiscDate.todaysDate() + " Error !if (!handOver)  catch (Exception igno) \r\n";
//            }
//            debugInfor += MiscDate.todaysDate() + " 11:  postRun()\r\n";
//        }

        private void offerBackORClean() {
            offer(this, OfferTimeout);
        }
    }
}

