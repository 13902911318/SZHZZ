package szhzz.Netty.Cluster;

/**
 * Created by HuangFang on 2015/3/30.
 * 22:57
 */
public class ClusterProperty {
    String stationName = "NoNamed";
    String type = "本地";
    boolean connected = false;
    boolean onTrade = false;
    boolean offline = false;
    boolean canShutdown = false;
    String lastUpdate = "";
    long timeLap = 0L;
    String ipAddress = "";
    String mack = "";
    int level = 0;
    String closeDate = "";
    int positionError = 0;

    int group = 0;
    String cpuID = "";
    String appClass = "";
}
