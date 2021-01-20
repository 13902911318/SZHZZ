package szhzz.NFile;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-5-10
 * Time: 上午12:14
 * To change this template use File | Settings | File Templates.
 */

import szhzz.App.AppManager;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.Executor;

import java.io.File;
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
    private Ctime ctime = null;
    private int timeout = 3;
    private File folder = null;
    Kind<?>[] events = null;
    private String lastFileName = "";
    private Kind<?> lastKind = null;

    public void autoRecoverPath(int seconds) {
        if (seconds > 0) {
            if (seconds < timeout) seconds = timeout + 1;

            if (ctime == null) {
                ctime = new Ctime();
                ctime.setCircleTime(seconds * 1000);
            }
        } else {
            if (ctime != null) {
                ctime.stopTimer();
                ctime = null;
            }
        }
    }

    public DirectoryWatcher(String dir) throws IOException {
        onPath = dir;
        folder = new File(dir);

        watcher = FileSystems.getDefault().newWatchService();
        path = Paths.get(onPath);
    }

    void reStart() throws IOException {
        close = false;

        watcher = FileSystems.getDefault().newWatchService();
        path = Paths.get(onPath);

        if (events == null)
            events = new Kind<?>[]{ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE, OVERFLOW};

        path.register(watcher, events); //,

        processEvents();

    }

    public void register(WatchEvent.Kind<?>... events) throws IOException {
        this.events = events;
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
                    key = watcher.poll(timeout, TimeUnit.SECONDS);
                } catch (InterruptedException x) {
                    logger.error(new Exception("Directory Watcher exit"));
                    break;
                }
                if (AppManager.isQuitApp()) {
                    break;
                }

                if (key != null) {
                    for (WatchEvent event : key.pollEvents()) {
                        Kind kind = event.kind();

                        if (kind == OVERFLOW) {
                            AppManager.logEvent("Captured OVERFLOW!" + onPath);
                            stopWatch();
                            break;
                        }


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
        } catch (Exception e) {
            logger.error(e);
        } finally {
            // Exit if directory is deleted
            try {
                watcher.close();
            } catch (IOException e) {

            }
            watcher = null;
            path = null;
            AppManager.logit("DirectoryWatcher stoped on " + onPath);
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
        if (kind == lastKind && fileName.equals(lastFileName)) return;
        lastKind = kind;
        lastFileName = fileName;

        notifyObservers(new FileSystemEventArgs(fileName, kind));
    }

    @Override
    public void run() {
        processEvents();
    }

    public void kille(){
        if (ctime != null) {
            ctime.stopTimer();
            ctime = null;
        }
        this.close = true;
    }

    public void stopWatch() {
        this.close = true;
        AppManager.logEvent("Directory Watcher stop on " + onPath);
    }


    boolean getFolder(File folder) {
        if (!folder.exists() || !folder.isDirectory()) {
            try {
                folder.mkdirs();
            } catch (Exception ignored) {

            }
            if (!folder.exists() || !folder.isDirectory()) {
                String command = "net use  " + folder.getAbsolutePath();
                try {
                    Executor.getInstance().execute(new String[]{command}, true, false, "", true);
                } catch (Exception e) {
                    logger.error(e);
                    return false;
                }
            }

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException ignored) {

            }
            try {
                folder.mkdirs();
            } catch (Exception ignored) {

            }
            if (!folder.exists() || !folder.isDirectory()) {
                AppManager.logit("Can not use folder:" + folder.getAbsolutePath());
            } else {
                AppManager.logEvent("use folder:" + folder.getAbsolutePath());
            }
        }
        return folder.exists();
    }

    class Ctime extends CircleTimer {

        @Override
        public void execTask() {
            try {
                if (folder == null) {
                    folder = new File(onPath);
                }
                if (watcher != null) {
                    if (!folder.exists() && !folder.isDirectory()) {
                        stopWatch();
                    }
                } else {
                    if (getFolder(folder)) {
                        AppManager.logEvent("DirectoryWatcher restart path watcher:" + onPath);
                        try {
                            reStart();
                        } catch (IOException e) {
                            logger.error(e);
                        }
                    }
                }
            } finally {
                this.circleTime();
            }
        }
    }

}