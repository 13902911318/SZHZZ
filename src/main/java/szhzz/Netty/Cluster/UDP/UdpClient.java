package szhzz.Netty.Cluster.UDP;

import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Utils.DawLogger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Hashtable;

/**
 * Created by Administrator on 2017/3/29.
 */
public class UdpClient extends UdpClient_Abstract {
    private static DawLogger logger = DawLogger.getLogger(UdpClient.class);
    DatagramSocket client = null;


    static Hashtable<String, InetAddress> remoteAdress = new Hashtable<>();


    public void addRemote(String host) {
        try {
            host = host.replace("/", "");
            if (host.indexOf(":") > 0) {
                host = host.substring(0, host.indexOf(":"));
            }
            if (remoteAdress.containsKey(host)) return;
            InetAddress addr = InetAddress.getByName(host);
            remoteAdress.put(host, addr);
        } catch (UnknownHostException e) {
            logger.error(e);
        }
    }

    public void removeRemote(String host) {
        remoteAdress.remove(host);
    }


    public void send(NettyExchangeData data) {
        send(data.encode());
    }

    @Override
    public void close() {

    }

    private void send(String sendStr) {
        byte[] sendBuf;

        sendBuf = sendStr.getBytes(Charset.forName("UTF-8"));
        try {
            if (client == null) {
                client = new DatagramSocket();
            }

            for (InetAddress addr : remoteAdress.values()) {
                DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, addr, getPort());
                client.send(sendPacket);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
