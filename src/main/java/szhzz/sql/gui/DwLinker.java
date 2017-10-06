package szhzz.sql.gui;


/**
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-10-6
 * Time: 9:15:13
 * To change this template use File | Settings | File Templates.
 */
public class DwLinker {
    DataWindow dwMaster;
    DataWindow dwSlave;

    public DwLinker() {

    }

    public void setDataWindows(DataWindow dwMaster, DataWindow dwSlave) {
        this.dwSlave = dwSlave;
        this.dwMaster = dwMaster;
        dwMaster.addsRowChangedListener(new RowChanged_());
    }

    public void RowChanged(int currentRow, int rowCount) {
    }

    private class RowChanged_ extends DWRowChanged {

        public void rowChanged(int currentRow, int rowCount) {
            RowChanged(currentRow, rowCount);
        }

        ;

    }

}
