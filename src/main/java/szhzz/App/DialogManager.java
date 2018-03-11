package szhzz.App;

import com.sun.deploy.panel.ControlPanel;
import szhzz.Netty.Cluster.ClusterStation;
import szhzz.StatusInspect.StatusView;
import szhzz.sql.database.DbConnectionsView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/10/6.
 */
public class DialogManager {
    protected static DialogManager onlyOne = null;
    protected ClusterStation clusterStation = null;
    protected StatusView statusView = null;
    protected JFrame frame;
    protected DbConnectionsView dbView = null;
    protected ArrayList<JDialog> dialogs = new ArrayList<>();
    protected TaskView taskView = null;

    private BeQuit autoQuit = new BeQuit() {

        @Override
        public boolean Quit() {
            for (JDialog d : dialogs) {
                d.dispose();
            }
            return true;
        }
    };

    protected DialogManager() {
        frame = AppManager.getApp().getMainFram();
    }

    public static DialogManager getInstance() {
        if (onlyOne == null) {
            onlyOne = new DialogManager();
        }
        return onlyOne;
    }



    public synchronized void openDbView() {
        if (dbView == null) {
            dbView = new DbConnectionsView();
            dbView.setModalityType(Dialog.ModalityType.MODELESS);
            dbView.pack();
            dialogs.add(dbView);

        }
        dbView.setVisible(true);
    }


    public void openClusterStation() {
        if (clusterStation == null) {
            clusterStation = new ClusterStation(frame);
            clusterStation.setModalityType(Dialog.ModalityType.MODELESS);
//            clusterStation.pack();
//            clusterStation.loadPref();
            dialogs.add(clusterStation);
        }
        clusterStation.setVisible(true);
    }


    public void openStatuesView() {
        if (statusView == null) {
            statusView = new StatusView();
            dialogs.add(statusView);

            statusView.pack();
            statusView.setTitle(frame.getTitle());

        }
        statusView.setVisible(true);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {

        }
        statusView.loadPref();
    }

    public StatusView getStatuesView() {
        return statusView;
    }

    public void closeStatuesView() {
        if (statusView != null) {
            statusView.setVisible(false);
        }
    }





    public void openTaskView() {
        if (taskView == null) {
            taskView = new TaskView(frame);
            taskView.setModalityType(Dialog.ModalityType.MODELESS);
            taskView.pack();
            taskView.setTitle("任务观察窗口");
            taskView.loadPref();
            dialogs.add(taskView);
        }
        taskView.fresh();
        taskView.setVisible(true);
    }



}
