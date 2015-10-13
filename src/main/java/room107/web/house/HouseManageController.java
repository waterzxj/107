package room107.web.house;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.datamodel.AuditStatus;
import room107.datamodel.House;
import room107.datamodel.HouseAndRoom;
import room107.datamodel.LogType;
import room107.datamodel.Room;
import room107.datamodel.User;
import room107.datamodel.UserStatus;
import room107.datamodel.web.JsonResponse;
import room107.datamodel.web.UserInfo;
import room107.service.api.weixin.Code2TicketManager;
import room107.service.house.IHouseService;
import room107.service.stat.HouseStatistics;
import room107.service.storage.qiniu.QiniuService;
import room107.service.user.IUserService;
import room107.util.JsonUtils;
import room107.util.UserStatLog;
import room107.util.UserUtils;
import room107.util.WebUtils;

import com.google.gson.Gson;

/**
 * @author WangXiao
 */
@CommonsLog
@RequestMapping("/house")
@Controller
@SuppressWarnings("unchecked")
public class HouseManageController {

    @Autowired
    private IHouseService houseService;

    @Autowired
    private IUserService userService;

    @Autowired
    private QiniuService qiniuService;

    @Autowired
    private Code2TicketManager code2TicketManager;

    @Autowired
    private HouseStatistics houseStatistics;
    
    @RequestMapping("/add")
    public String houseAdd(HttpServletRequest request,
    		HttpServletResponse response, Map<String, Object> map)
    		throws Exception{
    	User user = UserUtils.getUser(request, response);
    	if(user != null){
    		User u = userService.getUser(user.getUsername());
    		map.put("user", new UserInfo(u));
    	}
    	map.put("district2UserCount", houseStatistics.getDistrict2UserCount());
    	map.put("uptokenHouse", qiniuService.getUptokenHouse());
    	map.put("redirectuptokenHouse", qiniuService.getRedirectUptokenHouse(request.getServerName()));
    	return "house/add";
    }

    @RequestMapping("/manage")
    public String index(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> map)
            throws Exception {
        String houseId = request.getParameter("hid");
        User user = UserUtils.getUser(request, response);
        if (houseId == null && user != null) {
            Long id = houseService.getHouseId(user.getUsername());
            if (id != null) {
                houseId = "" + id;
            }
        }
        if (houseId != null) {
            if (user != null) {
                HouseAndRoom houseAndRoom = houseService.getHouseAndRoom(
                        user.getUsername(), Long.parseLong(houseId));
                map.put("houseAndRoom", houseAndRoom);
                User u = userService.getUser(user.getUsername());
                map.put("user", new UserInfo(u));
                /*
                 * ticket
                 */
                if (user != null) {
                    String ticket = code2TicketManager
                            .getTicketNoneBlocked(user.getId());
                    if (ticket != null) {
                        map.put("ticket", ticket);
                    }
                }
            }
        }
        /*
         * qiniu uptocken
         */
        map.put("uptokenHouse", qiniuService.getUptokenHouse());
        map.put("uptokenTest", qiniuService.getUptokenTest());
        map.put("redirectuptokenHouse",
                qiniuService.getRedirectUptokenHouse(request.getServerName()));
        return "house/manage";
    }

    @RequestMapping(value = "/manage/update", method = RequestMethod.POST)
    @ResponseBody
    public String update(HttpServletRequest request, HttpServletResponse response) {
        User user = UserUtils.getUser(request, response);
        if (user == null) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        Gson gson = new Gson();
        // house
        Long houseId = null;
        String json = request.getParameter("house");
        if (json != null) {
            Map<String, ?> map = gson.fromJson(json, Map.class);
            System.out.println("the house json is " + json);
            if (log.isDebugEnabled()) {
                log.debug("Deserialized JSON: house=" + map);
            }
            House house = deserialize(map, new House());
            houseService.updateHouse(user.getUsername(), house);
            houseId = house.getId();
        }
        // room
        Long roomId = null;
        json = request.getParameter("room");
        if (json != null) {
            Map<String, ?> map = gson.fromJson(json, Map.class);
            if (log.isDebugEnabled()) {
                log.debug("Deserialized JSON: room=" + map);
            }
            Room room = deserialize(map, new Room());
            room = houseService.updateOrAddRoom(user.getUsername(), room);
            // cover
            String cover = request.getParameter("cover");
            if (cover != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Update cover: houseId=" + room.getHouseId()
                            + ", cover=" + cover);
                }

                houseService.updateCover(room.getHouseId(), cover);
            }
            roomId = room.getId();
            UserStatLog.log(request, response, LogType.updateHouse, houseId, roomId);
            return JsonResponse.message(room.getId());
        }
        UserStatLog.log(request, response, LogType.updateHouse, houseId, roomId);
        return JsonResponse.OK;
    }

    @RequestMapping(value = "/manage/add", method = RequestMethod.POST)
    @ResponseBody
    public String add(HttpServletRequest request, HttpServletResponse response) {
        User user = UserUtils.getUser(request, response);
        if (user == null) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        House house = new House();
        List<Room> rooms = new ArrayList<Room>();
        deserialize(request, house, rooms);
        if (house.getImageId() == null) { // usually not happen
            for (Room room : rooms) {
                String ids = room.getImageIds();
                if (ids != null) {
                    try {
                        int i = ids.indexOf('|');
                        house.setImageId(i > 0 ? ids.substring(0, i) : ids);
                        break;
                    } catch (Exception e) {
                        log.error("Failed to get house cover", e);
                    }
                }
            }
        }
        house.setAuditStatus(AuditStatus.PROCESSING.ordinal());
        house.setUserStatus(UserStatus.NORMAL.ordinal());
        long houseId = houseService.addHouseAndRooms(user, house, rooms);
        UserUtils.updateUser(request, response, user);
        
        UserStatLog.log(request, response, LogType.addHouse, houseId, null);
        return JsonUtils.toJson("houseId", Long.toString(houseId));
    }

    @RequestMapping(value = "/manage/delete", method = RequestMethod.POST)
    @ResponseBody
    public String delete(HttpServletRequest request, HttpServletResponse response) {
        User user = UserUtils.getUser(request, response);
        if (user == null) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        long roomId = WebUtils.getInt(request, "rid", null);
        houseService.deleteRoom(user.getUsername(), roomId);
        UserStatLog.log(request, response, LogType.updateHouse, null, roomId, JsonUtils.toJson("type", "deleteRoom"));
        return JsonResponse.OK;
    }

    @RequestMapping(value = "/manage/close", method = RequestMethod.POST)
    @ResponseBody
    public String closeAll(HttpServletRequest request, HttpServletResponse response, @RequestParam long hid) {
        User user = UserUtils.getUser(request, response);
        if (user == null) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        houseService.closeAll(user.getUsername(), hid);
        UserStatLog.log(request, response, LogType.updateHouse, hid, null, JsonUtils.toJson("type", "closeAll"));
        return JsonResponse.OK;
    }

    @RequestMapping(value = "/manage/update/status", method = RequestMethod.POST)
    @ResponseBody
    public String updateStatus(HttpServletRequest request, HttpServletResponse response) {
        User user = UserUtils.getUser(request, response);
        if (user == null) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        Integer status = WebUtils.getInt(request, "status", null);
        Validate.notNull(status);
        // house
        Long id = WebUtils.getLong(request, "hid", null);
        if (id != null) {
            houseService.updateHouseStatus(user.getUsername(), id, status);
            UserStatLog.log(request, response, LogType.updateHouse, id, null, JsonUtils.toJson("type", "updateStatus", "status", status));
        }
        // room
        id = WebUtils.getLong(request, "rid", null);
        if (id != null) {
            houseService.updateRoomStatus(user.getUsername(), id, status);
            UserStatLog.log(request, response, LogType.updateHouse, null, id, JsonUtils.toJson("type", "updateStatus", "status", status));
        }
        return JsonResponse.OK;
    }

    @RequestMapping(value = "/upload/redirect", method = RequestMethod.GET)
    public String uploadRedirect() {
        return "house/redirect-upload";
    }

    private void deserialize(HttpServletRequest request, House house,
            List<Room> rooms) {
        String rentOption = request.getParameter("rentOption");
        Validate.notNull(rentOption, "null rentOption");
        Map<String, ?> map = new Gson().fromJson(rentOption, Map.class);
        if (log.isDebugEnabled()) {
            log.debug("Deserialized JSON: rentOption=" + map);
        }
        // basic
        Object value = map.get("rentType");
        if (value != null) {
            house.setRentType((int) Double.parseDouble(value.toString()));
        }
        value = map.get("cover");
        if (value != null) {
            house.setImageId(value.toString());
        }
        // house
        deserialize((Map<String, ?>) map.get("house"), house);
        // rooms
        Map<String, Map<String, ?>> roomsMap = (Map<String, Map<String, ?>>) map
                .get("rooms");
        if (roomsMap != null) {
            for (Entry<String, Map<String, ?>> e : roomsMap.entrySet()) {
                rooms.add(deserialize(e.getValue(), new Room()));
            }
        }
    }

    private House deserialize(Map<String, ?> houseMap, House house) {
        if (houseMap == null) {
            return null;
        }
        Object value = houseMap.get("houseId");
        if (value != null) {
            house.setId((long) Double.parseDouble(value.toString()));
        }
        value = houseMap.get("district");
        if (value != null) {
            house.setCity(value.toString());
        }
        value = houseMap.get("position");
        if (value != null) {
            house.setPosition(value.toString().trim());
        }
        value = houseMap.get("roomCount");
        if (value != null) {
            house.setRoomNumber((int) Double.parseDouble(value.toString()));
        }
        value = houseMap.get("hallCount");
        if (value != null) {
            house.setHallNumber((int) Double.parseDouble(value.toString()));
        }
        value = houseMap.get("kitchenCount");
        if (value != null) {
            house.setKitchenNumber((int) Double.parseDouble(value.toString()));
        }
        value = houseMap.get("toiletCount");
        if (value != null) {
            house.setToiletNumber((int) Double.parseDouble(value.toString()));
        }
        value = houseMap.get("floor");
        if (value != null) {
            house.setFloor((int) Double.parseDouble(value.toString()));
        }
        value = houseMap.get("facility");
        if (value != null) {
            house.setFacilities(value.toString().replace('、', '|'));
        }
        value = houseMap.get("description");
        if (value != null) {
            house.setDescription(value.toString().trim());
        }
        value = houseMap.get("telephone");
        if (value != null && !StringUtils.isEmpty(value.toString())) {
            house.setTelephone(value.toString().trim());
        }
        value = houseMap.get("qq");
        if (value != null && !StringUtils.isEmpty(value.toString())) {
            house.setQq(value.toString().trim());
        }
        value = houseMap.get("wechat");
        if (value != null && !StringUtils.isEmpty(value.toString())) {
            house.setWechat(value.toString().trim());
        }
        // by house
        value = houseMap.get("price");
        if (value != null) {
            house.setPrice((int) Double.parseDouble(value.toString()));
        }
        value = houseMap.get("area");
        if (value != null) {
            house.setArea((int) Double.parseDouble(value.toString()));
        }
        // by room
        value = houseMap.get("requiredGender");
        if (value != null) {
            house.setRequiredGender((int) Double.parseDouble(value.toString()));
        }
        return house;
    }

    private Room deserialize(Map<String, ?> roomMap, Room room) {
        if (roomMap == null) {
            return null;
        }
        Object value = roomMap.get("roomId");
        if (value != null) {
            room.setId((long) Double.parseDouble(value.toString()));
        }
        value = roomMap.get("houseId");
        if (value != null) {
            room.setHouseId((long) Double.parseDouble(value.toString()));
        }
        value = roomMap.get("status");
        if (value != null) {
            room.setStatus((int) Double.parseDouble(value.toString()));
        }
        value = roomMap.get("type");
        if (value != null) {
            room.setType((int) Double.parseDouble(value.toString()));
        }
        value = roomMap.get("price");
        if (value != null) {
            room.setPrice((int) Double.parseDouble(value.toString()));
        }
        value = roomMap.get("area");
        if (value != null) {
            room.setArea((int) Double.parseDouble(value.toString()));
        }
        value = roomMap.get("orientation");
        if (value != null) {
            room.setOrientation(value.toString());
        }
        value = roomMap.get("images");
        if (value != null) {
            room.setImageIds(StringUtils.trimToNull(StringUtils.join(
                    (Collection<String>) value, '|')));
        }
        return room;
    }

}
