package szhzz.Netty.Cluster.UDP;

import sun.applet.AppletEvent;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;

/**
 * Created by Administrator on 2017/3/29.
 */
public class ClientDebug {

    public static void main(String[] args) throws Exception {
//                DatagramSocket client = new DatagramSocket();


//        new UdpServer(7530).startServer();
//        UdpClient.addRemote("127.0.0.1");
//        UdpClient client = new UdpClient();
//        client.setPort(7525);
        UdpClient_Abstract.setInstance("UdpClient"); //"UdpClient_Netty"
        UdpClient_Abstract.getInstance().addRemote(args[0]);


//        NettyExchangeData data = new NettyExchangeData();
//        data.appendRow();
//
//        data.addData(0); //colErrCode
//        data.addData("login ID"); //colLogonID
//        data.addData("Request");  //colRequestID
//        data.addData("FUN-id");  // colFunID
//        data.addData("No Error");  //colMessage
//
//        data.appendRow();
//        data.appendRow();
//        data.addData("first");
//        data.addData("second");
//        data.addData("3");

//        String sendStr = data.encode();
//        byte[] sendBuf;
//        sendBuf = sendStr.getBytes();
//        InetAddress addr = InetAddress.getByName("127.0.0.1");
//        int tcpPort = 7525;
//        DatagramPacket sendPacket = new DatagramPacket(sendBuf ,sendBuf.length , addr , tcpPort);
//        client.send(sendPacket);


        UdpClient_Abstract.getInstance().send("TestString");


//        // 初始化本地UDP的Socket
//        LocalUDPSocketProvider.getInstance().initSocket();
//        // 启动本地UDP监听（接收数据用的）
//        LocalUDPDataReciever.getInstance().startup();
//
//        // 循环发送数据给服务端
//        while (true) {
//            // 要发送的数据
//            String toServer = "Hi，我是客户端，我的时间戳" + System.currentTimeMillis();
//            byte[] soServerBytes = toServer.getBytes("UTF-8");
//
//            // 开始发送
//            boolean ok = true;
////            boolean ok = UDPUtils.send(soServerBytes, soServerBytes.length);
//            if (ok)
//                System.out.println("ClientDebug 发往服务端的信息已送出.");
//            else
//                System.out.println("ClientDebug 发往服务端的信息没有成功发出！！！");
//
//            // 3000秒后进入下一次循环
//            Thread.sleep(3000);
//        }
    }
}