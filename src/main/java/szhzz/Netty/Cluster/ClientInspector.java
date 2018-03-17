package szhzz.Netty.Cluster;

import io.netty.channel.Channel;

/**
 * Created by Administrator on 2018/3/15.
 */
public interface ClientInspector {
    public void connected(Channel channel);
    public void login(String user, String password);
    public void logout(String user);
    public void disConnected();
}
