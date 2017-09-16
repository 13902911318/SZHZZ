package szhzz.Netty.Cluster.ExchangeDataType;


/**
 * Created by Administrator on 2015/7/6.
 */
public class SqlUpdateWrap {
    NettyExchangeData data = null;
    Integer row = null;

    public SqlUpdateWrap(NettyExchangeData data) {
        this.data = data;
    }

    public static NettyExchangeData getSqlUpdate(Iterable<String> data, String key){
        NettyExchangeData eData = getSqlUpdate(data);
        eData.setExtData(key, 1);
        return eData;
    }


    public static NettyExchangeData getSqlUpdate(Iterable<String> data) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
//        eData.setFunID(ClusterProtocal.FUNCTION.SqlUpdate.ordinal()); //ClusterProtocal.FUNCTION.UserData.ordinal()
        eData.setMessage("SQL_UPDATE");
        eData.setNettyType(ClusterProtocal.FUNCTION.SqlUpdate);


        eData.appendRow();

        for (String s : data) {
            eData.appendRow();
            eData.addData(s);
        }
        return eData;
    }

    public static NettyExchangeData getSqlUpdate(String data, String key) {
        NettyExchangeData eData = getSqlUpdate(data);
        eData.setExtData(key, 1);
        return eData;
    }
    public static NettyExchangeData getSqlUpdate(String data) {
        NettyExchangeData eData = new NettyExchangeData();

        eData.setErrorCode(0);
        eData.setEvenType(ClusterProtocal.EVENT.Broadcast.ordinal());
        eData.setRequestID(0);
//        eData.setFunID(ClusterProtocal.FUNCTION.SqlUpdate.ordinal());
        eData.setMessage("SQL_UPDATE");
        eData.setNettyType(ClusterProtocal.FUNCTION.SqlUpdate);


        eData.appendRow();

        eData.appendRow();
        eData.addData(data);
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


    public String getScript() {
        if (data == null) return null;
        return (String) data.getDataValue(row, 0);
    }

    public String getKey() {
        return data.getExtData(1).toString();
    }
}

