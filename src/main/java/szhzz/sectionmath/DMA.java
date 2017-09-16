package szhzz.sectionmath;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-6-14
 * Time: 9:44:37
 * To change this template use File | Settings | File Templates.
 */
public class DMA implements Serializable {
    private static final long serialVersionUID = 1L;

    float oldVal = 0f;
    int length = 0;

    public float Next(float X, float A) {
        if (length == 0) {
            oldVal = X;
            length = 1;
        }

        oldVal = (A * X + (1 - A) * oldVal);
        return oldVal;
    }

    public float Try(float X, float A) {
        if (length == 0) {
            oldVal = X;
        }
        return (A * X + (1 - A) * oldVal);
    }

    public float Next(float X, float oldX, float A) {
        if (oldX == 0) {
            oldX = X;
        }
        oldVal = (A * X + (1 - A) * oldX);
        return oldVal;
    }

    public void reset() {
        oldVal = 0f;
        length = 0;
    }
}
