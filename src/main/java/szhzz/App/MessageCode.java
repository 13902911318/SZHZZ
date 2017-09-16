package szhzz.App;

/**
 * Created by IntelliJ IDEA.
 * User: wn
 * Date: 2009-1-16
 * Time: 22:10:04
 * To change this template use File | Settings | File Templates.
 * <p/>
 * enum
 */
public enum MessageCode {
    QueryStatus,
    ReportStatus,
    InBackProcessStart,
    InBackProcessStop,
    StockcodecSelected,
    SetFilter,
    saveAs,
    StockCodesTableChanged,
    TradeAccountAdded,
    CapitalDataChanged,
    CreditDataChanged,
    CreditPostDataChanged,
    LiabilitiesDataChanged,
    SortInfoDataChanged,
    PositionDataChanged,
    OrderDataChanged,
    TradeDataChanged,
    ReckoningDataChanged,
    OneAccountInitFinished,
    AllAccountInitFinished,
    RealTimeDbInitFinished,
    SubmitToSaleStock,
    SubmitToCancel,
    TriggerSpareAmAuction,
    CheckHasSchedule,
    HardBit,
    TradeGuiDataSourceChanged,
    AllMarketClose,              //全部市场关闭
    ConfigChanged,
    ExternalEvent,
    OpenStockTradeEventView,
    Net_QueryOrderStatus,
    OnTradeStatusChanged,
    TDF_MayDisconnected,
    TDF_Disconnected;

}
