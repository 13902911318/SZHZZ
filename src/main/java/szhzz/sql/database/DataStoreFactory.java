package szhzz.sql.database;


import szhzz.Table.MatrixTable;
import szhzz.Utils.NU;

/**
 * Created by HuangFang on 2015/2/22.
 * 16:03
 */
public class DataStoreFactory {
    private static final int columnAlias = 0;
    private static final int columnType = 1;
    private static final int columnNotNull = 2;
    private static final int columnName = 3;
    private static final int columnLength = 4;
    private static final int columnDefault = 5;

    public static DataStore getDataStore(String T) {
        DataStore ds = new DataStore();
        MatrixTable table = new MatrixTable();
        table.read(T);
        for (int i = 0; i < table.rowCount(); i++) {
            ds.setColName(table.get(i, columnName), i);
            ds.setColTypeName(table.get(i, columnType), i);
            ds.setColLength(i, NU.parseInt(table.get(i, columnLength), 10));

            if ("Y".equals(table.get(i, columnNotNull))) {
                String val = table.get(i, columnDefault);

                String colDataType = table.get(i, columnType);
                if ("String".equals(colDataType) || "Date".equals(colDataType) || "Time".equals(colDataType)) {
                    if (val == null) val = "";
                    ds.setDefaltValues(i, val);
                } else if ("Integer".equals(colDataType)) {
                    ds.setDefaltValues(i, NU.parseInt(val, 0));
                } else if ("Long".equals(colDataType)) {
                    ds.setDefaltValues(i, NU.parseLong(val, 0L));
                } else if ("Double".equals(colDataType)) {
                    ds.setDefaltValues(i, NU.parseDouble(val, 0d));
                } else {
                    if (val == null) val = "";
                    ds.setDefaltValues(i, val);
                }
            }
        }
        return ds;
    }
}


