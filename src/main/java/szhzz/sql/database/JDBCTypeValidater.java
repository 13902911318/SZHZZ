package szhzz.sql.database;


import szhzz.Utils.DawLogger;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;


/**
 * 根据数据库字段的属性设置数据的检查功能
 * <p/>
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-9-30
 * Time: 9:40:58
 */
public class JDBCTypeValidater {
    private static DawLogger logger = DawLogger.getLogger(JDBCTypeValidater.class);

    public static void setValidater(DataStore ds, int col, ResultSetMetaData rm) {
        int type = 0;
        try {
            if (rm.isNullable(col) == ResultSetMetaData.columnNoNulls) {
                ds.setColValidator(col - 1, DataValidater.getNotNULL());
            }
            if (rm.isAutoIncrement(col) || rm.isReadOnly(col)) {
                ds.setReadOnlyCol(col - 1, true);
            }

            type = rm.getColumnType(col);
            switch (type) {
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                case Types.DOUBLE:
                case Types.DECIMAL:
                case Types.NUMERIC:
                case Types.FLOAT:
                    ds.setColValidator(col - 1, DataValidater.getIsNumber());
                    break;
                case Types.BIT:
//                    ds.setColValidator(col - 1, DataValidater.getIsNumber());
//                    ds.setColValidator(col - 1, DataValidater.getMaxNumber(1));
//                    ds.setColValidator(col - 1, DataValidater.getMinNumber(0));
                    break;
                case Types.DATE:
                    ds.setColValidator(col - 1, DataValidater.getDateValidater());
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    ds.setColValidator(col - 1, DataValidater.getStringLength(rm.getColumnDisplaySize(col)));
                case Types.BLOB:
                case Types.CLOB:
                case Types.NULL:
                default:
            }

        } catch (SQLException e) {
            logger.error("SQL Exception in getColumnData.", e);
        }
    }
}
