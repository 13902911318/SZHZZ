package szhzz.sql.database;


import java.util.Hashtable;

/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-2
 * Time: 10:21:07
 * To change this template use File | Settings | File Templates.
 */
public class SqlJavaTypeMaper {
    static Hashtable types = null;


    public SqlJavaTypeMaper() {
    }

    /**
     * @param sName
     * @return String
     */
    public static String SqlToJavaName(String sName) {
        if (null == types) {
            init();
        }
        if (null != sName && types.containsKey(sName.toLowerCase())) {
            return (String) types.get(sName.toLowerCase());
        }
        return sName;
    }

    /**
     * @param sName
     * @return Class
     */
    public static Class SqlToJavaClass(String sName) {
        if (null == types) {
            init();
        }
        try {
            if (null != sName) {
                // TODO Check data format Error!!!
                if (sName.toLowerCase().startsWith("date")) {
//                    //sName =
//                    String s = "java.lang.String";
                    String s = SqlToJavaName(sName);
                    return Class.forName(s);
                }
                return Class.forName(SqlToJavaName(sName));
            }
        } catch (ClassNotFoundException e) {
//            loger.getLoger().debug("SQL Type Map not found " + sName);
            return Object.class;
        }
        return Object.class;
    }

    public static void init() {
        types = new Hashtable();
        types.put("char", "java.lang.String");
        types.put("unichar", "java.lang.String");
        types.put("nchar", "java.lang.String");
        types.put("varchar", "java.lang.String");
        types.put("varchar2", "java.lang.String");
        types.put("longvarchar", "java.lang.String");
        types.put("univarchar", "java.lang.String");
        types.put("nvarchar", "java.lang.String");
        types.put("text", "java.lang.String");
        types.put("numeric", "java.math.BigDecimal");
        types.put("decimal", "java.math.BigDecimal");
        types.put("money", "java.math.BigDecimal");
        types.put("smallmoney", "java.math.BigDecimal");
        types.put("number", "java.lang.Integer");
        types.put("int", "java.lang.Integer");
        types.put("tinyint", "java.lang.Integer");
        types.put("smallint", "java.lang.Integer");
        types.put("java.lang.Integer", "java.lang.Integer");
        types.put("unsigned", "java.lang.Integer");
        types.put("unsigned int", "java.lang.Integer");
        types.put("bigint", "java.math.BigInteger");
        types.put("unsigned bigint", "java.math.BigInteger");
        types.put("real", "java.lang.Float");
        types.put("float", "java.lang.Double");
        types.put("double precision", "java.lang.Double");
        types.put("binary", "java.szhzz.sql.Blob");            // byte[]
        types.put("varbinary", "java.szhzz.sql.Blob");         // byte[]
        types.put("longvarbinary", "java.szhzz.sql.Blob");     // byte[]
        types.put("datetime", "java.szhzz.sql.Timestamp");
        types.put("smalldatetime", "java.szhzz.sql.Timestamp");
        types.put("date", "java.szhzz.sql.Date");
        types.put("time", "java.szhzz.sql.Time");
        types.put("bit", "java.lang.Boolean");
    }
}
