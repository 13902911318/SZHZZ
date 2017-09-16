package szhzz.Timer;

/**
 * Created by IntelliJ IDEA.
 * User: vmuser
 * Date: 2007-11-21
 * Time: 14:13:36
 * To change this template use File | Settings | File Templates.
 */

/**
 * 本线程设置了一个超时时间
 * 该线程开始运行后，经过指定超时时间，
 * 该线呈会抛出一个未检查异常通知调用该线呈的程序超时
 * 在超时结束前可以调用该类的cancel方法取消计时
 * <p/>
 * TimeoutThread t = new TimeoutThread(5000,new TimeoutException("超时"));
 * try{
 * t.start();
 * .....要检测超时的程序段....
 * t.cancel();
 * }catch (TimeoutException e)
 * {
 * ...对超时的处理...
 * }
 *
 * @author solonote
 */

//TimeoutThread t = new TimeoutThread(5000,new TimeoutException("超时"));
// try{
//  t.addFrom10JQKA();
//  .....要检测超时的程序段....
//  t.cancel();
// }catch (TimeoutException e)
// {
//  ...对超时的处理...
// }


public class TimeoutThread extends Thread {

    /**
     * 计时器超时时间
     */
    private long timeout;

    /**
     * 计时是否被取消
     */
    private boolean isCanceled = false;

    /**
     * 当计时器超时时抛出的异常
     */
    private TimeoutException timeoutException;

    /**
     * 构造器
     *
     * @param timeout 指定超时的时间
     */
    public TimeoutThread(long timeout, TimeoutException timeoutErr) {
        super();
        this.timeout = timeout;
        this.timeoutException = timeoutErr;
        //设置本线程为守护线程
        this.setDaemon(true);
    }

    /**
     * 取消计时
     */
    public void cancel() {
        isCanceled = true;
        notify();
    }

    /**
     * 启动超时计时器
     */
    public void run() {
        try {
            // Thread.sleep(timeout)
            this.wait(timeout);
            if (!isCanceled)
                throw timeoutException;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


