package szhzz.sql.database;

import java.sql.SQLException;

public class DBException extends Exception {
    public static final int UNKNOWN = 0;
    public static final int WRITEPROTECTED = 1;
    public static final int OTHERFILE = 2;
    public static final int NOOUTPUTFILE = 3;
    public static final int CLASSNOTFOUND = 4;
    public static final int SQLEXCEPTION = 5;
    public static final int DBNOTFOUND = 6;
    public static final int ILLEGALCOMMENT = 7;
    public static final int ILLEGALCHAR = 8;
    public static final int UNSUPPORTEDENCODING = 9;
    public static final int CONSTRAINTVIOLATION = 10;
    public static final int SYNTAXERROR = 11;
    public static final int PROPERTYFILENOTFOUND = 12;
    public static final int PROPERTYFILEREADERROR = 13;
    public static final int UNSUPPORTEDFEATURE = 14;
    public static final int IOEXCEPTION = 15;
    public static final int ILLEGALFORMAT = 16;
    public static final int UNKNOWNDBTYPE = 17;
    public static final int NOTIMPLEMENTED = 18;
    public static final int LAST = 18;
    public static final int USEREROOR = 19;
    public static final int RUNTIMEERROR = 20;
    public static final int ILLEGALVALUE = 21;

    /**
     * Array of default messages.
     */
    public static final String mess[] = {
            "Unknown Exception",
            "File is write protected! Please specify other output file or select standard out!",
            "Please specify other output file or select standard out!",
            "No output file queryName specified! Please specify output file or select standard out!",
            "Class not found! Please specify other class class or check CLASSPATH!",
            "SQL exception",
            "Database not found",
            "Comment contains illegal string: --",
            "Illegal charater in string",
            "Unsupported encoding",
            "Vioalation of constraint",
            "Syntax error in query field (Missing character ])",
            "Property file not found",
            "Could not read property file",
            "Driver does not support feature",
            "Error during IO",
            "Illegal time or date format",
            "Unsupported DB type found: ",
            "Method not implemented",
            "User operate Error",   // GUI operation error
            "Program runtime error. DEBUG noly!",
            "Illegal value."
    };

    private int kind;
    private Exception originalException;

    /**
     * Constructs a DBException of a given kind with a given message.
     *
     * @param s    the message string
     * @param type the kind of the exception
     */
    public DBException(String s, int type) {
        super(s);
        kind = type;
    }

    /**
     * Constructs a DBException of a given kind. The message is the
     * default message for this kind.
     *
     * @param type the kind of the exception
     */
    public DBException(int type) {
        super(mess[(type < 0 || type > LAST) ? UNKNOWN : type]);
        kind = (type < 0 || type > LAST) ? UNKNOWN : type;
    }

    /**
     * Constructs a DBException from a SQLException. The kind is
     * <code>SQLEXCEPTION</code>
     *
     * @param e SQLException
     */
    public DBException(SQLException e) {
        super(e);
        kind = SQLEXCEPTION;
        originalException = e;
    }

//    public DBException(Exception e) {
//      super(e.getMessage());
//      kind = RUNTIMEERROR;
//      originalException = e;
//    }

    /**
     * Access method for default message
     *
     * @param id the number of the exception
     * @return the default message
     */
    static public String getDefaultMessage(int id) {
        return mess[id];
    }

    /**
     * Access method for the kind of the exception
     *
     * @return the kind of the exception
     */
    public int getKind() {
        return kind;
    }

    /**
     * Access method for original exception
     *
     * @return the original exception
     */
    public Exception getOriginalException() {
        return originalException;
    }
}
