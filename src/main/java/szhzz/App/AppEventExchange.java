package szhzz.App;


import szhzz.Config.Config;
import szhzz.Config.SharedCfgProvider;
import szhzz.NFile.DirectoryWatcher;
import szhzz.NFile.FileSystemEventArgs;
import szhzz.Netty.Cluster.BusinessRuse;
import szhzz.Netty.Cluster.ExchangeDataType.CfgUpdateWrap;
import szhzz.Netty.Cluster.ExchangeDataType.NettyExchangeData;
import szhzz.Netty.Proxy.MarketProxy;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;

import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-10-4
 * Time: 下午3:24
 * To change this template use File | Settings | File Templates.
 */
public class AppEventExchange {
    private static final String eventFile = "event.ini";
    private static DawLogger logger = DawLogger.getLogger(AppEventExchange.class);
    private static AppEventExchange onlyOne = null;
    private boolean onSelfModify = false;
    private Config cfg = null;
    private long lastEventTime = 0;
    private long toDelay = 1000;
    private EVENT eventType = EVENT.CREATE;
    private DirectoryWatcher watcher = null;
    private int timeOffset = 0;


    public static enum EVENT {
        CREATE,
        DELETE,
        MODIFY
    }

    private AppEventExchange(){
        setLocal();
    }

    /**
     * 对于短时连续发生的文件更改，
     * 仅最后一次时间间隔大于 toDelay 的有效
     * 避免文件频繁修改时读入数据
     */
    private CircleTimer onDelay = new CircleTimer() {
        @Override
        public void execTask() {
            cfg.reLoad();
            timeOffset = ((Double)cfg.getDoubleVal("时间误差", 0d)).intValue();
            AppManager.setSystemTimeDiff(timeOffset);
            MessageAbstract.getInstance().sendMessage(MessageCode.ExternalEvent, "时间误差");
        }
    };

    public static AppEventExchange getInstance() {
        if (onlyOne == null) {
            onlyOne = new AppEventExchange();
        }
        return onlyOne;
    }

    public void clear() {
        if (cfg != null) {
            cfg.clear();
            cfg.save();
        }
    }

    private void setLocal() {
        if (watcher != null) return;
        cfg = SharedCfgProvider.getInstance("EVENT").getCfg("event.ini") ;
        try {
            watcher = new DirectoryWatcher(SharedCfgProvider.getInstance("EVENT").getDir());
            watcher.register(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);//
            watcher.addObserver(new FileObserver());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            AppManager.executeInBack(watcher);
        } catch (InterruptedException e) {
            logger.error(e);
        }
        AppManager.setSystemTimeDiff(NU.parseLong(getEvent("时间误差"), 0L));
    }

    public EVENT getEventType() {
        return eventType;
    }

    public void setEvent(String name, String value, String comment) {
        setEvent(name, value, false, comment);
    }
    public void setEvent(String name, String value) {
        setEvent(name, value, null);
    }

    public int getTimeOffset() {
        return timeOffset;
    }

//    public void setEvent(String name, String value, boolean broadcast, boolean isForMe) {
//        try {
//            if (name == null || value == null || name.length() == 0) return;
//
//            onSelfModify = true;
//            cfg.reLoad();//避免覆盖其它程序的更新
//            cfg.setProperty(name, value);
//            cfg.save();
//
//            if("时间误差".equals(name)){
//                timeOffset = NU.parseInt(value, 0);
//            }
//
//            //&& MarketProxy.getInstance().isServerAlive()
//            if (broadcast) {
//                NettyExchangeData data = CfgUpdateWrap.getCfgUpdate(cfg.getConfigUrl(), name, value, cfg.getComment(name));
//                if(isForMe)data.setForMe();
//
//                BusinessRuse.getInstance().broadcast(data);
//            }
//            MessageAbstract.getInstance().sendMessage(MessageCode.ExternalEvent, name);
//        } catch (Exception e) {
//            logger.error(e);
//        } finally {
//            onSelfModify = false;
//        }
//    }

    public void setEvent(String name, String value, boolean broadcast) {
        setEvent(name, value, broadcast, null);
    }
    public void setEvent(String name, String value, boolean broadcast, String comment) {
        try {
            if (name == null || value == null || name.length() == 0) return;

            onSelfModify = true;
            cfg.reLoad();//避免覆盖其它程序的更新
            cfg.setProperty(name, value, comment);
            cfg.save();

            if("时间误差".equals(name)){
                timeOffset = NU.parseInt(value, 0);
            }

            //&& MarketProxy.getInstance().isServerAlive()
            if (broadcast) {
                NettyExchangeData data = CfgUpdateWrap.getCfgUpdate(cfg.getConfigUrl(), name, value, cfg.getComment(name));
                BusinessRuse.getInstance().broadcast(data);
            }
            MessageAbstract.getInstance().sendMessage(MessageCode.ExternalEvent, name);
        } catch (Exception e) {
            logger.error(e);
        } finally {
            onSelfModify = false;
        }
    }


    public void setEvent(ArrayList<String> name, ArrayList<String> value, ArrayList<String> comments, boolean broadcast) {
        try {
            if(name.size() > 0 &&  value.size() > 0) {
                onSelfModify = true;
                cfg.reLoad();//避免覆盖其它程序的更新
                for (int i = 0; i < name.size() && i < value.size(); i++) {
                    if(i < comments.size()){
                        cfg.setProperty(name.get(i), value.get(i), comments.get(i));
                    }else{
                        cfg.setProperty(name.get(i), value.get(i));
                    }
                    MessageAbstract.getInstance().sendMessage(MessageCode.ExternalEvent, name.get(i));

                    if (broadcast && MarketProxy.getInstance().isServerAlive()) {
                        NettyExchangeData data = CfgUpdateWrap.getCfgUpdate(cfg.getConfigUrl(), name.get(i), value.get(i), comments.get(i));
                        MarketProxy.getInstance().broadcast(data);
                    }
                }
                cfg.save();
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            onSelfModify = false;
        }
    }

    public String getEvent(String name) {
        return cfg.getProperty(name, "");
    }

    public String getEvent(String name, String def) {
        return cfg.getProperty(name, def);
    }

    public Config getEventCfg() {
        cfg.reLoad();
        return cfg;
    }


    public class FileObserver implements Observer {
        @Override
        public void update(Observable observable, Object eventArgs) {

            if (onSelfModify) return;

            try {
                FileSystemEventArgs args = (FileSystemEventArgs) eventArgs;
                if (!args.getFileName().contains(eventFile)) return;

                if (args.getKind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    eventType = EVENT.DELETE;
                } else if (args.getKind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    eventType = EVENT.CREATE;
                    onDelay.setCircleTime(toDelay);
                } else if (args.getKind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    eventType = EVENT.MODIFY;
                    onDelay.setCircleTime(toDelay);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }
}
