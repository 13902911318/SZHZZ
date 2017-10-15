package szhzz.WebCrawler;

import com.google.common.base.Joiner;
import szhzz.Utils.DawLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhua8634 on 2016-03-28.
 */
public class WebTableModel {
    private static DawLogger logger = DawLogger.getLogger(WebTableModel.class);
    List<String> header;
    private List<List<String>> values;

    public WebTableModel() {
        this.values = new ArrayList<>();
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public int addValues(List<String> values) {
        this.values.add(values);
        return this.values.size();
    }

    public int getRowCount() {
        return this.values.size();
    }

    @Override
    public String toString() {

        String result = "";
        result += "=================================================\r\n";

        result += Joiner.on("\t").join(header) + "\r\n";

        for (List<String> value : values) {
            result += Joiner.on("\t").join(value) + "\r\n";
        }

        result += "=================================================\r\n";
        return result;
    }

    public List<List<String>> getValues() {
        return values;
    }

    /////////////////////////////////////////////////////
}
