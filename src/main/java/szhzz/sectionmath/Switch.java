package szhzz.sectionmath;

/**
 * Created with IntelliJ IDEA.
 * User: SZHZZ
 * Date: 13-3-4
 * Time: 下午2:45
 * To change this template use File | Settings | File Templates.
 * 钝化指标，消除毛刺
 */
public class Switch {
    private int threshold = 0;
    private int accVal = 0;
    private boolean state = true;

    public Switch(int threshold) {
        this.threshold = threshold;
    }

    public Switch(int threshold, boolean initState) {
        this.threshold = threshold;
        this.state = initState;
    }

    public boolean add(int val) {
//        System.out.print("\t" + val );
        if (val > 0) {
            if (accVal < threshold)
                if (++accVal == threshold) {
                    state = true;
                }
        } else {
            if (accVal > 0)
                if (--accVal == 0) {
                    state = false;
                }
        }

//        System.out.println("\t" + accVal + "\t" + state);
        return state;
    }

    public boolean add(boolean val) {
        return add(val ? 1 : -1);
    }
}
