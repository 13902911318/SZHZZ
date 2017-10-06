package szhzz.DataBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-1-31
 * Time: 下午4:25
 * To change this template use File | Settings | File Templates.
 */
public interface DataConsumer {
    public long in(Object obj);

    public long in(long dataID, Object obj);
}
