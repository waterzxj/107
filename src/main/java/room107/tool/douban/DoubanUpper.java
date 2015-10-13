package room107.tool.douban;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang.Validate;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import room107.datamodel.DoubanPoster;
import room107.util.EmailUtils;
import room107.util.JsonUtils;
import room107.util.StringUtils;

/**
 * @author WangXiao
 */
@RequiredArgsConstructor
public class DoubanUpper {

    static final String UA = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11";

    static final String REF = "http://www.douban.com/group/fangzi/?ref=sidebar";

    @NonNull
    private DoubanPoster doubanPoster;

    @Setter
    private String ocrPath = "C:/Tesseract-OCR/tesseract.exe";

    private int commentCount, recognizeCount;

    private int postRetry = 10;

    private int recognizeRetry = 3;

    private boolean autoRemoveComment = true;

    private CodeRecognizer recognizer = new CodeRecognizer(ocrPath);;

    private ThreadLocal<AtomicInteger> commentIndex = new ThreadLocal<AtomicInteger>() {
        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };

    public synchronized void up(String pageUrl) throws Exception {
        /*
         * normalize
         */
        pageUrl = StringUtils.trimToNull(pageUrl);
        Validate.notNull(pageUrl, "null pageUrl");
        int i = pageUrl.lastIndexOf('?');
        if (i > 0) {
            pageUrl = pageUrl.substring(0, i);
        }
        if (!pageUrl.endsWith("/")) {
            pageUrl += "/";
        }
        pageUrl = pageUrl.startsWith("http://") ? pageUrl
                : ("http://" + pageUrl);
        System.out.println("Up: " + pageUrl + ", name="
                + doubanPoster.getName() + ", time=" + new Date());
        /*
         * id
         */
        Document doc = getConnection(pageUrl).get();
        String id = doc.select(".nav-user-account span").first().text();
        try {
            id = id.substring(0, id.length() - 3);
        } catch (Exception e) {
            System.err.println("Parse ID failed: " + id);
        }
        /*
         * get token
         */
        doc = getCommentPage(pageUrl);
        Element e = doc.select(".comment-form input[name=ck]").first();
        String ck = e == null ? "null" : e.attr("value");
        /*
         * post
         */
        int retry = postRetry;
        Map<String, String> data = new HashMap<String, String>();
        while (retry-- > 0) {
            String commentUrl = getCommentUrl(pageUrl);
            // System.out.println(new Date());
            // System.out.println("Comment URL: " + commentUrl);
            data.put("ck", ck);
            data.put("rv_comment", getComment());
            // System.out.println("\tComment param: " + data);
            if (data.containsKey("captcha-solution")) {
                recognizeCount++;
            }
            getConnection(commentUrl).data(data).post();
            doc = getCommentPage(pageUrl);
            e = doc.select("input[name=captcha-id]").first();
            if (e == null) { // OK
                commentCount++;
                // System.out.println("\tComment successfully!");
                data.remove("captcha-solution");
                break;
            } else { // captcha
                String captchaId = e.attr("value");
                String captchaUrl = getCaptchaUrl(captchaId);
                data.put("captcha-id", captchaId);
                // System.out.println("\tNeed captcha: captchaUrl=" +
                // captchaUrl);
                int r = recognizeRetry;
                while (r-- > 0) {
                    try {
                        String code = recognizer.recognize(captchaUrl);
                        // System.out.println("\tGuess captcha: " + code);
                        data.put("captcha-solution", code);
                        if (code != null) {
                            break;
                        } else {
                            data.put("captcha-solution", "normal");
                        }
                    } catch (Exception e2) {
                        System.err.println("\t guess exception: "
                                + e2.getMessage());
                    }
                }
                Thread.sleep(100);
            }
        }
        /*
         * remove
         */
        if (autoRemoveComment) {
            Elements es = doc.select(".comment-item");
            int n = es.size() - 1;
            if (es != null) {
                for (i = 0; i < n; i++) {
                    e = es.get(i);
                    String commenter = e.select(".reply-doc h4 a").text();
                    if (!id.equals(commenter)) {
                        continue; // other's comment
                    }
                    String removeUrl = getRemoveUrl(pageUrl);
                    System.out.println("Remove URL: " + removeUrl);
                    data.put("cid", e.attr("id"));
                    // System.out.println("\tRemove param: " + data);
                    getConnection(removeUrl).data(data).method(Method.POST)
                            .ignoreContentType(true).execute();
                }
            }
        }
        // statistic
        if (recognizeCount > 0) {
            // System.out.println("Correct rate: " + (float) 100.0 *
            // commentCount
            // / recognizeCount + "%");
        }
    }

    @SuppressWarnings("unchecked")
    private Connection getConnection(String url) {
        Map<String, String> cookies;
        try {
            cookies = JsonUtils.fromJson(doubanPoster.getCookie(),
                    Map.class);
        } catch (Exception e) {
            String message = "Invalid cookie format: "
                    + doubanPoster.getCookie() + ", name="
                    + doubanPoster.getName();
            EmailUtils.sendAdminMail("Douban Upper", message);
            throw new RuntimeException(message);
        }
        return Jsoup.connect(url).userAgent(UA).referrer(REF).cookies(cookies)
                .timeout(10000);
    }

    private String getCommentUrl(String pageUrl) {
        return pageUrl + "add_comment#last";
    }

    private String getRemoveUrl(String pageUrl) {
        String result = pageUrl;
        result = StringUtils.replace(pageUrl, "douban.com/", "douban.com/j/");
        return result + "remove_comment";
    }

    private Document getCommentPage(String pageUrl) throws IOException {
        Document doc = getConnection(pageUrl).get();
        Element e = doc.select(".paginator").first();
        if (e != null) { // paging
            e = e.select(".paginator > a").last();
            if (e != null) {
                doc = getConnection(e.attr("href")).get();
            }
        }
        return doc;
    }

    private String getCaptchaUrl(String captchaId) {
        return "http://www.douban.com/misc/captcha?id=" + captchaId;
    }

    private String getComment() {
        AtomicInteger atomicInteger = commentIndex.get();
        int n = atomicInteger.getAndIncrement() % 5;
        StringBuilder b = new StringBuilder("mark");
        for (int i = 0; i < n; i++) {
            b.append('k');
        }
        b.append('~');
        return b.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        // Validate.isTrue(args.length >= 2,
        // "Usage: <tesseract path> <url1,url2,...> [intervalSec]");
        // // init
        // String ocrPath = StringUtils.trimToNull(args[0]);
        // String[] pageUrls = StringUtils.split(args[1], ',');
        // String intervalSec = args.length > 2 ? args[2] : null;
        // // run
        // for (String pageUrl : pageUrls) {
        // // init
        // DoubanUpper upper = new DoubanUpper(pageUrl);
        // upper.setOcrPath(ocrPath);
        // if (intervalSec != null) {
        // upper.setIntervalSec(Integer.parseInt(intervalSec));
        // }
        // // sleep randomly
        // Thread.sleep((RandomUtils.nextInt(20) + 10) * 1000);
        // new Thread(upper).start();
        // }
    }

}
