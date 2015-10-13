package room107.web.house;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.dao.IDao;
import room107.dao.house.HouseResult;
import room107.datamodel.GenderType;
import room107.datamodel.HouseFavor;
import room107.datamodel.Location;
import room107.datamodel.LogType;
import room107.datamodel.RentType;
import room107.datamodel.Suite;
import room107.datamodel.User;
import room107.datamodel.VerifyStatus;
import room107.datamodel.web.JsonResponse;
import room107.service.api.weixin.BasicSubscriberInfo;
import room107.service.api.weixin.Code2TicketManager;
import room107.service.api.weixin.WeiXinService;
import room107.service.house.IHouseService;
import room107.service.house.search.HouseQuery;
import room107.service.house.search.IHouseSearcher;
import room107.service.house.search.SearchInfo;
import room107.service.house.search.position.IPositionSearcher;
import room107.service.user.InsufficientBalanceException;
import room107.util.JsonUtils;
import room107.util.UserBehaviorLog;
import room107.util.UserStatLog;
import room107.util.UserUtils;
import room107.util.WebUtils;

import com.google.gson.Gson;

/**
 * @author WangXiao
 */
@Controller
@RequestMapping("/house")
public class HouseController {

    @Autowired
    private IHouseService houseService;

    @Autowired
    private IHouseSearcher houseSearcher;
    
    @Autowired
    private WeiXinService weiXinService;

    @Autowired
    private Code2TicketManager code2TicketManager;
    
    @RequestMapping(value = "/search")
    public String search(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> map) {
        /*
         * ticket
         */
        User user = UserUtils.getUser(request, response);
        if (user != null) {
            String ticket = code2TicketManager.getTicketNoneBlocked(user
                    .getId());
            if (ticket != null) {
                map.put("ticket", ticket);
            }
        }
        /*
         * favor
         */
        String username = UserUtils.getUsername(request, response);
        if (username != null) {
            List<HouseResult> houseResults = houseService.getFavors(username)
                    .getHouseResults();
            map.put("favorsJson", JsonUtils.toJson(HouseResult
                    .toItems(houseResults)));
        }
        return "house-search";
    }

    @RequestMapping(value = "/search/json")
    @ResponseBody
    public String searchJson(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> map) {
        /*
         * create query
         */
        HouseQuery query = new HouseQuery(StringUtils.trimToNull(request
                .getParameter("query")));
        Integer rentType = WebUtils.getInt(request, "rentType", null);
        if (rentType != null) {
            query.setRentType(RentType.values()[rentType]);
        }
        query.setPriceFrom(WebUtils.getInt(request, "price1", 0));
        query.setPriceTo(WebUtils.getInt(request, "price2", Integer.MAX_VALUE));
        int gender = 0;
        if (WebUtils.getBoolean(request, "male", false)) {
            gender |= GenderType.MALE.ordinal();
        }
        if (WebUtils.getBoolean(request, "female", false)) {
            gender |= GenderType.FEMALE.ordinal();
        }
        query.setGender(GenderType.values()[gender]);
        UserBehaviorLog.HOUSE_SEARCH.info("Search house: "
                + UserUtils.getUserSignature(request, response) + ", query="
                + query);
        /*
         * search
         */
        int maxCount = WebUtils.getInt(request, "count", -1);
        SearchInfo houseResults = houseSearcher.search(query, maxCount);
        
        Location location = null;
        String moreinfo = null;
        if (!CollectionUtils.isEmpty(houseResults.getQueryLocations())) {
            location = houseResults.getQueryLocations().get(0);
            moreinfo = JsonUtils.toJson("locations", houseResults.getQueryLocations());
        }
        UserStatLog.log(request, response, LogType.search, location, moreinfo);
        
        /*
         * convert
         */
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("token", request.getParameter("token"));
        result.put("items", HouseResult.toItems(houseResults.getHouseResults()));
        result.put("locations", houseResults.getQueryLocations());
        
        return new Gson().toJson(result);
    }

    @RequestMapping(value = "/h{houseId}")
    public String houseDetail(HttpServletRequest request,
            HttpServletResponse response, @PathVariable long houseId,
            Map<String, Object> map) {
        UserBehaviorLog.HOUSE_VIEW.info("View house: "
                + UserUtils.getUserSignature(request, response) + ", houseId="
                + houseId);
        Suite suite = houseService.getSuiteByHouse(houseId);
        if (suite == null) {
            map.put("code", HttpServletResponse.SC_NOT_FOUND);
            return "error";
        }
        UserStatLog.log(request, response, LogType.detail, houseId, null);
        
        map.put("suite", new room107.datamodel.web.Suite(suite));
        return "house-detail";
    }

    @RequestMapping(value = "/r{roomId}")
    public String roomDetail(HttpServletRequest request,
            HttpServletResponse response, @PathVariable long roomId,
            Map<String, Object> map) {
        UserBehaviorLog.HOUSE_VIEW.info("View house: "
                + UserUtils.getUserSignature(request, response) + ", roomId="
                + roomId);
        Suite suite = houseService.getSuiteByRoom(roomId);
        if (suite == null) {
            map.put("code", HttpServletResponse.SC_NOT_FOUND);
            return "error";
        }
        UserStatLog.log(request, response, LogType.detail, suite.house.getId(), roomId);
        
        map.put("suite", new room107.datamodel.web.Suite(suite));
        return "house-detail";
    }

    @RequestMapping(value = "/hreport", method = RequestMethod.POST)
    @ResponseBody
    public String updateHouseReportUsers(HttpServletRequest request, HttpServletResponse response) {
        User user = UserUtils.getUser(request, response);
        if (user == null
                || user.getVerifyStatus() == VerifyStatus.UNVERIFIED.ordinal()) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        Long houseId = WebUtils.getLong(request, "houseId", null);
        String userId = request.getParameter("userId");
        String reportType = request.getParameter("reportType");
        String extraInfo = request.getParameter("extraInfo");
        Validate.notNull(userId);
        if (reportType!=null && houseId != null && userId != null && userId.equals(user.getId().toString())) {
            houseService.updateHouseReportUsers(houseId, user, reportType, extraInfo);
            String moreInfo = JsonUtils.toJson("reportType", reportType, "extraInfo", extraInfo,
                        "userId", user.getId());
            UserStatLog.log(request, response, LogType.report, houseId, null, moreInfo);
        }

        return JsonResponse.OK;
    }

    @RequestMapping(value = "/rreport", method = RequestMethod.POST)
    @ResponseBody
    public String updateRoomReportUsers(HttpServletRequest request, HttpServletResponse response) {
        User user = UserUtils.getUser(request, response);
        if (user == null
                || user.getVerifyStatus() == VerifyStatus.UNVERIFIED.ordinal()) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        Long houseId = WebUtils.getLong(request, "houseId", null);
        Long roomId = WebUtils.getLong(request, "roomId", null);
        String userId = request.getParameter("userId");
        String reportType = request.getParameter("reportType");
        String extraInfo = request.getParameter("extraInfo");
        Validate.notNull(userId);
        if (reportType!=null && houseId != null && roomId != null && userId != null && userId.equals(user.getId().toString())) {
            houseService.updateRoomReportUsers(houseId, roomId, user, reportType, extraInfo);
            String moreInfo = JsonUtils.toJson("reportType", reportType, "extraInfo", extraInfo,
                        "userId", user.getId());
            UserStatLog.log(request, response, LogType.report, houseId, roomId, moreInfo);
        }

        return JsonResponse.OK;
    }

    @RequestMapping(value = "/favor/update")
    @ResponseBody
    public String updateFavorJson(HttpServletRequest request,
            HttpServletResponse response, @RequestParam int channel,
            @RequestParam int type, @RequestParam long itemId,
            @RequestParam boolean value) {
        String username = UserUtils.getUsername(request, response);
        if (username == null) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        UserBehaviorLog.HOUSE.info("Update favor: "
                + UserUtils.getUserSignature(request, response) + ", channel="
                + channel + ", type" + type + ", id=" + itemId + ", value="
                + value);
        HouseFavor houseFavor = new HouseFavor();
        houseFavor.setUsername(username);
        houseFavor.setChannel(channel);
        houseFavor.setType(type);
        houseFavor.setItemId(itemId);
        houseFavor.setStatus(value ? IDao.STATUS_OK : IDao.STATUS_DELETED);
        houseFavor.setModifiedTime(new Date());
        houseService.updateFavor(houseFavor);
        
        if (type == 1) {
            UserStatLog.log(request, response, LogType.favorite, null, itemId);
        } else if (type == 2){
            UserStatLog.log(request, response, LogType.favorite, itemId, null);
        }
        return JsonResponse.OK;
    }

    @RequestMapping(value = "/favor/")
    @ResponseBody
    public String getFavorsJson(HttpServletRequest request,
            HttpServletResponse response) {
        String username = UserUtils.getUsername(request, response);
        if (username == null) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        UserBehaviorLog.HOUSE.info("Get favors: "
                + UserUtils.getUserSignature(request, response));
        List<HouseResult> result = houseService.getFavors(username)
                .getHouseResults();
        return JsonUtils.toJson(HouseResult.toItems(result));
    }

    /**
     * Apply for the house contact.
     * 
     * @return {"telephone": string} if OK, or {"balance": false} for the reason
     *         of out of balance
     * @throws InsufficientBalanceException
     */
    @RequestMapping(value = "/apply/{houseId}")
    @ResponseBody
    public String apply(HttpServletRequest request, HttpServletResponse response, @PathVariable long houseId)
            throws InsufficientBalanceException {
        try {
            int authStatus = UserUtils.getAuthenticationStatus(request, response);
            if (authStatus != UserUtils.AUTH_STATUS_VERIFIED) {
                return JsonUtils.toJson(new HouseContact(
                        authStatus));
            } else {
                User user = UserUtils.getUser(request, response);
                HouseContact contact = houseService
                        .applyForHouse(user, houseId);
                
                UserStatLog.log(request, response, LogType.contact, houseId, null);
                
                return JsonUtils.toJson(contact);
            }
        } catch (Exception e) {
            return JsonUtils.toJson(new HouseContact(
                    UserUtils.AUTH_STATUS_NOT_LOGGED_IN));
        }
    }
    
    @RequestMapping(value = "/search/subscribe")
    @ResponseBody
    public String searchSubscribe(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> map) {
        String position = WebUtils.getNullIfEmpty(request, "position");
        List<BasicSubscriberInfo> subscribers = weiXinService.getNearistSubscribers(
                position, IPositionSearcher.SHOW_SUBSCRIBER_RADIUS);
        UserStatLog.log(request, response, LogType.getSubscribe, JsonUtils.toJson("number", subscribers.size()));
        return JsonUtils.toJson(subscribers);
    }

}
