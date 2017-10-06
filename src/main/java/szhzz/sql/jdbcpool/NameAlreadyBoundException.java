/*
 * NameAlreadyBoundException.java
 *
 * Created on 2003年7月7日, 下午10:41
 */

package szhzz.sql.jdbcpool;

/**
 * @author HuangFang
 */
public class NameAlreadyBoundException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>NameAlreadyBoundException</code> without detail message.
     */
    public NameAlreadyBoundException() {
    }


    /**
     * Constructs an instance of <code>NameAlreadyBoundException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NameAlreadyBoundException(String msg) {
        super(msg);
    }
}
