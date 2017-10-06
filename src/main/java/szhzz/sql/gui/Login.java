package szhzz.sql.gui;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-9-27
 * Time: 22:08:31
 * <p/>
 * A simple login manager
 * we may extend this class to manager liogin base on user data in a database
 */
public abstract class Login {
    public Login() {
    }

    public abstract void logIn();

    public abstract void logOut();
}
