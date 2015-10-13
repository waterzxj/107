package room107.service.api.weixin.response;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import room107.service.api.weixin.response.NewsResponse.News;

public class NewsJsonResponseTest {

    @Test
    public void test() {
        List<News> newsList = new ArrayList<NewsResponse.News>();
        newsList.add(new News("title", "desc", "picurl", "url"));
        System.out.println(new NewsJsonResponse("to", newsList));
    }

}
