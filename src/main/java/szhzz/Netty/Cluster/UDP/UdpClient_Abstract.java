package szhzz.Netty.Cluster.UDP;

import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;

import java.net.SocketException;

/**
 * Created by Administrator on 2017/3/29.
 */
public abstract class UdpClient_Abstract {

    int port = 7530;
    private static UdpClient_Abstract onlyOne = null;

    public static void setInstance(String className) {
        if ("UdpClient_Netty".equals(className)) {
            onlyOne = new UdpClient_Netty();
        } else if ("UdpClient".equals(className)) {
            onlyOne = new UdpClient();
        }

    }

    public static UdpClient_Abstract getInstance() {
        if (onlyOne == null) {
            onlyOne = new UdpClient_Netty();
        }
        return onlyOne;
    }

    public void setPort(int port) throws SocketException {
        this.port = port;
    }

    public abstract void addRemote(String host);

    public int getPort() {
        return port;
    }

    public abstract void removeRemote(String host);
//    {
//        remoteAdress.remove(host);
//    }

    public abstract void send(NettyExchangeData data);

    public abstract void close();


}
