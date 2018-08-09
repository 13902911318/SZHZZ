package szhzz.Utils;

/**
 * Created by Administrator on 2018/7/28.
 *
 * 用于测试程序段占用时间的工具
 *
 *
 */
public class SpeedTest {
    private static DawLogger logger = DawLogger.getLogger(SpeedTest.class);
    private long accCounter = 0;
    private long countTo = 1000;
    private String ID;
    private int trrigger = 0;

    private SpeedTest() {
    }

    //    private long interval = 10000;
    private long startTimeStamp = 0;
    private long accTime = 0;

    private long summaryTime = 0;
    private long summaryCount = 0;
    private Double speed = 0d;

    /**
     * @param ID      标示
     * @param countTo 计数单位，大于此计数单位时，计算并显示速度
     */
    public SpeedTest(String ID, long countTo) {
        this.countTo = countTo;
        this.ID = ID;
//        this.interval = interval;
    }

    /**
     * 不适用于多线程调用
     */
    public void straTimer() {
        if (trrigger != 0) {
            logger.info(ID + " 重复调用 straTimer()");
        }
        trrigger = 1;
        startTimeStamp = System.nanoTime();
        accCounter++;
    }

    /**
     *
     * 不适用于多线程调用
     */
    public void endTimer() {
        if (trrigger != 1) {
            logger.info(ID + " 重复调用 endTimer()");
        }
        trrigger = 0;
        accTime += (System.nanoTime() - startTimeStamp);
    }

    public long summery() {
        if (accCounter > 0) {
            speed = (double) accTime / accCounter;
            logger.info(ID + " Time consumee (" + accTime + "/" + accCounter + ")= " + speed + " nano");
        }
        summaryTime = accTime;
        summaryCount = accCounter;

        accTime = 0;
        accCounter = 0;
        return summaryTime;
    }

//    public void count() {
//        if (interval > 0) {
//            ++accCounter;
//            if ((System.currentTimeMillis() - timer) > interval) {
//                logIt(speed(accCounter));
//            }
//        } else if (++accCounter >= countTo) {
//            logIt(speed(accCounter));
//        }
//    }
//
//    public void count(int count) {
//        accCounter += count;
//        if (interval > 0) {
//            if ((System.currentTimeMillis() - timer) > interval) {
//                logIt(speed(accCounter));
//            }
//        } else if (accCounter >= countTo) {
//            logIt(speed(accCounter));
//        }
//    }

//    public double speed(long count) {
//        long t = System.currentTimeMillis();
//        long sectionCount = accCounter;
//        accCounter = 0;
//        long sectionTime = t - timer;
//        timer = t;
//        return 1000 * sectionCount / sectionTime;
//    }


//    public void logIt(double speed) {
//        AppManager.logit(ID + " Speed test = " + FT.format00(speed) + " c/s");
//    }

    public long getSummaryTime() {
        return summaryTime;
    }

    public long getSummaryCount() {
        return summaryCount;
    }

    public Double getSpeed() {
        return speed;
    }
}
