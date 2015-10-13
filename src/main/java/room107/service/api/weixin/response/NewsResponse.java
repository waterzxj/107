package room107.service.api.weixin.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.apachecommons.CommonsLog;

import org.dom4j.Document;
import org.dom4j.Element;

import room107.util.StringUtils;

/**
 * @author WangXiao
 */
@CommonsLog
public class NewsResponse extends AbstractResponse {

    @ToString
    @AllArgsConstructor
    public static class News {
        public String title, desc, picUrl, url;
    }

    private List<News> newsList;

    public NewsResponse(String from, String to, List<News> newsList) {
        super(from, to);
        this.newsList = newsList;
    }

    @Override
    protected String getMessageType() {
        return "news";
    }

    @Override
    protected Document toXml() {
        if (log.isDebugEnabled()) {
            log.debug("News: " + newsList);
        }
        Document result = super.toXml();
        Element root = result.getRootElement();
        root.addElement("ArticleCount")
                .setText(String.valueOf(newsList.size()));
        Element articles = root.addElement("Articles");
        if (newsList != null) {
            for (News news : newsList) {
                Element item = articles.addElement("item");
                if (StringUtils.isNotEmpty(news.title)) {
                    item.addElement("Title").addCDATA(news.title);
                }
                if (StringUtils.isNotEmpty(news.desc)) {
                    item.addElement("Description").addCDATA(news.desc);
                }
                if (StringUtils.isNotEmpty(news.picUrl)) {
                    item.addElement("PicUrl").addCDATA(news.picUrl);
                }
                if (StringUtils.isNotEmpty(news.url)) {
                    item.addElement("Url").addCDATA(news.url);
                }
            }
        }
        return result;
    }

}
