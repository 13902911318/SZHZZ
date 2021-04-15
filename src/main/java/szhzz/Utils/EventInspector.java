package szhzz.Utils;

/**
 * Created by Administrator on 2019/11/2.
 */
public interface EventInspector<I> {
    public boolean callBack(I inf);
    public void exceptionCaught(Throwable cause);
}
