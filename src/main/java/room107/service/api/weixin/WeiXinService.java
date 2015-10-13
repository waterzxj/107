package room107.service.api.weixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import room107.dao.IUserDao;
import room107.dao.IWxSubscribeDao;
import room107.dao.IWxUserDao;
import room107.dao.house.HouseResult;
import room107.dao.house.IHouseDao;
import room107.dao.house.RoomResult;
import room107.datamodel.District;
import room107.datamodel.House;
import room107.datamodel.Location;
import room107.datamodel.LogType;
import room107.datamodel.Platform;
import room107.datamodel.Poi;
import room107.datamodel.RentStatus;
import room107.datamodel.RentType;
import room107.datamodel.Room;
import room107.datamodel.RoomType;
import room107.datamodel.User;
import room107.datamodel.UserType;
import room107.datamodel.WxSubscribe;
import room107.datamodel.WxUser;
import room107.datamodel.WxUserBasicInfo;
import room107.service.api.weixin.WeiXinNotifier.NotifyMessage;
import room107.service.api.weixin.admin.WeiXinAdminService;
import room107.service.api.weixin.request.AbstractRequest;
import room107.service.api.weixin.request.LocationEventRequest;
import room107.service.api.weixin.request.LocationRequest;
import room107.service.api.weixin.request.MenuEventRequest;
import room107.service.api.weixin.request.MenuEventRequest.EventKey;
import room107.service.api.weixin.request.ScanEventRequest;
import room107.service.api.weixin.request.ScanEventRequest.SceneException;
import room107.service.api.weixin.request.SubscribeEventRequest;
import room107.service.api.weixin.request.TextRequest;
import room107.service.api.weixin.request.UnsubscribeEventRequest;
import room107.service.api.weixin.request.VoiceRequest;
import room107.service.api.weixin.response.AbstractResponse;
import room107.service.api.weixin.response.NewsJsonResponse;
import room107.service.api.weixin.response.NewsResponse.News;
import room107.service.api.weixin.response.TextResponse;
import room107.service.api.weixin.response.TextResponse.Macro;
import room107.service.house.IHouseService;
import room107.service.house.IPoiService;
import room107.service.house.search.HouseQuery;
import room107.service.house.search.IHouseSearcher;
import room107.service.house.search.line.LineQuery;
import room107.service.house.search.position.IPositionSearcher;
import room107.service.location.ILocator;
import room107.service.message.MessageService;
import room107.util.BaiduMapUtils;
import room107.util.CoordinateUtils;
import room107.util.GeographyUtils;
import room107.util.HouseUtils;
import room107.util.JsonUtils;
import room107.util.QiniuUploadUtils;
import room107.util.StringUtils;
import room107.util.UserBehaviorLog;
import room107.util.UserStatLog;
import room107.util.WebUtils;
import room107.util.collection.AppendableLinkedHashMap;
import room107.wechat.AccessTokenManager;
import room107.wechat.Constants;

/**
 * Assert all requests are non-null.
 * <p>
 * Assert {@link WxUser} has been created (by calling
 * {@link #createIfNotExist(String)}) before any handle operation.
 * 
 * @author yanghao
 */
@CommonsLog
@Service
public class WeiXinService {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IHouseService houseService;
    
    @Autowired
    private IHouseDao houseDao;

    @Autowired
    private IHouseSearcher houseSearcher;

    @Autowired
    private Renderer renderer;

    @Autowired
    private IPoiService poiService;

    @Autowired
    private IWxUserDao wxUserDao;

    @Autowired
    private IWxSubscribeDao wxSubscribeDao;

    @Autowired
    private WeiXinAdminService weiXinAdminService;

    @Qualifier(value = "cachedLocator")
    @Autowired
    private ILocator locator;

    @Autowired
    private WeiXinNotifier weiXinNotifier;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AccessTokenManager appTokenManager;

    @Autowired
    private WechatBindManager wechatBindManager;
        
    private List<BasicSubscriberInfo> subscribes = null;

    private static final long SUBSCRIBE_UPDATE_INTERVAL = 1000L * 60 * 20;

    public static final Log WEIXIN_LOG = LogFactory
            .getLog("room107.biz.weixin");

    /**
     * Left 1 for abstract.
     */
    public static final int MAX_COUNT = 10;

    /**
     * @throws Exception
     *             when authentication failed
     */
    public void auth(HttpServletRequest request) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug(Arrays.toString(new String[] {
                    request.getParameter("timestamp"),
                    request.getParameter("nonce"),
                    request.getParameter("signature") }));
        }
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String expSignature = request.getParameter("signature");
        String[] p = new String[] { Constants.TOKEN, timestamp, nonce };
        Validate.notNull(timestamp, "Null timestamp");
        Validate.notNull(nonce, "Null nonce");
        Validate.notNull(expSignature, "Null signature");
        /*
         * sort
         */
        Arrays.sort(p);
        /*
         * SHA1
         */
        String signature = StringUtils.join(p);
        signature = DigestUtils.shaHex(signature);
        Validate.isTrue(expSignature.equalsIgnoreCase(signature),
                "Validate failed: signature=" + signature + ", expected="
                        + expSignature + ", params=" + Arrays.toString(p));
    }

    /**
     * @param openId
     *            non-null
     */
    public void createIfNotExist(String openId) {
        Validate.notNull(openId);
        WxUser user = wxUserDao.get(WxUser.class, openId);
        if (user == null) { // create
            Date date = new Date();
            user = new WxUser();
            user.setSessionStatus(SessionStatus.NORMAL.ordinal());
            user.setOpenId(openId);
            user.setCreatedTime(date);
            user.setModifiedTime(date);
        }
        user.setActionTime(new Date());
        wxUserDao.saveOrUpdate(user);
    }

    public String handleSubscribeEventRequest(SubscribeEventRequest request) {
        return handleScanEventRequest(request);
    }

    /**
     * Bind user name with openId.
     */
    public String handleScanEventRequest(ScanEventRequest request) {
        String openId = request.getFrom();
        WxUser wxUser = wxUserDao.get(WxUser.class, openId);
        updateWechatBasicInfo(wxUser);
        try {
            //login user, bind user and wxUser.
            long userId = request.getScene().getUserId();
            User user = userDao.getUser(userId);
            Validate.notNull(user, "Null user: userId=" + userId);
            bind(wxUser, user);
            wxUserDao.saveOrUpdate(wxUser);
            userDao.update(user);
            wechatBindManager.release(user.getUsername(), user);
            UserStatLog.log(LogType.subscribe, wxUser.getUsername(), openId);
        } catch (SceneException e) {
            // unlogin user, can not bind.
            wxUserDao.saveOrUpdate(wxUser);
            UserStatLog.log(LogType.subscribe, wxUser.getUsername(),
                    openId, JsonUtils.toJson("channel", request.getScene().getValue()));
        }
        String content = TextResponse.BIND;
        if (StringUtils.isNotEmpty(wxUser.getNickname())) {
            content = String.format(TextResponse.CALLER, wxUser.getNickname()) + content;
        }
        return new TextResponse(request.getTo(), request.getFrom(), content).toString();
    }

    public String handleUnsubscribeEventRequest(UnsubscribeEventRequest request) {
        String openId = request.getFrom();
        WxUser wxUser = wxUserDao.get(WxUser.class, openId);
        if (wxUser != null) {
            wxUserDao.delete(wxUser);
            wxSubscribeDao.deleteByOpenId(openId);
        }
        UserBehaviorLog.WEIXIN.info("Unsubscribe: openId=" + openId);
        return null; // need to do nothing
    }

    public String handleMenuEventRequest(MenuEventRequest request) {
        EventKey eventKey = EventKey.valueOf(request.getEventKey());
        final String openId = request.getFrom();
        WxUser user = wxUserDao.get(WxUser.class, openId);
        SessionStatus sessionStatus = SessionStatus.values()[user.getSessionStatus()];
        user.setSessionStatus(SessionStatus.NORMAL.ordinal());
        AbstractResponse resultResponse = null;
        if (eventKey == EventKey.HELP) {
            resultResponse = new TextResponse(request.getTo(), openId,
                    TextResponse.MENU_HELP);
        } else if (eventKey == EventKey.UNSUBSCRIBE) {
            List<String> keywords = getSubscription(openId);
            wxSubscribeDao.deleteByOpenId(openId);
            if (CollectionUtils.isNotEmpty(keywords)) {
                String keyword = StringUtils.join(keywords, ' ');
                resultResponse = new TextResponse(request.getTo(), openId,
                        String.format(TextResponse.MENU_UNSUBSCRIBE, keyword));
            } else {
                resultResponse = new TextResponse(request.getTo(), openId,
                        TextResponse.MENU_UNSUBSCRIBE_EMPTY);
            }
        } else if (eventKey == EventKey.HOUSE_OPEN || eventKey == EventKey.HOUSE_CLOSE
                || eventKey == EventKey.HOUSE_VIEW) {
            resultResponse = handleHouseEvent(user, eventKey, openId, request);
        } else if (eventKey == EventKey.SUBSCRIBE) {
            if (sessionStatus == SessionStatus.CONFIRM_HOUSE_MANAGE) {
                resultResponse = new TextResponse(request.getTo(), openId,
                        TextResponse.MENU_HOUSE_MANAGE);
            } else {
                resultResponse = handleSubscribeEvent(user, openId, request, sessionStatus);
            }
        }
        wxUserDao.update(user);
        return resultResponse.toString();
    }
    
    public String handleTextRequest(TextRequest request) {
        String text = StringUtils.trimToNull(request.getContent());
        if (text == null) {
            return new TextResponse(request.getTo(), request.getFrom(),
                    TextResponse.TEXT_QUERY_INVALID).toString();
        }
        /*
         * check admin
         */
        String adminResponse = weiXinAdminService.handle(request);
        if (adminResponse != null) {
            return new TextResponse(request.getTo(), request.getFrom(),
                    adminResponse).toString();
        }
        String openId = request.getFrom();
        WxUser user = wxUserDao.get(WxUser.class, openId);
        int sessionStatus = user.getSessionStatus();
        /*
         * handle house manage event
         */
        if (sessionStatus == SessionStatus.UPDATE_HOUSE_STATUS.ordinal()) {
            List<House> userHouses = houseDao.getHouses(user.getUsername());
            if (userHouses.isEmpty()) {
                return new TextResponse(request.getTo(), openId,
                      TextResponse.HOUSE_VIEW_EMPTY).toString();
            } else {
                House house = userHouses.get(0);
                List<Room> userRooms = houseDao.getRooms(house.getId());
                int i = 1;
                for (Room room : userRooms ) {
                    if (room.isRentable()) {
                        if (text.equals(String.valueOf(i))) {
                            String status = "";
                            if (room.getStatus() == RentStatus.OPEN.ordinal()) {
                                room.setStatus(RentStatus.CLOSED.ordinal());
                                status = "已关闭。";
                            } else if (room.getStatus() == RentStatus.CLOSED.ordinal()) {
                                room.setStatus(RentStatus.OPEN.ordinal());
                                status = "已开放。";
                            }
                            houseDao.updateRoom(room);
                            String hint = "“房间" + Integer.parseInt(text) + "：" + room.getName() + "，"
                                    + room.getPrice() + "元/月”" + "\n" + status;
                            return new TextResponse(request.getTo(), openId, hint).toString();
                        }
                        i ++;
                    }
                }
            }
        }
        HouseQuery query = new HouseQuery(text);
        /*
         * update subscription
         */
        wxSubscribeDao.deleteByOpenId(request.getFrom());
        Date date = new Date();
        for (final String keyword : query.getKeywords()) {
            try {
                WxSubscribe subscribe = new WxSubscribe(keyword, openId, date);
                String s = District.getDistrictName(keyword);
                if (s != null) { // district
                    subscribe.setFeature(s);
                } else {
                    s = LineQuery.normalize(keyword);
                    if (s != null) { // line
                        subscribe.setFeature(s);
                    } else { // location
                        Location location = locator.getLocation(keyword);
                        if (location != null) {
                            subscribe.setLocationX(location.getX());
                            subscribe.setLocationY(location.getY());
                        }
                    }
                }
                wxSubscribeDao.save(subscribe);
            } catch (Exception e) {
                log.error("Subscribe failed: keyword=" + keyword + ", openId="
                        + request.getFrom());
            }
        }
        /*
         * update refresh time
         */
        user.setRefreshTime(date);
        wxUserDao.update(user);
        /*
         * search
         */
        List<HouseResult> results = houseSearcher.search(query, MAX_COUNT)
                .getHouseResults();
        if (results.isEmpty()) {
            String hint = TextResponse.TEXT_QUERY_NO_RESULT.replace(
                    Macro.SUBSCRIPTION, text);
            return new TextResponse(request.getTo(), request.getFrom(), hint)
                    .toString();
        } else {
            return renderer.renderHouseSearchResult(request, query, results).toString();
        }
    }

    public String handleLocationEventRequest(LocationEventRequest request) {
        String openId = request.getFrom();
        Date date = new Date();
        Location location = CoordinateUtils.convertGCJ2Baidu(request.toLocation());
        try {
            WxUser wxUser = wxUserDao.get(WxUser.class, openId);
            UserStatLog.log(LogType.location, date, wxUser.getUsername(), openId, null, Platform.wechat, null, null, location.getX(), location.getY(), null);
            User user = userDao.getUser(wxUser.getUsername());
            if ((user == null || user.getType() == UserType.TENANT.ordinal())
                    && System.currentTimeMillis() - wxUser.getCreatedTime().getTime() < 1000L * 60 * 5) {
                List<WxSubscribe> wxSubscribes = wxSubscribeDao.getByOpenId(openId);
                if (CollectionUtils.isEmpty(wxSubscribes)) {
                    // update subscription
                    wxUser.setRefreshTime(date);
                    wxUserDao.update(wxUser);
                    String address = BaiduMapUtils.getAddress(location.getX(), location.getY());
                    //first location request, subscribe current location.
                    WxSubscribe subscribe = new WxSubscribe(null, openId, date);
                    subscribe.setLocationX(location.getX());
                    subscribe.setLocationY(location.getY());
                    subscribe.setPosition(address);
                    wxSubscribeDao.save(subscribe);
                    HouseQuery query = new HouseQuery(location);
                    List<HouseResult> results = houseSearcher.search(query, MAX_COUNT)
                            .getHouseResults();
                    if (results.isEmpty()) {
                        String hint = TextResponse.LOCATION_QUERY_NO_RESULT.replace(
                                Macro.QUERY, StringUtils.trimToEmpty(address));
                        return new TextResponse(request.getTo(), request.getFrom(), hint)
                                .toString();
                    } else {
                        return renderer.renderHouseSearchResult(request,
                                address, results).toString();
                    }
                }
            }
        } catch(Exception e) {
            log.error("subscribe location (" + location.getX() + "," + location.getY()
                    + ") failed, openId = " + openId, e);
        }
        return "";
    }
    
    public String handleLocationRequest(LocationRequest request) {
        Location location = CoordinateUtils.convertGCJ2Baidu(request.toLocation());
        HouseQuery query = new HouseQuery(location);
        List<HouseResult> results = houseSearcher.search(query, MAX_COUNT).getHouseResults();
        if (results.isEmpty()) {
            String hint = TextResponse.LOCATION_QUERY_NO_RESULT.replace(
                    Macro.QUERY, StringUtils.trimToEmpty(request.getLabel()));
            return new TextResponse(request.getTo(), request.getFrom(), hint)
                    .toString();
        } else {
            return renderer.renderHouseSearchResult(request,
                    request.getLabel(), results).toString();
        }
    }

    public String handleVoiceRequest(VoiceRequest request) {
        String text = StringUtils.trimToNull(request.getRecognition());
        if (text == null) {
            return new TextResponse(request.getTo(), request.getFrom(),
                    TextResponse.VOICE_QUERY_INVALID).toString();
        }
        HouseQuery query = new HouseQuery(text);
        List<HouseResult> results = houseSearcher.search(query, MAX_COUNT)
                .getHouseResults();
        if (results.isEmpty()) {
            String hint = TextResponse.VOICE_QUERY_NO_RESULT.replace(
                    Macro.QUERY, request.getRecognition());
            return new TextResponse(request.getTo(), request.getFrom(), hint)
                    .toString();
        } else {
            return renderer.renderHouseSearchResult(request, query, results).toString();
        }
    }

    public String handleUnsupportedMessage(AbstractRequest request) {
        log.info("Unsupported message: " + request.toXml());
        return new TextResponse(request.getTo(), request.getFrom(),
                TextResponse.UNSUPPORTED_REQUEST).toString();
    }
    
    public WxUser updateWechatBasicInfo(WxUser wxUser) {
        log.info("updateWechatBasicInfo: openId=" + wxUser.getOpenId());
        try {
            String url = String
                    .format("https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN",
                            appTokenManager.getAccessToken(),
                            wxUser.getOpenId());
            WxUserBasicInfo basicInfo = WebUtils.getJson(url,
                    WxUserBasicInfo.class);
            if (basicInfo != null && basicInfo.getSubscribe() != 0) {
                if (basicInfo.getSex() != 0) {
                    wxUser.setGender(basicInfo.getSex());
                }
                if (!StringUtils.isEmpty(basicInfo.getHeadimgurl())) {
                    String key = QiniuUploadUtils.uploadFavicon(wxUser.getUsername(),
                            basicInfo.getHeadimgurl());
                    wxUser.setFaviconUrl(key);
                }
                String nickname = StringUtils
                        .filterUtf8Mb4(basicInfo.getNickname());
                if (!StringUtils.isEmpty(nickname)) {
                    wxUser.setNickname(nickname);
                }
                UserBehaviorLog.WEIXIN.info("updateWechatBasicInfo: username="
                        + wxUser.getUsername() + ", openId=" + wxUser.getOpenId()
                        + "nickname=" + wxUser.getNickname()
                        + ", gender=" + wxUser.getGender() + ", favicon="
                        + wxUser.getFaviconUrl());
            }
        } catch (Exception e) {
            log.error("Update basic info from wechat failed: openId="
                    + wxUser.getOpenId(), e);
        }
        return wxUser;
    }
    
    public WxUser bind(String openId, String username) {
        if (openId == null || username == null) return null;
        WxUser wxUser = wxUserDao.get(WxUser.class, openId);
        if (wxUser == null) return null;
        User user = userDao.getUser(username);
        if (user == null) return null;
        bind(wxUser, user);
        wxUserDao.saveOrUpdate(wxUser);
        userDao.update(user);
        return wxUser;
    }

    public void bind(WxUser wxUser, User user) {
        if (wxUser == null || user == null) {
            return ;
        }
        if (wxUser == null || !user.getUsername().equals(wxUser.getUsername())) {
            WxUser other = wxUserDao.getByUsername(user.getUsername());
            if (other != null) {
                wxUserDao.delete(other);
                UserBehaviorLog.WEIXIN
                        .info("Unbind: username=" + other.getUsername()
                                + ", openId=" + other.getOpenId());
            }
        }
        wxUser.setUsername(user.getUsername());
        
        if (StringUtils.isNotEmpty(wxUser.getNickname())) {
            user.setName(wxUser.getNickname());
        }
        if (StringUtils.isNotEmpty(wxUser.getFaviconUrl())) {
            user.setFaviconUrl(wxUser.getFaviconUrl());
        }
        if (wxUser.getGender() != null && wxUser.getGender() != 0) {
            user.setGender(wxUser.getGender());
        }
        
        UserBehaviorLog.WEIXIN.info("Bind: username=" + user.getUsername() + ", openId="
                + wxUser.getOpenId());
    }

    /**
     * @return subscribed positions
     */
    public List<String> getSubscription(String openId) {
        if (openId == null) {
            return Collections.emptyList();
        }
        List<WxSubscribe> subscribes = wxSubscribeDao.getByOpenId(openId);
        if (subscribes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>(subscribes.size());
        for (WxSubscribe s : subscribes) {
            result.add(s.getPosition());
        }
        return result;
    }
    
    private TextResponse handleHouseEvent(WxUser user, EventKey houseEvent,
            String openId, MenuEventRequest request) {
        if (user == null || StringUtils.isEmpty(user.getUsername())) {
            return new TextResponse(request.getTo(), openId,
                    TextResponse.HOUSE_VIEW_NULL);
        }
        List<House> userHouses = houseDao.getHouses(user.getUsername());
        if (userHouses.isEmpty()) {
            return new TextResponse(request.getTo(), openId,
                    TextResponse.HOUSE_VIEW_EMPTY);
        }
        
        String header = "";
        String content = "";
        String footer = "";
        
        int houseStatus = 0;
        String openStatus = "开放中";
        String closeStatus = "关闭中";
        
        //handle house info
        House house = userHouses.get(0);
        if (houseEvent == EventKey.HOUSE_OPEN) {
            if (house.getRentType() == RentType.BY_HOUSE.ordinal()) {
                houseService.updateHouseStatus(house.getId(),
                        RentStatus.OPEN.ordinal());
            } else {
                houseDao.openAll(house.getId());
            }
            houseStatus = 2;
            openStatus = "已开放";
            header = "操作成功。最新房子状态为：\n";
        } else if (houseEvent == EventKey.HOUSE_CLOSE) {
            if (house.getRentType() == RentType.BY_HOUSE.ordinal()) {
                houseService.updateHouseStatus(house.getId(),
                        RentStatus.CLOSED.ordinal());
            } else {
                houseDao.closeAll(house.getId());
            }
            houseStatus = -2;
            closeStatus = "已关闭";
            header = "操作成功。最新房子状态为：\n";
        } else if (houseEvent == EventKey.HOUSE_VIEW) {
            houseStatus = house.getStatus() == RentStatus.OPEN.ordinal() ? 1 : -1;
            header = "";
        }
        if (house.getRentType() == RentType.BY_HOUSE.ordinal()) {
            //by house
            String status = houseStatus > 0 ? openStatus : closeStatus;
            content = house.getCity() + "区" + house.getPosition() + "，" +
                    house.getPrice() + "元/月，" + status;
        } else {
            //by room
            //handle room info
            List<Room> userRooms = houseDao.getRooms(house.getId());
            int rentableCnt = 1;
            String singleContent = house.getCity() + "区" + house.getPosition() + "，";
            String multiContent = house.getCity() + "区" + house.getPosition() + "\n";
            for (Room room : userRooms) {
                if (room.isRentable()) {
                    String status;
                    if (houseStatus > 1 || (houseStatus > 0 && room.getStatus() == RentStatus.OPEN.ordinal())) {
                        status = openStatus;
                    } else {
                        status = closeStatus;
                    }
                    singleContent += room.getName() + "，" + room.getPrice() + "元/月，"
                            + status + "\n";
                    multiContent += "房间" + rentableCnt + "：" + room.getName()
                            + "，" + room.getPrice() + "元/月，\n" + status + "\n";
                    rentableCnt++;
                }
            }
            if (rentableCnt == 1) {
                //empty room
                return new TextResponse(request.getTo(), openId,
                        TextResponse.HOUSE_VIEW_EMPTY);
            } else if (rentableCnt == 2) {
                //only one rentable bedroom
                content = singleContent;
            } else {
                //multi rentable bedrooms
                content = multiContent;
                if (houseEvent == EventKey.HOUSE_CLOSE) {
                    footer = "回复相应数字可以开放该房间。";
                } else if (houseEvent == EventKey.HOUSE_OPEN) {
                    footer = "回复相应数字可以关闭该房间。";
                } else if (houseEvent == EventKey.HOUSE_VIEW) {
                    footer = "回复相应数字可以开放或者关闭该房间。";
                }
                user.setSessionStatus(SessionStatus.UPDATE_HOUSE_STATUS.ordinal());
            }
        }
        
        String hint = header + content + footer;
        return new TextResponse(request.getTo(), openId, hint);
    }
    
    private AbstractResponse handleSubscribeEvent(WxUser user, String openId,
            MenuEventRequest request, SessionStatus sessionStatus) {
        Date lastRefreshTime = null;
        Date date = new Date();
        lastRefreshTime = user.getRefreshTime();
        user.setRefreshTime(date);
        if (log.isDebugEnabled()) {
            log.debug("lastRefreshTime: " + lastRefreshTime);
        }
        // get subscription
        List<String> subscriptions = getSubscription(openId);
        if (subscriptions.isEmpty()) { // not subscribe
            return new TextResponse(request.getTo(), openId,
                    TextResponse.MENU_REFRESH_NOT_SUBSCRIBE);
        }
        /*
         * search
         */
        HouseQuery query = new HouseQuery(
                subscriptions.toArray(new String[subscriptions.size()]));
        query.setModifiedTimeFrom(lastRefreshTime);
        List<HouseResult> results = houseSearcher.search(query, MAX_COUNT)
                .getHouseResults();
        if (results.isEmpty()) { // no result
            String hint = "";
            if (sessionStatus == SessionStatus.NORMAL
                    || sessionStatus == SessionStatus.UPDATE_HOUSE_STATUS) {
                hint = TextResponse.MENU_REFRESH_NO_RESULT.replace(
                        Macro.SUBSCRIPTION,
                        StringUtils.join(subscriptions, ' '));
            } else if (sessionStatus == SessionStatus.CONFIRM_RESUBSCRIBE) {
                hint = TextResponse.MENU_REFRESH_RESUBSCRIBE;
            }
            return new TextResponse(request.getTo(), openId, hint);
        } else {
            return renderer.renderHouseSearchResult(request, query, results);
        }
    }

    private void updateSubscribers() {
        try {
            long start = System.currentTimeMillis();
            List<BasicSubscriberInfo> newSubscribes = wxSubscribeDao
                    .getAllValidSubscribe();
            subscribes = newSubscribes;
            log.info("update wx subscribes, total count " + subscribes.size()
                    + ", elapse " + (System.currentTimeMillis() - start) + " ms.");
        } catch(Exception e) {
            log.error(e, e);
        }
    }
    
    @PostConstruct
    public void init() {
        updateSubscribers();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                updateSubscribers();
            }
        }, SUBSCRIBE_UPDATE_INTERVAL, SUBSCRIBE_UPDATE_INTERVAL);
    }

    public List<BasicSubscriberInfo> getNearistSubscribers(String position,
            int radius) {
        long start = System.currentTimeMillis();
        List<BasicSubscriberInfo> nearistSubscribers = new ArrayList<BasicSubscriberInfo>();
        try {
            Location location = locator.getLocation(position);
            if (location != null) {
                for (BasicSubscriberInfo subscriber : subscribes) {
                    double distance = GeographyUtils.getDistance(
                            subscriber.x, subscriber.y, location.getX(), location.getY());
                    if (distance > radius) continue;
                    BasicSubscriberInfo valid = new BasicSubscriberInfo(subscriber);
                    valid.distance = distance;
                    nearistSubscribers.add(valid);
                }
                log.info("search subscriber, total " + nearistSubscribers.size()
                        + " users, position = " + position);
                Collections.sort(nearistSubscribers,
                        new Comparator<BasicSubscriberInfo>() {
                            @Override
                            public int compare(BasicSubscriberInfo s1,
                                    BasicSubscriberInfo s2) {
                                if (s1.distance < s2.distance)
                                    return -1;
                                else if (s1.distance > s2.distance)
                                    return 1;
                                return 0;
                            }
                        });
            } else {
                log.info("location is null, position = " + position);
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info("getNearistSubscribers, total " + nearistSubscribers.size()
                + ", elapse " + (System.currentTimeMillis() - start) + " ms.");
        return nearistSubscribers;
    }

    /**
     * @see #notifySubscribers(long, List)
     */
    public void notifySubscribers(long houseId) {
        notifySubscribers(houseId, poiService.getPois(houseId));
    }

    /**
     * Notify with new houses when house (location) changed.
     */
    public void notifySubscribers(long houseId, List<Poi> pois) {
        House house = houseService.getHouse(houseId);
        if (!HouseUtils.isValidToPush(house)) {
            log.info("Don't notify subscribers, invalid house status: houseId="
                    + houseId
                    + ", status="
                    + StringUtils.toString(house.getStatus(),
                            house.getAuditStatus()));
            return;
        }
        Validate.notNull(house);
        /*
         * location
         */
        Location location = null;
        if (house.getLocationX() != null && house.getLocationY() != null) {
            location = new Location(house.getLocationX(), house.getLocationY());
        }
        Set<String> features = new HashSet<String>();
        /*
         * district
         */
        features.add(house.getCity());
        /*
         * line
         */
        if (CollectionUtils.isNotEmpty(pois)) {
            for (Poi poi : pois) {
                features.add(poi.getName());
            }
        }
        /*
         * subscribe
         */
        List<WxSubscribe> subscribes = wxSubscribeDao.getByPosition(location,
                features, IPositionSearcher.SEARCH_POSITION_RADIUS);
        log.info("Hit subscribes: count=" + subscribes.size() + ", houseId="
                + houseId + ", position=" + house.getCity()
                + house.getPosition() + ", location=" + location
                + ", features=" + features);
        if (subscribes.isEmpty()) {
            return;
        }
        /*
         * house results
         */
        List<HouseResult> houseResults = new ArrayList<HouseResult>();
        if (RentType.isRentByHouse(house.getRentType())) {
            houseResults.add(new HouseResult(house));
        }
        if (RentType.isRentByRoom(house.getRentType())) {
            List<Room> rooms = houseService.getRooms(houseId);
            for (Room room : rooms) {
                if (RoomType.isBedroom(room.getType())
                        && room.getStatus() == RentStatus.OPEN.ordinal()) {
                    houseResults.add(new RoomResult(house, room));
                }
            }
        }
        /*
         * <subscriber, {subscribe}>
         */
        AppendableLinkedHashMap<String, WxSubscribe> subscribeMap = new AppendableLinkedHashMap<String, WxSubscribe>(
                subscribes.size());
        for (WxSubscribe wxSubscribe : subscribes) {
            subscribeMap.append(wxSubscribe.getOpenId(), wxSubscribe);
        }
        /*
         * notify
         */
        for (List<WxSubscribe> ss : subscribeMap.values()) {
            String openId = null;
            /*
             * title
             */
            List<String> positions = new ArrayList<String>();
            for (WxSubscribe s : ss) {
                if (openId == null) {
                    openId = s.getOpenId();
                }
                positions.add(s.getPosition());
            }
            StringBuilder title = new StringBuilder();
            String keywords = StringUtils.join(positions, ' ');
            if (StringUtils.isNotEmpty(keywords)) {
                title.append('“').append(keywords).append('”');
            }
            title.append("附近的新房");
            /*
             * render
             */
            List<News> newsList = renderer.renderHouseSearchResult(openId,
                    title.toString(), keywords, houseResults);
            /*
             * send
             */
            try {
                WEIXIN_LOG
                        .info("Lazy notify subscriber: openId=" + openId
                                + ", subscribes=" + positions + "; houseId="
                                + houseId + ", position=" + house.getCity()
                                + house.getPosition());
                weiXinNotifier
                        .notifySubscriber(new NotifyMessage(openId,
                                new NewsJsonResponse(openId, newsList)
                                        .toString(), true));
                WxUser wxUser = wxUserDao.get(WxUser.class, openId);
                if (wxUser != null) {
                    wxUser.setRefreshTime(new Date());
                    wxUserDao.update(wxUser);
                }
            } catch (Exception e) {
                log.error("Notify subscriber failed: openId=" + openId
                        + ", houseId=" + houseId + ", positions=" + positions,
                        e);
            }
        }
    }
}
