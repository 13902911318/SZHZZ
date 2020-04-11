package szhzz.NFile;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-5-10
 * Time: 上午12:14
 * To change this template use File | Settings | File Templates.
 */

import szhzz.App.AppManager;
import szhzz.Utils.DawLogger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * 监控一个目录内文件的更新、创建和删除事件（不包括子目录）
 * <p/>
 * <p/>
 * 适于Java SE 7
 */
public class DirectoryWatcher extends Observable implements Runnable {
    private static DawLogger logger = DawLogger.getLogger(DirectoryWatcher.class);
    private WatchService watcher;
    private Path path;
    private WatchKey key;
    private String onPath = "";
    private boolean close = false;

    public DirectoryWatcher(String dir) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        path = Paths.get(dir);
        onPath = dir;
        path.register(watcher, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE); //,
    }

    public void register(WatchEvent.Kind<?>... events) throws IOException {
        path.register(watcher, events); //ENTRY_MODIFY,
    }

    public String toString() {
        return getClass().getName() + " on " + onPath;
    }

    /**
     * 监控文件系统事件
     */
    private void processEvents() {
        try {
        while (!close) {
            // 等待直到获得事件信号

            try {
                key = watcher.poll(3, TimeUnit.SECONDS);
            } catch (InterruptedException x) {
                logger.error(new Exception( "Directory Watcher exit"));
                return;
            }
            if (AppManager.isQuitApp()) {
                break;
            }

            if (key != null) {
                for (WatchEvent event : key.pollEvents()) {
                    Kind kind = event.kind();

                    // TBD - provide example of how OVERFLOW event is handled
                    //                if (kind == OVERFLOW) {
//                    continue;
//                }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path name = ev.context();
                    notifiy(name.getFileName().toString(), kind);
                }
                //    为监控下一个通知做准备
                if (!key.reset()) {
                        break;
                }
            }
        }
        // Exit if directory is deleted
        watcher.close();
        } catch (Exception e) {
            logger.error(e);
        }finally {
            close = false;
        }
    }

    /**
     * 通知外部各个Observer目录有新的事件更新
     */
    void notifiy(String fileName, Kind<?> kind) {
        // 标注目录已经被做了更改
        setChanged();
        //     主动通知各个观察者目标对象状态的变更
        //    这里采用的是观察者模式的“推”方式
        notifyObservers(new FileSystemEventArgs(fileName, kind));
    }

    @Override
    public void run() {
        processEvents();
     }

    public void stopWatch() {
        this.close = close;
    }
}