package room107.service.api.weixin.response;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import room107.service.api.weixin.response.NewsResponse.News;

/**
 * @author WangXiao
 */
public class NewsJsonResponse extends AbstractJsonResponse {

    @AllArgsConstructor
    @ToString
    public static class JsonNews {
        public JsonArticle[] articles;
    }

    @AllArgsConstructor
    @ToString
    public static class JsonArticle {
        public String title, description, picurl, url;
    }

    @Getter
    private JsonNews news;

    public NewsJsonResponse(String to, List<News> newsList) {
        super("news", to);
        List<JsonArticle> articles = new ArrayList<JsonArticle>(newsList.size());
        for (News news : newsList) {
            articles.add(new JsonArticle(news.title, news.desc, news.picUrl,
                    news.url));
        }
        news = new JsonNews(articles.toArray(new JsonArticle[articles.size()]));
    }

}
