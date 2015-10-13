/**
 * 
 */
package room107.web.admin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import room107.datamodel.AuditStatus;
import room107.datamodel.ExternalHouse;
import room107.datamodel.GenderType;
import room107.datamodel.House;
import room107.datamodel.LogType;
import room107.datamodel.Platform;
import room107.datamodel.RegRole;
import room107.datamodel.RentStatus;
import room107.datamodel.RentType;
import room107.datamodel.Room;
import room107.datamodel.RoomType;
import room107.datamodel.User;
import room107.datamodel.UserStatus;
import room107.datamodel.UserType;
import room107.datamodel.VerifyStatus;
import room107.datamodel.web.JsonResponse;
import room107.service.admin.IAdminService;
import room107.service.admin.IExternalHouseService;
import room107.service.house.IHouseService;
import room107.util.EncryptUtils;
import room107.util.HouseUtils;
import room107.util.JsonUtils;
import room107.util.QiniuUploadUtils;
import room107.util.UserStatLog;
import room107.util.WebUtils;

/**
 * @author yanghao
 */
@CommonsLog
@Controller
@RequestMapping("/admin/external")
public class AdminExternalHouseController {
    
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMddHHmmSS");
    
    private static final String ANONYMOUS_USERNAME = "Anonymous_%s";
    
    @Autowired
    private IExternalHouseService service;
    
    @Autowired
    private IAdminService adminService;
    
    @Autowired
    private IHouseService houseService;
    
    @RequestMapping(value = "/")
    public String list(HttpServletRequest request, Map<String, Object> map) {
        map.put("houses", service.getUnauditedExternalHouse());
        return "admin/external-house-list";
    }
    
    @RequestMapping(value = "/forum")
    public String forum(HttpServletRequest request, Map<String, Object> map) {
        map.put("houses", service.getUnauditedForumHouse());
        map.put("isForum", true);
        return "admin/external-house-list";
    }
    
    @RequestMapping(value = "/ext{houseId}")
    public String operation(HttpServletRequest request, Map<String, Object> map,
            @PathVariable String houseId) {
        long id = Long.valueOf(houseId);
        ExternalHouse house = service.getHouse(id);
        house.setRentType(WebUtils.getInt(request, "rentType", house.getRentType()));
        map.put("house", house);
        return "admin/external-house-form";
    }
    
    @RequestMapping(value = "/create_{rentType}")
    public String create(HttpServletRequest request, Map<String, Object> map,
            @PathVariable String rentType) {
        ExternalHouse house = new ExternalHouse();
        int type = Integer.valueOf(rentType);
        house.setRentType(type);
        house.setCreatedTime(new Date());
        house.setCity("朝阳");
        map.put("house", house);
        map.put("isCreate", true);
        return "admin/external-house-form";
    }
    
    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    @ResponseBody
    public String verify(HttpServletRequest request, Map<String, Object> map) {
        String telephone = WebUtils.getNullIfEmpty(request, "telephone");
        String qq = WebUtils.getNullIfEmpty(request, "qq");
        String wechat = WebUtils.getNullIfEmpty(request, "wechat");
        if (telephone == null && qq == null && wechat == null) return JsonResponse.error("联系方式为空！");
        List<House> houses = houseService.getHousesByContact(telephone, wechat, qq);
        if (CollectionUtils.isEmpty(houses)) {
            return JsonResponse.message("验证通过，没有相同的联系方式。");
        } else {
            Set<Long> ids = new HashSet<Long>();
            for (House h : houses) {
                ids.add(h.getId());
            }
            return JsonResponse.error("验证未通过，存在冲突的联系方式！houseId=[" +
                    org.apache.commons.lang.StringUtils.join(ids, ',') + "]");
        }
    }
    
    
    @RequestMapping(value = "/addPic/ext{houseId}", method = RequestMethod.POST)
    @ResponseBody
    public String addPic(HttpServletRequest request, @RequestParam("files") MultipartFile file,
            Map<String, Object> map, @PathVariable String houseId) {
        try {
            String key;
            if (StringUtils.isEmpty(houseId)) {
                key = "manual_op";
            } else {
                key = "manual_" + houseId;
            }
            String url = QiniuUploadUtils.upload(QiniuUploadUtils.HOUSE_BUCKET, key, file.getBytes());
            return JsonResponse.message(url);
        } catch(Exception e) {
            log.error("upload picture to qiniu failed.", e);
            return JsonResponse.error("upload failed.");
        }
    }
    
    @RequestMapping(value = "/submit/ext{houseId}", method = RequestMethod.POST)
    @ResponseBody
    public String submit(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> map, @PathVariable String houseId) {
        try {
            long id = Long.valueOf(houseId);
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("status", 200);
            result.put("houseId", -1);
            int operationStatus = WebUtils.getInt(request, "operationStatus", 1);
            if (operationStatus == AuditStatus.REJECTED.ordinal()) {
                service.discardHouse(id);
            } else if (operationStatus == AuditStatus.ACCEPTED.ordinal()) {
                House house = save(request);
                service.bindHouse(id, house);
                result.put("houseId", house.getId());
                UserStatLog.log(LogType.adminAddHouse, new Date(), house.getUsername(), null,
                        null, Platform.pc, house.getId(), null, null, null, JsonUtils.toJson("externalHouseId", id));
            } else if(operationStatus == AuditStatus.DELAY.ordinal()){
                service.delayHouse(id);
            }
            return JsonUtils.toJson(result);
        } catch(Exception e) {
            log.warn("submit failed.", e);
            return JsonResponse.error(e.getMessage());
        }
    }
    
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    @ResponseBody
    public String submit(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("status", 200);
            result.put("houseId", -1);
            int operationStatus = WebUtils.getInt(request, "operationStatus", 1);
            if (operationStatus == AuditStatus.ACCEPTED.ordinal()) {
                House house = save(request);
                service.createHouse(WebUtils.getNullIfEmpty(request, "url"), house);
                result.put("houseId", house.getId());
                UserStatLog.log(LogType.adminAddHouse, new Date(), house.getUsername(), null,
                        null, Platform.pc, house.getId(), null, null, null, null);
            }
            return JsonUtils.toJson(result);
        } catch(Exception e) {
            log.warn("submit failed.", e);
            return JsonResponse.error(e.getMessage());
        }
    }
    
    @RequestMapping(value = "/show/h{houseId}")
    public String show(HttpServletRequest request, Map<String, Object> map,
            @PathVariable String houseId) {
        long id = Long.valueOf(houseId);
        House house = houseService.getHouse(id);
        map.put("house", house);
        try {
            map.put("encryptKey", EncryptUtils.encryptAnomynous(house.getUsername()));
        } catch (Exception e) {}
        return "admin/external-house-detail";
    }
    
    private String getCover(List<Room> rooms) {
        String imageIds = rooms.get(0).getImageIds();
        if (StringUtils.isEmpty(imageIds)) return null;
        String[] ss = imageIds.split("\\|");
        return ss[0];
    }
    
    private House save(HttpServletRequest request) throws Exception {
        User user = getUser(request);
        House house = getHouse(request, user);
        List<Room> rooms = getRooms(request, user, house);
        house.setImageId(getCover(rooms));
        log.debug("To save: user=" + user);
        log.debug("To save: house=" + house);
        log.debug("To save: rooms=" + rooms);
        adminService.save(user, house, rooms);
        return house;
    }
    
    private User getUser(HttpServletRequest request) throws Exception {
        User user = new User();
        user.setUsername(String.format(ANONYMOUS_USERNAME, FORMAT.format(new Date())));
        String telephone = WebUtils.getNullIfEmpty(request, "telephone");
        String qq = WebUtils.getNullIfEmpty(request, "qq");
        String wechat = WebUtils.getNullIfEmpty(request, "wechat");
        if (StringUtils.isEmpty(telephone) && StringUtils.isEmpty(qq) && StringUtils.isEmpty(wechat)) {
            throw new Exception("电话、qq、微信至少填写一项！");
        }
        user.setTelephone(telephone);
        user.setType(UserType.LANDLORD.ordinal());
        user.setVerifyStatus(VerifyStatus.UNVERIFIED.ordinal());
        user.setStatus(UserStatus.ANONYMOUS.ordinal());
        user.setCreatedTime(new Date());
        user.setModifiedTime(new Date());
        user.setRegRole(RegRole.PC_LANDLORD);
        user.setPassword(String.format(ANONYMOUS_USERNAME, "" + (int)(Math.random() * 1000000)));
        return user;
    }
    
    private House getHouse(HttpServletRequest request, User user) throws Exception {
        House house = new House();
        house.setRentType(WebUtils.getInt(request, "rentType", -1));
        if (house.getRentType() < 0) {
            throw new Exception("出租方式不允许为空！");
        }
        house.setCity(WebUtils.getNullIfEmpty(request, "city"));
        if (StringUtils.isEmpty(house.getCity())) {
            throw new Exception("城市不允许为空！");
        }
        house.setPosition(WebUtils.getNullIfEmpty(request, "position"));
        if (StringUtils.isEmpty(house.getPosition())) {
            throw new Exception("地址不允许为空！");
        }
        house.setStatus(RentStatus.OPEN.ordinal());
        house.setRoomNumber(WebUtils.getInt(request, "roomNumber", 0));
        if (house.getRoomNumber() <= 0) {
            throw new Exception("room number is null!");
        }
        house.setHallNumber(WebUtils.getInt(request, "hallNumber", null));
        house.setKitchenNumber(WebUtils.getInt(request, "kitchenNumber", null));
        house.setToiletNumber(WebUtils.getInt(request, "toiletNumber", null));
        house.setFloor(WebUtils.getInt(request, "floor", null));
        Date date = HouseUtils.parseDate(WebUtils.getNullIfEmpty(request, "createdTime"));
        if (date == null) {
            throw new Exception("发布时间不允许为空！");
        }
        house.setCreatedTime(date);
        house.setModifiedTime(new Date());
        house.setUsername(user.getUsername());
        house.setDescription(WebUtils.getNullIfEmpty(request, "description"));
        house.setFacilities(WebUtils.getMultiValue(request, "facilities", null));
        if (house.getRentType() == RentType.BY_ROOM.ordinal()) {
            house.setRequiredGender(WebUtils.getInt(request, "requiredGender", GenderType.MALE_AND_FEMALE.ordinal()));
        } else {
            house.setArea(WebUtils.getInt(request, "area", null));
            house.setPrice(WebUtils.getInt(request, "price", null));
            if (house.getPrice() == null) {
                throw new Exception("价格不允许为空！");
            }
        }
        house.setName(HouseUtils.formatStruct(house));
        house.setRank(WebUtils.getInt(request, "rank", 50));
        house.setTelephone(WebUtils.getNullIfEmpty(request, "telephone"));
        house.setQq(WebUtils.getNullIfEmpty(request, "qq"));
        house.setWechat(WebUtils.getNullIfEmpty(request, "wechat"));
        house.setComment(WebUtils.getNullIfEmpty(request, "comment"));
        house.setAuditStatus(AuditStatus.PROCESSING.ordinal());
        house.setUserStatus(UserStatus.ANONYMOUS.ordinal());
        
        return house;
    }
    
    private List<Room> getRooms(HttpServletRequest request, User user, House house) throws Exception {
        String[] ids = request.getParameterValues("roomId");
        if (ids == null || ids.length <= 0) {
            throw new Exception("至少填写一个房间！");
        }
        boolean containsBedRoom = false;
        List<Room> rooms = new ArrayList<Room>();
        for (String id : ids) {
            Room room = new Room();
            room.setType(WebUtils.getInt(request, "roomType_" + id, 1));
            if (room.getType() == RoomType.UNKNOWN.ordinal()) {
                throw new Exception("不允许有类型为未知的房间！");
            }
            if (RoomType.isBedroom(room.getType()) && room.getStatus() == RentStatus.OPEN.ordinal()) {
                containsBedRoom = true;
            }
            if (house.getRentType() == RentType.BY_ROOM.ordinal() && RoomType.isBedroom(room.getType())) {
                room.setStatus(WebUtils.getInt(request, "roomStatus_" + id, 0));
                if (room.getStatus() == RentStatus.OPEN.ordinal()) {
                    room.setPrice(WebUtils.getInt(request, "roomPrice_" + id, null));
                    if (room.getPrice() == null) {
                        throw new Exception("待租的卧室价格不允许为空！");
                    }
                }
                room.setOrientation(WebUtils.getNullIfEmpty(request, "roomOrientation_" + id));
                room.setArea(WebUtils.getInt(request, "roomArea_" + id, null));
            }
            room.setRank(house.getRank());
            room.setCreatedTime(house.getCreatedTime());
            room.setModifiedTime(house.getModifiedTime());
            room.setName(RoomType.values()[room.getType()].toString());
            room.setUsername(user.getUsername());
            String imgs = WebUtils.getNullIfEmpty(request, "imageIds_" + id);
            if (StringUtils.isEmpty(imgs)) {
                if (ids.length > 1) {
                    throw new Exception("房间图片不允许为空 ！");
                }
            }
            room.setImageIds(imgs);
            rooms.add(room);
        }
        if (!containsBedRoom) {
            throw new Exception("必须有至少一个待租卧室！");
        }
        return rooms;
    }
}
