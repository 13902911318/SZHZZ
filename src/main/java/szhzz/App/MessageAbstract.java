package szhzz.App;


import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Utils.DawLogger;

import java.util.Collections;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-9-10
 * Time: 11:25:33
 * To change this template use File | Settings | File Templates.
 * <p/>
 * 2014-01-07
 * 修改，增加信息队列，使信息发出为单个线程，避免系统陷入不可预期的混乱
 */
public abstract class MessageAbstract implements Comparable {
    //    private static BoundedBuffer queue = new BoundedBuffer();

    static MessageAbstract messager = null;
    static ObjBufferedIO dataBuffer = null;
    private static DawLogger logger = DawLogger.getLogger(MessageAbstract.class);
    private static WatchMessage messageWatch = null;
    private static Vector<MessageAbstract> messageObject = new Vector<MessageAbstract>();
    private static final Vector<MessageAbstract> newObject = new Vector<MessageAbstract>();
    private Integer weight = 10;  // 0 - 10


    public MessageAbstract() {
        synchronized (newObject) {
            newObject.add(this);
        }
        postConstruct();
    }

    public void postConstruct() {

    }

    private static synchronized void sendMessage_(MessageCode messageID, Object caller, Object message) {
        if (messageWatch == null) {
            messageWatch = new WatchMessage();
            dataBuffer = new ObjBufferedIO();
            try {
                dataBuffer.setReader(messageWatch, 10240);           //500
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
        dataBuffer.push(new MessageObj(messageID, caller, message));
    }

    public static MessageAbstract getInstance() {
        if (messager == null) messager = new Messager_();
        return messager;
    }

    /**
     * @param messageID
     * @param caller
     * @param message
     * @return true 将吃掉message，不再继续发布此消息
     */
    public abstract boolean acceptMessage(MessageCode messageID, Object caller, Object message);


    public void dropMessage() {
        synchronized (newObject) {
            messageObject.remove(this);
            newObject.remove(this);
        }
    }

    public synchronized void sendMessage(final MessageCode messageID, final Object message) {
        sendMessage_(messageID, MessageAbstract.this, message);
    }

    @Override
    public int compareTo(Object o) {
        MessageAbstract compTo = (MessageAbstract) o;
        return weight.compareTo(compTo.weight);
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }


    static class Messager_ extends MessageAbstract {
        @Override
        public boolean acceptMessage(MessageCode messageID, Object caller, Object message) {
            // do no thing
            return false;
        }
    }

    static class WatchMessage implements DataConsumer {
        public long in(long dataID, Object obj) {
            return 0;
        }

        @Override
        public long in(Object obj) {
            MessageObj msg = (MessageObj) obj;
//            long maxTime = 0;
//            String timeConsumer = "";
            if (msg != null) {
                if (newObject.size() > 0) {
                    logger.info("message user added " + newObject.size());
                    synchronized (newObject) {
                        messageObject.addAll(newObject);
                        newObject.clear();
                        Collections.sort(messageObject);
                    }
                }

                for (int i = 0; i < messageObject.size(); i++) {
                    try {
                        MessageAbstract o = messageObject.get(i);
                        if (o != null && o != msg.caller) {
                            long startT = System.nanoTime();
                            if (o.acceptMessage(msg.messageID, msg.caller, msg.message)) {
//                                    long t = System.nanoTime() - startT;
//                                    if(t > maxTime){
//                                        maxTime = t;
//                                        timeConsumer = o.getClass().getName();
//                                    }
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                        //prevent any Error may break this thread
                    }
                }
            }

//                if(maxTime > 0) {
//                    //找出最耗时的事件消费者
//                    logger.info("Max time consumer(" + maxTime + ") in message is " + timeConsumer);
//                }

            return 0;
        }
    }

    private static class MessageObj {
        MessageCode messageID;
        Object caller;
        Object message;

        MessageObj(MessageCode messageID, Object caller, Object message) {
            this.messageID = messageID;
            this.caller = caller;
            this.message = message;
        }
    }
}
