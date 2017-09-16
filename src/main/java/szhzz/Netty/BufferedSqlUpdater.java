package szhzz.Netty;

import szhzz.App.AppManager;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Netty.Cluster.Cluster;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Utils.DawLogger;

/**
 * Created by Administrator on 2016/5/2.
 */
public class BufferedSqlUpdater implements DataConsumer {
    private static DawLogger logger = DawLogger.getLogger(BufferedSqlUpdater.class);
    private static AppManager App = AppManager.getApp();
    protected ObjBufferedIO dataBuffer = null;
    private static BufferedSqlUpdater onlyOne = null;

    public static BufferedSqlUpdater getInstance() {
        if (onlyOne == null) {
            onlyOne = new BufferedSqlUpdater();
        }
        return onlyOne;
    }

    private BufferedSqlUpdater() {
        setBufferSize(100);
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            dataBuffer = null;
            return;
        }
        if(dataBuffer != null) return;

        dataBuffer = new ObjBufferedIO();
        try {
            dataBuffer.setReader(this, bufferSize);
        } catch (InterruptedException e) {
            logger.error(e);

            dataBuffer.close();
            dataBuffer = null;
        }
    }
    public void push(Object data) {
        try {
            if (dataBuffer != null) {
                dataBuffer.push(data);
            } else {
                in(data);
            }
        } finally {
        }
    }

    public long size(){
        if(dataBuffer == null) return 0;
        return dataBuffer.size();
    }
    private void sqlUpdate(NettyExchangeData eData) {
        //同一机器上不需SQL更新
        if(Cluster.getCpuID().equals(eData.getCpuID())){
            return ;
        }

//        Database db = DbStack.getDb(this.getClass());
//        try {
//            SqlUpdateWrap sqlWrap = new SqlUpdateWrap(eData);
//            while (sqlWrap.next()) {
//                String update = sqlWrap.getScript();
//                try {
//                    szhzz.App.logit(update, false);
//                    db.executeUpdate(update);
//                } catch (DBException e) {
//                    logger.error(update);
//                }
//            }
//        } finally {
//            DbStack.closeDB(db);
//        }
    }

    private void sqlUpdate(String update) {
//        Database db = DbStack.getDb(this.getClass());
//        try {
//            szhzz.App.logit(update);
//            db.executeUpdate(update);
//        } catch (DBException e) {
//            logger.error(update);
//        } finally {
//            DbStack.closeDB(db);
//        }
    }

    @Override
    public long in(Object obj) {
        if (obj instanceof NettyExchangeData) {
            sqlUpdate((NettyExchangeData) obj);
        }else{
            sqlUpdate(obj.toString());
        }
        return 0;
    }

    @Override
    public long in(long dataID, Object obj) {
        return 0;
    }
}
