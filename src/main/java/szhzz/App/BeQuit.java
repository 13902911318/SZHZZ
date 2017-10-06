package szhzz.App;

/**
 * Created with IntelliJ IDEA.
 * User: HuangFang
 * Date: 13-11-10
 * Time: 下午7:51
 * To change this template use File | Settings | File Templates.
 */
public abstract class BeQuit implements Comparable {
    private Integer prio = 10;

    public BeQuit(Integer prio) {
        this.prio = prio;
        AppManager.registerBeQuit(this);
    }

    public BeQuit() {
        AppManager.registerBeQuit(this);
    }

    public int compareTo(Object o) {
        return prio.compareTo(((BeQuit) o).prio);
    }

    public abstract boolean Quit();

    public void setPrio(Integer prio) {
        this.prio = prio;
    }
}
