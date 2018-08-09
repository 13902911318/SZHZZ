package szhzz.DataBuffer;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import szhzz.App.AppManager;
import szhzz.App.BeQuit;
import szhzz.Utils.DawLogger;
import szhzz.Utils.SpeedGroup;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-1-29
 * Time: 下午3:52
 * To change this template use File | Settings | File Templates.
 */
public class ObjBufferedIO implements Runnable {
    public static final int INITIAL_DEFAULT_CAPACITY = 10;
    private static DawLogger logger = DawLogger.getLogger(ObjBufferedIO.class);
    long totalCount = 0;
    long lostCount = 0;
    long startMmSecond = 0;
    boolean isBuffered = false;
    DataConsumer dataReader = null;
    long blockTimeout = 1000;
    private BoundedBuffer queue = null;
    private int bufferSize = INITIAL_DEFAULT_CAPACITY;

    private int waringSize = 800;
    private boolean autoDropIfFull = false;
    private String speedTestID = "";
    private boolean logError = false;
    private String bufferName = null;

    public void close() {
        isBuffered = false;
        while (queue.peek() != null) {
            try {
                queue.poll(100);
            } catch (InterruptedException e) {

            }
        }
    }

    public void close(int waitInSecond) {
        isBuffered = false;
        if (queue.peek() != null) {
            try {
                TimeUnit.SECONDS.sleep(waitInSecond);
            } catch (InterruptedException e) {

            }
        }
        close();
    }

    public boolean isClosed() {
        return isBuffered;
    }

    BeQuit a = new BeQuit() {
        @Override
        public boolean Quit() {
            close();
            return true;
        }
    };

    /**
     * 1.缓存数据
     * 2.线程隔离，使数据交换双方产生弱耦合，
     * 当buffer值==1时，典型的线程隔离设置
     * 3 当buffer值==0时, 将没有线程隔离的功能，数据交换双方是紧密耦合
     *
     * @param dataReader
     * @param bufferSize 表示 ObjIO 的个数，不是数据的实际长度。
     * @throws InterruptedException
     */
    public void setReader(DataConsumer dataReader, int bufferSize) throws InterruptedException {
        this.dataReader = dataReader;

        if (bufferSize < 0) bufferSize = 0;
        this.bufferSize = bufferSize;

        if (bufferSize > 0) {
            queue = new BoundedBuffer(bufferSize);
            isBuffered = true;
            waringSize = (int) (0.95 * bufferSize);
            if (waringSize < 1) waringSize = 1;

            AppManager.executeInBack(this, false);
        } else {
            close();
        }
        logger.info(dataReader.getClass().getName() + " set buffer size = " + bufferSize);
    }


    public void setReader(DataConsumer dataReader) throws InterruptedException {
        setReader(dataReader, INITIAL_DEFAULT_CAPACITY);
    }

    public void preClose() {
    }

    private void write(Object data) {
        if (dataReader != null) {
            dataReader.in(data);
        }
    }


    public boolean push(Object data) {
        boolean success = queue != null;
        if (isBuffered && queue != null) {
            try {
                if (!queue.offer(data, blockTimeout)) {
                    success = false;
                    lostCount++;
                    if (dataReader != null && logError) {
                        logger.info(data.toString());
                        logger.error(new Exception(" 缓存(" + queue.size() + "/" + bufferSize + ")溢出(1) = " + lostCount + " rows 记录丢失\n" +
                                "请加大缓存或超时. reader is " + dataReader.getClass()));
                    }
                }
            } catch (InterruptedException e) {
                logger.error(e);
                success = false;
            }
        } else {
            write(data);
        }
        return success;
    }

    public void run() {
        if (queue == null) {
            queue = new BoundedBuffer(bufferSize);
        }
        isBuffered = true;

        startMmSecond = System.currentTimeMillis();
        Thread.currentThread().setName(getBufferName());
        while (isBuffered) {
            try {
                //TODO  TBD
                while (autoDropIfFull && queue.size() > waringSize) { //除非已经设置，否则不会删除老数据
                    queue.take();  //just dequeue
                    if (++totalCount > waringSize) {
                        if (logError) {
                            logger.error(new Exception(dataReader.getClass() + " 缓存溢出(2), 删除 " + totalCount + " rows 记录"));
                            AppManager.logit(dataReader.getClass() + " 缓存溢出 删除 " + totalCount + " rows, 记录");
                        }
                        totalCount = 0;
                    }
                }
                Object data = queue.poll(3000);  //不能用 take , 否则无法退出
                if (data != null) {
                    //TODO debug 之后 mark it
//                    if(data instanceof ExchangeData){
//                        ExchangeData d= (ExchangeData) data;
//                        d.setDbgMsg(dataReader.getClass().getName());
//                        d.setHandledCount();
//                        if(d.getHandledCount() > 1){
//                            AppManager.logit(" 多级缓存("+d.getHandledCount()+") " + d.getDbgMsg());
//                        }
//                    }

                    write(data);
                }
            } catch (Exception e) {
                logger.error(e);
            }
            if (AppManager.isQuitApp()) {
                isBuffered = false;
            }
            if (!isBuffered && queue.size() == 0) {
                break;   //优雅地退出
            }
        }
        preClose();
        dataReader = null;
        bufferName = null;
        if (isBuffered && !AppManager.isQuitApp()) {
            logger.error(new Exception("错误:缓存自动退出!"));
            AppManager.logit("错误:缓存自动退出!");
        }
        isBuffered = false;
    }

    public void setAutoDropIfFull(boolean autoDropIfFull) {
        this.autoDropIfFull = autoDropIfFull;
    }

    public long size() {
        if (queue == null) return 0;
        return queue.size();
    }

    /**
     * @param blockTimeout miniSecond
     */
    public void setBlockTimeout(long blockTimeout) {
        this.blockTimeout = blockTimeout;
    }

    public String toString() {
        if (dataReader == null) {
            return this.getClass().getName() + "->?";
        } else {
            return this.getClass().getName() + "->" + dataReader.toString();
        }
    }

    public void setSpeedTester(SpeedGroup stockSpeedTester, String ID) {
        this.speedTestID = ID;
    }

    public void setLogError(boolean logError) {
        this.logError = logError;
    }

    private String getBufferName() {
        if (bufferName == null && dataReader != null) {
            bufferName = dataReader.getClass().getSimpleName() + Thread.currentThread().getId();
        }
        if (bufferName != null) {
            return bufferName;
        }
        return "BufferIO-" + Thread.currentThread().getId();
    }

    public void setBufferName(String bufferName) {
        if (this.bufferName == null) {
            this.bufferName = bufferName;
        }
    }
}
