package room107.web.mobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import room107.datamodel.AuditStatus;
import room107.datamodel.House;
import room107.datamodel.LogType;
import room107.datamodel.Platform;
import room107.datamodel.RentStatus;
import room107.datamodel.RentType;
import room107.datamodel.Room;
import room107.datamodel.RoomType;
import room107.datamodel.Suite;
import room107.datamodel.User;
import room107.datamodel.WxUser;
import room107.datamodel.web.RoomPreview;
import room107.datamodel.web.UserInfo;
import room107.service.api.weixin.Renderer;
import room107.service.api.weixin.WeiXinService;
import room107.service.house.IHouseService;
import room107.service.user.IUserService;
import room107.util.EncryptUtils;
import room107.util.HouseUtils;
import room107.util.UserBehaviorLog;
import room107.util.UserStatLog;
import room107.util.UserUtils;
import room107.util.WebUtils;

/**
 * @author WangXiao
 */
@CommonsLog
@RequestMapping("/m")
@Controller
public class MobileController {

    @Autowired
    private IHouseService houseService;

    @Autowired
    private IUserService userService;

    @Autowired
    private WeiXinService weiXinService;

    @RequestMapping({ "/", "/index" })
    public String index() {
        return "mobile/index";
    }

    @RequestMapping(value = "/user/{euid}")
    public String home(HttpServletRequest request,
            HttpServletResponse response, @PathVariable String euid,
            Map<String, Object> map) throws Exception {
        String openId = EncryptUtils.decrypt(euid);
        List<String> positions = weiXinService.getSubscription(openId);
        UserBehaviorLog.WEIXIN.info("View subscription: openId=" + openId);
        map.put("positions", positions);
        map.put(Renderer.PARAM_ENCRYPTED_UID, euid);
        return "mobile/home";
    }

    @RequestMapping("/house/{id}")
    public String viewHouse(HttpServletRequest request,
            HttpServletResponse response, @PathVariable long id,
            Map<String, Object> map) {
        String euid = request.getParameter(Renderer.PARAM_ENCRYPTED_UID);
        WxUser wxUser = getWxUser(request, response);
        User user = getUser(request, response, wxUser);
        UserBehaviorLog.WEIXIN.info("View house: "
                + UserUtils.getUserSignature(user, euid) + ", houseId=" + id);
        Suite suite = houseService.getSuiteByHouse(id);
        Validate.notNull(suite, "Invalid: houseId=" + id);
        //redirect to viewRoom
        if (suite.house.getRentType() == RentType.BY_ROOM.ordinal()) {
            Room selectedRoom = null;
            for (Room room : suite.rooms) {
                if (RoomType.isBedroom(room.getType())) {
                    selectedRoom = room;
                    if (room.getStatus() == RentStatus.OPEN.ordinal()) {
                        break ;
                    }
                }
            }
            if (selectedRoom != null) {
                return viewRoom(request, response, selectedRoom.getId(), map);
            }
        }
        
        
        /*
         * bedrooms
         */
        List<RoomPreview> roomPreviews = new ArrayList<RoomPreview>();
        for (Room room : suite.rooms) {
            roomPreviews.add(new RoomPreview(room));
        }
        
        /*
         * model
         */
        setLandlordData(map, suite);
        
        String username = null;
        String openId = null;
        if (wxUser != null) {
            username = wxUser.getUsername();
            openId = wxUser.getOpenId();
        }
        UserStatLog.log(LogType.detail, new Date(), username, openId,
                WebUtils.getRealIp(request), Platform.wechat, id, null, null, null, null);

        House house = suite.house;
        map.put("user", user);
        map.put(Renderer.PARAM_ENCRYPTED_UID, euid);
        map.put("house", house);
        map.put("status", (house.getStatus() == RentStatus.OPEN.ordinal()) &&
                (house.getAuditStatus() == AuditStatus.ACCEPTED.ordinal()));
        map.put("roomPreviews", roomPreviews);
        // detail
        map.put("rentType", house.getRentType());
        map.put("area", house.getArea());
        map.put("price", house.getPrice());
        map.put("floor", house.getFloor());
        map.put("struct", HouseUtils.formatStruct(house));
        return "mobile/house-detail";
    }

    @RequestMapping("/room/{id}")
    public String viewRoom(HttpServletRequest request,
            HttpServletResponse response, @PathVariable final long id,
            Map<String, Object> map) {
        WxUser wxUser = getWxUser(request, response);
        User user = getUser(request, response, wxUser);
        String euid = request.getParameter(Renderer.PARAM_ENCRYPTED_UID);
        UserBehaviorLog.WEIXIN.info("View room: "
                + UserUtils.getUserSignature(user, euid) + ", roomId=" + id);
        Suite suite = houseService.getSuiteByRoom(id);
        Validate.notNull(suite, "Invalid: roomId=" + id);
        /*
         * room
         */
        List<RoomPreview> roomPreviews = new ArrayList<RoomPreview>();
        for (Room room : suite.rooms) {
            roomPreviews.add(new RoomPreview(room));
        }
        CollectionUtils.filter(roomPreviews, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                Room r = ((RoomPreview) object).getRoom();
                if (RoomType.isBedroom(r.getType())
                        && r.getId().longValue() != id) {
                    return false;
                }
                return true;
            }
        });
        Room room = null;
        if (!roomPreviews.isEmpty()) {
            // for reliability
            room = roomPreviews.get(0).getRoom();
        }
        
        /*
         * model
         */
        setLandlordData(map, suite);
        
        String username = null;
        String openId = null;
        if (wxUser != null) {
            username = wxUser.getUsername();
            openId = wxUser.getOpenId();
        }
        Long houseId = (room == null ? null : room.getHouseId());
        UserStatLog.log(LogType.detail, new Date(), username, openId,
                WebUtils.getRealIp(request), Platform.wechat, houseId, id, null, null, null);

        House house = suite.house;
        map.put("user", user);
        map.put(Renderer.PARAM_ENCRYPTED_UID, euid);
        map.put("house", house);
        map.put("room", room);
        map.put("status", (house.getStatus() == RentStatus.OPEN.ordinal()) &&
                (room.getStatus() == RentStatus.OPEN.ordinal()) &&
                (house.getAuditStatus() == AuditStatus.ACCEPTED.ordinal()));
        map.put("roomPreviews", roomPreviews);
        // detail
        map.put("area", room.getArea());
        map.put("price", room.getPrice());
        map.put("rentType", RoomType.values()[room.getType()]);
        // legacy
        map.put("requiredGender", house.getRequiredGender());
        map.put("orientation", room.getOrientation());
        map.put("floor", house.getFloor());
        map.put("struct", HouseUtils.formatStruct(house));
        return "mobile/house-detail";
    }
    
    /**
     * Setting landlord data for template rendering.
     * @param map
     * @param suite
     */
    private void setLandlordData(Map<String, Object> map, Suite suite){
        UserInfo landlord = new UserInfo(suite.landlord);

        map.put("name", landlord.getName());
        map.put("age", landlord.getAge());
        map.put("gender", landlord.getGender());
        map.put("constellation", landlord.getConstellation());
        map.put("school", landlord.getSchool());
        map.put("major", landlord.getMajor());
        map.put("company", landlord.getCompany());
        map.put("profession", landlord.getProfession());
        map.put("description", landlord.getDescription());
        map.put("faviconUrl", landlord.getFaviconUrl());
    }
    
    private WxUser getWxUser(HttpServletRequest request,
            HttpServletResponse response) {
        String euid = request.getParameter(Renderer.PARAM_ENCRYPTED_UID);
        if (euid == null) {
            return null;
        }
        // open ID
        String openId;
        try {
            openId = EncryptUtils.decrypt(euid);
        } catch (Exception e) {
            log.error("Invliad euid: " + euid);
            return null;
        }
        WxUser wxUser = userService.getWxUser(openId);
        return wxUser;
    }

    /**
     * @return null when invalid
     */
    private User getUser(HttpServletRequest request,
            HttpServletResponse response, WxUser wxUser) {
        // login
        if (wxUser != null) {
            User user = userService.getUser(wxUser.getUsername());
            if (user != null) {
                UserUtils.login(user, wxUser, request, response);
                return user;
            }
        }
        return null;
    }

}
