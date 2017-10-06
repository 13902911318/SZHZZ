package szhzz.DataBuffer;

/**
 * Created by Administrator on 2016/12/27.
 */
public interface ObjectCoder {
    public Object decode(String msg);

    public String encode(Object obj);
}
