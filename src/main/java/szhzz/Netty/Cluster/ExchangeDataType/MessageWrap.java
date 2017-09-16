package szhzz.Netty.Cluster.ExchangeDataType;


import szhzz.App.MessageCode;
import szhzz.Utils.NU;

/**
 * Created by Administrator on 2015/7/6.
 */
public class MessageWrap {
    NettyExchangeData data = null;

    public static NettyExchangeData getMessageObject(MessageCode messageID, String data) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);

        eData.setMessage(messageID.ordinal());
        eData.setNettyType(ClusterProtocal.FUNCTION.SendMessage);

        eData.appendRow();
        eData.appendRow();
        eData.addData(data);        //信息内容
        return eData;
    }

    public MessageWrap(NettyExchangeData data) {
        this.data = data;
    }


    public MessageCode getMessageCode() {
        if (data == null) return null;
        return MessageCode.values()[(NU.parseInt(data.getMessageString(), 0))];
    }

    public String getMessageData() {
        if (data == null) return null;
        return (String) data.getDataValue(0, 0);
    }
}

