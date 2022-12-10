package szhzz.Netty.Cluster;

import szhzz.App.AppManager;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Cluster.ExchangeDataType.StationPropertyWrap;
import szhzz.Utils.DawLogger;


/**
 * Created with IntelliJ IDEA.
 * User: HuangFang
 * Date: 13-11-10
 * Time: &#x4e0b;&#x5348;12:49
 * To change this template use File | Settings | File Templates.
 */
public class NettyRequystor {
    private static DawLogger logger = DawLogger.getLogger(NettyRequystor.class);
    private static int SUBSCRIBE_ID = 0;
    protected final Object lock_ = new Object();
    protected long blockWaitTime = 1000;
    protected ObjBufferedIO dataBuffer = null;
    protected DataConsumer reader = null;
    protected boolean subscribeModel = false;
    private long requestID = 0L; //new SynchronizedLong(0) 确保当前仅有1个未完成的查询
    private long funID = 0L;
    private BLOCK onBlock = BLOCK.ON_BLOCK_DROP_PRE;
//    private NettyExchangeData queryData = null;


    private String stationName = null;


    public NettyRequystor(String stationName) {//, String args[]

//        if (args.length > 0)
//            ipAddress = args[0];
//        if (args.length > 1)
        this.stationName = stationName;

    }

    public boolean Query() throws Exception {
        synchronized (lock_) {
            if (getRequestID() != 0) {
                if (blockWaitTime > 0) {
                    lock_.wait(blockWaitTime);
                }

                switch (getOnBlock()) {
                    case ON_BLOCK_DROP_PRE:
                        if (getRequestID() != 0) {
                            deQueue();
                        }
                        break;
                    case ON_BLOCK_DENY:
                        break; //throw Exception
                }
                if (getRequestID() != 0) {
                    throw new Exception("重叠的查询错误");
                }
            }
        }
        if (isConnected() ) {
            if(Cluster.isRouterDebug()){
                logger.info("标志 -1 正常连接" + AppManager.getHostName()+ "->" +
                        this.getStationName() + "@" + this.getIpAddress());
            }
            return ClusterClients.getInstance().query(this);
        }else if ( hasByPassChannel()) {
            return ClusterClients.getInstance().query(this);
        }
        return false;
    }


    public String getIpAddress() {
        return ClusterClients.getInstance().getAddress(stationName);
    }

    public NettyExchangeData getQueryData() {
        NettyExchangeData queryData = StationPropertyWrap.getStationLevelQuery();
        queryData.setIpAddress(getIpAddress());
        return queryData;
    }


//    public void setQueryData(NettyExchangeData queryData) {
//        this.queryData = queryData;
//    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    //    public NettyExchangeData getCallbackData() {
    //        return callbackData;
    //    }
    //
    //    public void setCallbackData(NettyExchangeData callbackData) {
    //        this.callbackData = callbackData;
    //    }

    public boolean isSubscribeModel() {
        return subscribeModel;
    }

    public void setSubscribeModel(boolean subscribeModel) {
        this.subscribeModel = subscribeModel;
    }

    public boolean isConnected() {
        return ClusterClients.getInstance().isConnect(stationName) ;
    }

    public boolean hasByPassChannel() {
        //ServerHandler.hasByPassChannal()
        if(ClusterClients.getInstance().hasByPassChannel(stationName) ){
            logger.info("标志 0 可旁路 ->" + stationName);
            return true;
        }else{
            logger.info("标志 0 不可旁路 ->" + stationName );
            return false;
        }

    }
//    public String getIpAddress() {
//        return ipAddress;
//    }

    public void setReader(DataConsumer reader) {
        dataBuffer = null;
        this.reader = reader;
    }

    public void deQueue() {
        ClusterClients.abandon(requestID);
        requestID = 0;
    }

    /**
     * 如果设定Buffer， 数据将先推给Buffer， 然后由Buffer 推给 reader
     * 否则，直接推给 reader
     *
     * @param reader
     * @param bufferSize
     */
    public void setReader(DataConsumer reader, int bufferSize) {
        if (bufferSize <= 0) {
            setReader(reader);
            return;
        }
        dataBuffer = new ObjBufferedIO();
        try {
            this.reader = reader;
            dataBuffer.setReader(reader, bufferSize);
        } catch (InterruptedException e) {
            logger.error(e);

            dataBuffer.close();
            dataBuffer = null;
        }
    }

    public boolean Cancel() throws Exception {
        return false;
    }

    public void setOnBlock(BLOCK onBlock, long waitTime) {
        this.onBlock = onBlock;
        blockWaitTime = waitTime;
    }

    public BLOCK getOnBlock() {
        return onBlock;
    }

    /**
     * @return
     */
    public long getRequestID() {
        return requestID;
    }

    public void setRequestID(long id) {
        requestID = id;
        if (id == 0L) {
            logger.error(new Exception("requestID be set to 0"));
        }
    }

    public long getFunID() {
        return funID;
    }

    public void setFunID(long funID) {
        this.funID = funID;
    }

    /**
     *
     */
    public void clearRequestID() {
        requestID = 0L;
    }

    public void clear() {
        if (dataBuffer != null) {
            dataBuffer.close();
            dataBuffer = null;
        }
    }

    /**
     * 为了不阻塞服务器的回调，建立缓存接收返回的数据
     * 如果 JNI 已经缓存，此处不需再加缓存
     *
     * @param data
     */
    public void push(Object data) {
//        long lastID = requestID;
        try {
            if (data instanceof NettyExchangeData) {
//TODO ?          ((NettyExchangeData) data).setIpAddress(ipAddress);
//                ((NettyExchangeData) data).setMack(mack);

            }

            if (dataBuffer != null) {
                dataBuffer.push(data);
            } else {
                if (reader == null) {
                    logger.error(new Exception("Data Reader not defined!"));
                } else {
                    reader.in(data);  //链式查询可能会导致 requestID 改变
                }
            }
        } finally {
        }
    }

    /**
     * 此函数未能实现缓存
     *
     * @param dataID
     * @param data
     */
    public void push(long dataID, Object data) {
        try {
            if (reader == null) {
                logger.error(new Exception("Data Reader not defined!"));
            } else {
                reader.in(dataID, data);
            }
        } finally {
            if (!subscribeModel) {
                deQueue();
            }
        }
    }


    public enum BLOCK {
        ON_BLOCK_DENY,
        ON_BLOCK_DROP_PRE,
    }
}

