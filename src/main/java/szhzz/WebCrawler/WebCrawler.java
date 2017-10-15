package szhzz.WebCrawler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//import java.util.stream.Collectors;

/**
 * Created by zhua8634 on 2016-03-28.
 * <p>
 * 每个 WebCrawler Object 有针对网页读取的设定，运行逻辑为：
 * <p>
 * 1. 调用 {@link #parseTable(String, int)} 函数读取网页
 * <p>
 * 2. WebCrawler会先读取指定网页，然后每隔1秒检查 {@link #expectedContentLookupQuery} 内有否出现 {@link
 * #expectedContentCount}，若有则判定为网页已经读取完成
 * <p>
 * 3. WebCrawler读取 {@link #tableExtractionQuery} ，在此之下采用 {@link #headerExtractionQuery}、{@link
 * #rowExtractionQuery} 及 {@link #valueExtractionQuery} 读取表格内容。 此处需注意各 Query 假设以下架构进行Relative搜索：
 * <pre>{@code
 * Web Page
 *   |----> table
 *           |----> Header
 *           |----> Row
 *                   |----> RowValue
 * }</pre>
 */
public class WebCrawler {
    private final String expectedContentLookupQuery;
    private final ExpectedContentCounter expectedContentCount;

    private final String tableExtractionQuery;
    private final String headerExtractionQuery;
    private final String rowExtractionQuery;
    private final String valueExtractionQuery;

    private final String nextPageAnchorQuery;

    /**
     * 记录网页预期表格行数，及最后一页的行数
     */
    public static class ExpectedContentCounter {
        private final int rowPerPage;
        private final int oddPageCount;

        public ExpectedContentCounter(int rowPerPage, int oddPageCount) {
            this.rowPerPage = rowPerPage;
            this.oddPageCount = oddPageCount;
        }

        public int getRowPerPage() {
            return rowPerPage;
        }

        public int getOddPageCount() {
            return oddPageCount;
        }
    }

    public WebCrawler(String expectedContentLookupQuery,
                      ExpectedContentCounter expectedContentCounter,
                      String tableExtractionQuery,
                      String headerExtractionQuery,
                      String rowExtractionQuery,
                      String valueExtractionQuery,
                      String nextPageAnchorQuery) {
        this.expectedContentLookupQuery = expectedContentLookupQuery;
        this.expectedContentCount = expectedContentCounter;

        this.tableExtractionQuery = tableExtractionQuery;
        this.headerExtractionQuery = headerExtractionQuery;
        this.rowExtractionQuery = rowExtractionQuery;
        this.valueExtractionQuery = valueExtractionQuery;

        this.nextPageAnchorQuery = nextPageAnchorQuery;
    }

    public static WebCrawler instanceOfSinaIndustryRank() {
        return new WebCrawler("count(//div[@id='list_wrap']//table//tbody/tr)",
                new ExpectedContentCounter(40, 8),
                "//div[@id='list_wrap']//table",
                "./thead//th/a/text()|./thead//td/a/text()",
                "./tbody//tr",
                ".//th/a/text()|.//th/a/a/text()|.//td/text()",
                "//a[text()='下一页']");
    }

    private List<HtmlAnchor> nextPageAnchor(HtmlPage page) {
        try {
            List<HtmlAnchor> nextPageLinks = (List<HtmlAnchor>) page.getByXPath(nextPageAnchorQuery);
            return nextPageLinks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<WebTableModel> getTablesFromPage(HtmlPage page, int timeoutSecond, boolean isOddPage) throws InterruptedException {
        List<WebTableModel> webTableModels = new ArrayList<>();
        JavaScriptJobManager manager = page.getEnclosingWindow().getJobManager();

        int seconds = 0;
        while (manager.getJobCount() > 0 && seconds < timeoutSecond) {
            Double count = (Double) page.getByXPath(expectedContentLookupQuery).get(0);
            if ((!isOddPage && count >= expectedContentCount.getRowPerPage()) ||
                    (isOddPage && count >= expectedContentCount.getOddPageCount())) {
                break;
            } else {
                TimeUnit.SECONDS.sleep(1);
                seconds++;
            }
        }

        List<HtmlTable> tables = (List<HtmlTable>) page.getByXPath(tableExtractionQuery);
        for (HtmlTable table : tables) {
            WebTableModel wtm = new WebTableModel();

            List<String> headers = table.getByXPath(headerExtractionQuery)
                    .stream().map(node -> ((DomNode) node).getTextContent())
                    .collect(Collectors.toList());
            wtm.setHeader(headers);

            List<DomNode> rows = (List<DomNode>) table.getByXPath(rowExtractionQuery);
            for (DomNode row : rows) {
                List<String> values = row.getByXPath(valueExtractionQuery)
                        .stream()
                        .map(node -> ((DomNode) node).getTextContent())
                        .collect(Collectors.toList());

                if (!values.isEmpty()) {
                    wtm.addValues(values);
                }
            }
            webTableModels.add(wtm);
        }

//        webTableModels.stream().forEach(System.out::println);
        return webTableModels;
    }

    public List<WebTableModel> parseTable(String url, int timeoutSecond) throws IOException {
        try (final WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11)) { //INTERNET_EXPLORER_11

            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());

            HtmlPage page = webClient.getPage(url);
            List<WebTableModel> webTableModels = new ArrayList<>();

            webTableModels.addAll(getTablesFromPage(page, timeoutSecond, false));

            List<HtmlAnchor> nextPages = nextPageAnchor(page);
            while (nextPages != null && !nextPages.isEmpty()) {
                page = nextPages.get(0).click();
                nextPages = nextPageAnchor(page);
                if (nextPages == null) break;

                webTableModels.addAll(getTablesFromPage(page, timeoutSecond, nextPages.isEmpty()));
            }

            return webTableModels;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }


    public static void main(String args[]) throws Exception {
        WebCrawler sinaCrawler = WebCrawler.instanceOfSinaIndustryRank();

        List<WebTableModel> tables = sinaCrawler.parseTable("http://money.finance.sina.com.cn/mkt/#stock_hs_up", 70);

        tables.stream().forEach(System.out::println);
    }
}

