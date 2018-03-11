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
    MarketDataEvent,
    TDF_Disconnected,  //depreciate
    TradeProxyConnect,
    ////////////////////// for Quantitative Analyses ////////////////////////////////////
    AutoTradeEvent,
    StockWinEvent,
    MarketClientEvent,
    NotDefined,
    ConfigFileChanged,
    BandContentChanged,
    BandNameChanged,
    BandReloaded,
    dateChanged,
    Bar_AndToBand,
    Bar_ReplaceToBand,
    Bar_OrToBand,
    Bar_SubsTractFromBand,
    DW_AndToBand,
    DW_ReplaceToBand,
    DW_OrToBand,
    DW_SubsTractFromBand,
    Event3060_started,
    Event3060_finished,
    SynchronizeStockCodeChanged,
    SynchronizeBandCodeChanged,
    CallUpdateFromStockHistory,
    OneStockOptimised,
    AllStockOptimised,
    MonyFlowFindStock,
    TradingRecordReverted,
    TransactionAccountNameChanged,
    TransactionStockCodeChanged,
    TransactionDateChanged,
    TransactionAskSyncRow,
    OpenDiary,
    StockCalculateProcessing,
    ScanWebStart,
    ScanWebFinish,
    MaintainDatabase,
    CheckTime,
    FindAccountTreeNode,
    CreateTradeCfg,
    TDF_Downlaod,
    CloseOperation,
    PublicEvent;
    ;
}
