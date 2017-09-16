package szhzz.sectionmath;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-6-14
 * Time: 9:44:37
 * To change this template use File | Settings | File Templates.
 */
public class EMA implements Serializable {
    float oldVal = 0f;
    int length = 0;

    public float Next(float newVal, int period) {
        if (length == 0) {
            oldVal = newVal;
            length = 1;
        }


        oldVal = (2 * newVal + (period - 1) * oldVal) / (period + 1);
        return oldVal;
    }

    public float Next(float newVal, float oldV, int period) {
        if (oldV == 0) {
            oldV = newVal;
            length = 1;
        }
        return (2 * newVal + (period - 1) * oldV) / (period + 1);
    }
}
