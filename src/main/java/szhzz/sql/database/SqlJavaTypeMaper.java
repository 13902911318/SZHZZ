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
    private static Hashtable typeClass = null;
    private static Hashtable<String, String> typeName = null;


    public SqlJavaTypeMaper() {
    }

    /**
     * @param sName
     * @return String
     */
    public static String SqlToJavaName(String sName) {
        if (null == typeClass) {
            init();
        }
        if (null != sName && typeClass.containsKey(sName.toLowerCase())) {
            return (String) typeClass.get(sName.toLowerCase());
        }
        return sName;
    }

    /**
     * @param sName
     * @return Class
     */
    public static Class SqlToJavaClass(String sName) {
        if (null == typeClass) {
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

    public static String SqlToJavaType(String sName) {
        init();
        String[] n = sName.split(" ");
        String javaType = typeName.get(n[0]);
        if (javaType == null) {
            javaType = typeName.get(n[1]);
        }
        if (javaType == null) {
            javaType = "String";
        }
        return javaType;
    }

    public static synchronized void init() {
        if (typeClass != null) return;

        typeClass = new Hashtable();
        typeName = new Hashtable<String, String>();

        typeClass.put("char", "java.lang.String");
        typeClass.put("unichar", "java.lang.String");
        typeClass.put("nchar", "java.lang.String");
        typeClass.put("varchar", "java.lang.String");
        typeClass.put("varchar2", "java.lang.String");
        typeClass.put("longvarchar", "java.lang.String");
        typeClass.put("univarchar", "java.lang.String");
        typeClass.put("nvarchar", "java.lang.String");
        typeClass.put("text", "java.lang.String");
        typeClass.put("numeric", "java.math.BigDecimal");
        typeClass.put("decimal", "java.math.BigDecimal");
        typeClass.put("money", "java.math.BigDecimal");
        typeClass.put("smallmoney", "java.math.BigDecimal");
        typeClass.put("number", "java.lang.Integer");
        typeClass.put("int", "java.lang.Integer");
        typeClass.put("tinyint", "java.lang.Integer");
        typeClass.put("smallint", "java.lang.Integer");
        typeClass.put("java.lang.Integer", "java.lang.Integer");
        typeClass.put("unsigned", "java.lang.Integer");
        typeClass.put("unsigned int", "java.lang.Integer");
        typeClass.put("bigint", "java.math.BigInteger");
        typeClass.put("unsigned bigint", "java.math.BigInteger");
        typeClass.put("real", "java.lang.Float");
        typeClass.put("float", "java.lang.Double");
        typeClass.put("double", "java.lang.Double");
        typeClass.put("binary", "java.sql.Blob");            // byte[]
        typeClass.put("varbinary", "java.sql.Blob");         // byte[]
        typeClass.put("longvarbinary", "java.sql.Blob");     // byte[]
        typeClass.put("datetime", "java.sql.Timestamp");
        typeClass.put("smalldatetime", "java.sql.Timestamp");
        typeClass.put("date", "java.sql.Date");
        typeClass.put("time", "java.sql.Time");
        typeClass.put("bit", "java.lang.Boolean");

        typeName.put("char", "String");
        typeName.put("unichar", "String");
        typeName.put("nchar", "String");
        typeName.put("varchar", "String");
        typeName.put("varchar2", "String");
        typeName.put("longvarchar", "String");
        typeName.put("univarchar", "String");
        typeName.put("nvarchar", "String");
        typeName.put("text", "String");
        typeName.put("numeric", "BigDecimal");
        typeName.put("decimal", "BigDecimal");
        typeName.put("money", "BigDecimal");
        typeName.put("smallmoney", "BigDecimal");
        typeName.put("number", "Integer");
        typeName.put("int", "Integer");
        typeName.put("tinyint", "Integer");
        typeName.put("smallint", "Integer");
        typeName.put("Integer", "Integer");
        typeName.put("unsigned", "Integer");
        typeName.put("unsigned int", "Integer");
        typeName.put("bigint", "BigInteger");
        typeName.put("real", "Float");
        typeName.put("float", "Double");
        typeName.put("double", "Double");
        typeName.put("binary", "Blob");            // byte[]
        typeName.put("varbinary", "Blob");         // byte[]
        typeName.put("longvarbinary", "Blob");     // byte[]
        typeName.put("datetime", "Timestamp");
        typeName.put("smalldatetime", "Timestamp");
        typeName.put("date", "Date");
        typeName.put("time", "Time");
        typeName.put("bit", "Boolean");
    }
}
