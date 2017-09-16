package szhzz.Netty.Cluster.ExchangeDataType;


import java.util.ArrayList;

/**
 * Created by Administrator on 2015/7/6.
 */
public class OrderStateWrap {
    NettyExchangeData data = null;
    Integer row = null;


    public static NettyExchangeData getOrderState(ArrayList<String> orderID, ArrayList<String> userSusband, ArrayList<String> activeStates) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
//        eData.setFunID(ClusterProtocal.FUNCTION.OrderState.ordinal()); //ClusterProtocal.FUNCTION.UserData.ordinal()

        eData.setMessage("ORDER_STATES");
        eData.setNettyType(ClusterProtocal.FUNCTION.OrderState);

        eData.appendRow();

        int len = Math.min(orderID.size(), userSusband.size());
        len = Math.min(len, activeStates.size());
        for (int i = 0; i < len; i++) {
            eData.appendRow();
            eData.addData(orderID.get(i));
            eData.addData(userSusband.get(i));
            eData.addData(activeStates.get(i));
        }
        return eData;
    }

    public static NettyExchangeData getOrderState(String orderID, String userSusband, String activeStates) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
//        eData.setFunID(ClusterProtocal.FUNCTION.OrderState.ordinal()); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage("ORDER_STATES");
        eData.setNettyType(ClusterProtocal.FUNCTION.OrderState);

        eData.appendRow();
        eData.appendRow();
        eData.addData(orderID);
        eData.addData(userSusband);
        eData.addData(activeStates);
        return eData;
    }

    public boolean next() {
        if (data == null) return false;
        if (data.getDataRowCount() <= 0) return false;
        if (row == null) {
            row = 0;
            return true;
        }
        row++;
        return row < data.getDataRowCount();
    }

    public OrderStateWrap(NettyExchangeData data) {
        this.data = data;
    }


    public String getOrderID() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 0);
    }

    public String getUserSusband() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 1);
    }

    public String getActiveStates() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 2);
    }
}

