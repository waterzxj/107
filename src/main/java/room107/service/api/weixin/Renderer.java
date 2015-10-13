package room107.service.api.weixin;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Component;

import room107.Constants;
import room107.dao.house.HouseResult;
import room107.datamodel.House;
import room107.datamodel.RentType;
import room107.service.api.weixin.request.AbstractRequest;
import room107.service.api.weixin.request.LocationEventRequest;
import room107.service.api.weixin.request.MenuEventRequest;
import room107.service.api.weixin.request.TextRequest;
import room107.service.api.weixin.response.AbstractResponse;
import room107.service.api.weixin.response.NewsResponse;
import room107.service.api.weixin.response.NewsResponse.News;
import room107.service.api.weixin.response.TextResponse;
import room107.service.api.weixin.response.TextResponse.Macro;
import room107.service.house.search.HouseQuery;
import room107.util.EncryptUtils;
import room107.util.QiniuUploadUtils;
import room107.util.web.ResourceUtils;

/**
 * @author WangXiao
 */
@CommonsLog
@Component
public class Renderer {

    public static final String PARAM_ENCRYPTED_UID = "euid";

    public static final String NEXT_LINE = "\n";

    private static final String URL_MOBILE_LOGO = ResourceUtils
            .getStaticPath("static/image/mobile/wx-logo.png");
    private static final String URL_MOBILE = Constants.SITE_URL + "/m/";
    private static final String URL_USER = URL_MOBILE + "user/";
    
    private static News[] advNews = {
        new News("#有一种家叫宿舍# 微博活动送大礼，智能电视等你拿！","",
                QiniuUploadUtils.getUrl(QiniuUploadUtils.USER_BUCKET, "毕业就有家_logo_0.jpg"),
                "http://mp.weixin.qq.com/s?__biz=MjM5NjExODczNA==&mid=206838020&idx=1&sn=4590f5c7c782bab8219d5a6e9424c3f7")};
    
    public AbstractResponse renderHouseSearchResult(AbstractRequest request,
            HouseQuery query, List<HouseResult> results) {
        return renderHouseSearchResult(request,
                StringUtils.join(query.getKeywords(), ' '), results);
    }

    public AbstractResponse renderHouseSearchResult(AbstractRequest request,
            String keywords, List<HouseResult> results) {
        if (results.isEmpty()) { // should not happen
            log.warn("Render empty results: request=" + request);
            String hint = TextResponse.TEXT_QUERY_NO_RESULT.replace(
                    Macro.SUBSCRIPTION, keywords);
            return new TextResponse(request.getTo(), request.getFrom(), hint);
        }
        /*
         * title
         */
        String title = "";
        if (StringUtils.isNotEmpty(keywords)) {
            if (request instanceof TextRequest
                    || request instanceof MenuEventRequest
                    || request instanceof LocationEventRequest) {
                title = "订阅地址：";
            } else {
                title = "当前搜索：";
            }
            title += keywords;
        }
        return new NewsResponse(request.getTo(), request.getFrom(),
                renderHouseSearchResult(request.getFrom(), title, keywords, results));
    }

    public List<News> renderHouseSearchResult(String openId, String title,
            String keywords, List<HouseResult> results) {
        Validate.notEmpty(results);
        /*
         * init
         */
        String euid = "";
        try {
            euid = EncryptUtils.encrypt(openId);
        } catch (Exception e) {
            log.error("Encrypt failed: uid=" + openId);
        }
        final String TITLE_SPACE = "  ";
        List<News> newsList = new ArrayList<News>();
        /*
         * abstract
         */
        newsList.add(new News(title, "", URL_MOBILE_LOGO, URL_USER + euid));
        /*
         * content
         */
        int i = WeiXinService.MAX_COUNT - 1 - advNews.length;
        boolean needMore = false;
        if (results.size() > i) {
            needMore = true;
            i--;
        }
        for (HouseResult result : results) {
            if (i-- <= 0) {
                break;
            }
            House house = result.getHouse();
            // type
            String type = result.getRentType() == RentType.BY_HOUSE ? "整租"
                    : result.getName();
            Integer price = result.getPrice();
            // title
            title = "【" + house.getCity() + "】" + TITLE_SPACE
                    + house.getPosition() + TITLE_SPACE + type + TITLE_SPACE
                    + (price == null ? "" : (price + "元/月"));
            String desc = "";
            String picUrl = "";
            String url = renderHouseUrl(result.getRentType(), result.getId(),
                    euid);
            newsList.add(new News(title, desc, picUrl, url));
        }
        /*
         * need more news
         */
        if (needMore) {
            if (StringUtils.isEmpty(keywords)) {
                newsList.add(new News("更多房子请访问http://107room.com", "", "", "http://107room.com"));
            } else {
                newsList.add(new News("更多“" + keywords + "”附近的房子请访问http://107room.com", "", "", "http://107room.com"));
            }
        }
        /*
         * advertise news
         */
        for (News news : advNews) {
            newsList.add(news);
        }
        return newsList;
    }

    /**
     * @return URL to access house or room, like: /m/house/id or /m/room/id
     */
    private String renderHouseUrl(RentType rentType, long id, String euid) {
        String type = rentType == RentType.BY_HOUSE ? "house" : "room";
        return URL_MOBILE + type + "/" + id + "?" + PARAM_ENCRYPTED_UID + "="
                + euid;
    }

}
