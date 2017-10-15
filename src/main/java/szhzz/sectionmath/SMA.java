package szhzz.sectionmath;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-6-14
 * Time: 9:44:37
 * To change this template use File | Settings | File Templates.
 */
public class SMA implements Serializable {
    double oldVal = 0f;
    int length = 0;

    /**
     * 用法：SMA(X,N,M)，求X的N日移动平均，M为权重。
     * 算法： 若Y=SMA(X,N,M)则 old_X=[M*X+(N-M)*old_X']/N,
     * 其中Y'表示上一周期Y值，N必须大于M。
     * 例如：SMA(CLOSE,30,1)表示求30日移动平均价。
     */
    public double Next(double X, int N, int M) {
        if (length == 0) {
            oldVal = X;
            length = 1;
        }

        oldVal = (M * X + (N - M) * oldVal) / N;
        return oldVal;
    }

    public double Try(double X, int N, int M) {
        if (length == 0) {
            oldVal = X;
//            length = 1;
        }
        return (M * X + (N - M) * oldVal) / N;
    }

    public double Next(double X, double oldX, int N, int M) {
        if (oldX == 0) {
            oldX = X;
        }
        oldVal = (M * X + (N - M) * oldX) / N;
        return oldVal;
    }

    public void reset() {
        oldVal = 0f;
        length = 0;
    }
}
