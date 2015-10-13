package room107.service.house;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.IDao;
import room107.dao.IUserDao;
import room107.dao.house.HouseResult;
import room107.dao.house.IExternalHouseDao;
import room107.dao.house.IHouseDao;
import room107.dao.house.IHouseFavorDao;
import room107.dao.house.RoomResult;
import room107.datamodel.AuditStatus;
import room107.datamodel.ExternalHouse;
import room107.datamodel.House;
import room107.datamodel.HouseAndRoom;
import room107.datamodel.HouseAndUser;
import room107.datamodel.HouseFavor;
import room107.datamodel.RentType;
import room107.datamodel.Room;
import room107.datamodel.RoomType;
import room107.datamodel.Suite;
import room107.datamodel.User;
import room107.datamodel.UserStatus;
import room107.datamodel.UserType;
import room107.datamodel.VerifyStatus;
import room107.service.house.search.SearchInfo;
import room107.service.message.MessageService;
import room107.service.message.type.NewHouse;
import room107.service.user.InsufficientBalanceException;
import room107.util.AuthUtils;
import room107.util.EmailUtils;
import room107.util.EmailUtils.EmailAccount;
import room107.util.HouseUtils;
import room107.util.UserBehaviorLog;
import room107.util.UserUtils;
import room107.web.house.HouseContact;

/**
 * @author WangXiao
 */
@CommonsLog
@Service
@Transactional
public class HouseServiceImpl implements IHouseService {

    @Autowired
    private IHouseDao houseDao;

    @Autowired
    private IExternalHouseDao externalHouseDao;

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IHouseFavorDao houseFavorDao;

    @Autowired
    private MessageService messageService;

    public static final float DEFAULT_RANK = 50;

    @Override
    public Suite getSuiteByRoom(long roomId) {
        House house = houseDao.getHouseByRoom(roomId);
        return getSuite(house, roomId);
    }

    @Override
    public Suite getSuiteByHouse(long houseId) {
        House house = houseDao.getHouse(houseId);
        return getSuite(house, null);
    }

    /**
     * @param roomId
     *            when set make this room the first room to display
     */
    private Suite getSuite(House house, final Long roomId) {
        if (house == null) {
            return null;
        }
        /*
         * init landlord
         */
        User user = userDao.getUser(house.getUsername());
        /*
         * init rooms
         */
        List<Room> rooms = houseDao.getRooms(house.getId());
        Collections.sort(rooms, new Comparator<Room>() {
            @Override
            public int compare(Room o1, Room o2) {
                if (roomId != null) { // display specified first
                    if (o1.getId().equals(roomId)) {
                        return -1;
                    }
                    if (o2.getId().equals(roomId)) {
                        return 1;
                    }
                }
                // sort by type
                return o1.getType() - o2.getType();
            }
        });
        return new Suite(user, house, rooms);
    }

    @Override
    public List<House> getHouses() {
        return houseDao.getAllHouses();
    }

    @Override
    public House getHouse(long houseId) {
        return houseDao.getHouse(houseId);
    }

    @Override
    public HouseAndRoom getHouseAndRoom(long houseId) {
        House house = houseDao.getHouse(houseId);
        Validate.notNull(house);
        List<Room> rooms = houseDao.getRooms(houseId);
        return new HouseAndRoom(house, rooms);
    }

    @Override
    public HouseAndRoom getHouseAndRoom(String username, long houseId) {
        Validate.notNull(username);
        House house = houseDao.getHouse(houseId);
        Validate.notNull(house);
        Validate.isTrue(username.equals(house.getUsername()));
        List<Room> rooms = houseDao.getRooms(houseId);
        return new HouseAndRoom(house, rooms);
    }

    @Override
    public List<Room> getRooms(long houseId) {
        return houseDao.getRooms(houseId);
    }

    @Override
    public List<Room> getImageUnbackupedRooms() {
        return houseDao.getImageUnbackupedRooms();
    }

    @Override
    public HouseContact applyForHouse(User user, long houseId)
            throws InsufficientBalanceException {
        // auth
        if (user.getVerifyStatus() == VerifyStatus.UNVERIFIED.ordinal()) {
            UserBehaviorLog.AUTH
                    .warn("Illegal access: action=applyForHouse, username="
                            + user.getUsername() + ", houseId=" + houseId);
            return null;
        }
        // try house telephone, than landlord telephone
        House house = houseDao.getHouse(houseId);
        Validate.notNull(house, "null house: houseId=" + houseId);
        HouseContact result = new HouseContact(UserUtils.AUTH_STATUS_VERIFIED,
                HouseUtils.formatTelephone(house.getTelephone()),
                HouseUtils.formatQQ(house.getQq()),
                HouseUtils.formatWechat(house.getWechat()));
        UserBehaviorLog.APPLY_FOR_HOUSE.info("username=" + user.getUsername()
                + ", houseId=" + houseId);
        return result;
    }

    @Override
    public Long addHouseAndRooms(User user, House house, List<Room> rooms) {
        Validate.notNull(user);
        Validate.notNull(house);
        Validate.notEmpty(rooms);
        String username = user.getUsername();

        // validate house by username
        List<House> oldHouse = houseDao.getHouses(username);
        if (CollectionUtils.isNotEmpty(oldHouse)) {
            log.error("add a duplicated house for username " + username
                    + ", discard.");
            return oldHouse.get(0).getId();
        }

        // house
        house.setUsername(username);
        house.setName(HouseUtils.formatStruct(house.getRoomNumber(),
                house.getHallNumber(), null, null));
        house.setRank(DEFAULT_RANK);
        Date date = new Date();
        house.setCreatedTime(date);
        house.setModifiedTime(date);
        long houseId = houseDao.saveHouse(house);
        // rooms
        addRooms(username, houseId, rooms);
        String msg = "Add house: username=" + username + ", houseId=" + houseId;
        // update user role
        int type = user.getType();
        if ((type & UserType.LANDLORD.ordinal()) == 0) {
            user.setType(type | UserType.LANDLORD.ordinal());
            userDao.update(user);
            msg += ", +landlord";
        }
        UserBehaviorLog.HOUSE_ADD.info(msg);
        messageService.send(new NewHouse(house));
        return houseId;
    }

    @Override
    public void addRooms(String username, long houseId, List<Room> rooms) {
        Validate.notNull(username);
        Validate.notEmpty(rooms);
        int[] roomTypeCounts = new int[RoomType.values().length];
        for (Room room : rooms) {
            roomTypeCounts[room.getType()]++;
        }
        int[] roomTypeIndex = new int[roomTypeCounts.length];
        Date date = new Date();
        for (Room room : rooms) {
            room.setUsername(username);
            room.setHouseId(houseId);
            room.setRank(DEFAULT_RANK);
            room.setCreatedTime(date);
            room.setModifiedTime(date);
            int type = room.getType();
            String typeString = HouseUtils.getRoomType(type);
            if (roomTypeCounts[type] > 1) {
                room.setName(typeString + (++roomTypeIndex[type]));
            } else {
                room.setName(typeString);
            }
            Long roomId = houseDao.saveRoom(room);
            UserBehaviorLog.HOUSE_ADD.info("Add room: username=" + username
                    + ", roomId=" + roomId);
        }
    }

    @Override
    public void updateHouse(House house) {
        houseDao.updateHouse(house, false);
    }

    /**
     * Excludes: username, status...
     */
    @Override
    public void updateHouse(String username, House house) {
        Validate.notNull(username);
        Validate.notNull(house);
        House oldHouse = houseDao.getHouse(house.getId());
        AuthUtils.isTrue(
                username.equals(oldHouse.getUsername()),
                "Update house: username=" + username + ", houseId="
                        + house.getId());
        boolean positionChanged = !StringUtils.equals(oldHouse.getPosition(),
                house.getPosition());
        oldHouse.setArea(house.getArea());
        oldHouse.setCity(house.getCity());
        boolean descriptionChanged = !StringUtils.equals(
                oldHouse.getDescription(), house.getDescription());
        if (descriptionChanged) {
            EmailUtils.sendAdminMail(
                    "Update house:the description information is changed",
                    "houseId=" + house.getId() + ", username=" + username
                            + ", description=" + house.getDescription());
        }
        oldHouse.setDescription(house.getDescription());
        oldHouse.setFacilities(house.getFacilities());
        oldHouse.setFloor(house.getFloor());
        oldHouse.setHallNumber(house.getHallNumber());
        if (house.getImageId() != null) {
            oldHouse.setImageId(house.getImageId());
        }
        oldHouse.setKitchenNumber(house.getKitchenNumber());
        oldHouse.setName(HouseUtils.formatStruct(house.getRoomNumber(),
                house.getHallNumber(), null, null));
        oldHouse.setPosition(house.getPosition());
        oldHouse.setPrice(house.getPrice());
        oldHouse.setRoomNumber(house.getRoomNumber());
        oldHouse.setTelephone(house.getTelephone());
        oldHouse.setQq(house.getQq());
        oldHouse.setWechat(house.getWechat());
        oldHouse.setToiletNumber(house.getToiletNumber());
        oldHouse.setModifiedTime(new Date());
        oldHouse.setRequiredGender(house.getRequiredGender());
        houseDao.updateHouse(oldHouse, positionChanged);
    }

    @Override
    public void updateRoom(Room room) {
        houseDao.updateRoom(room);
    }

    @Override
    public void update(Room room) {
        houseDao.update(room);
    }
    
    private void sendReportEmail(long houseId, Long roomId, User user, String typeString, String extraInfo){    
        String title = "举报房子";
        String desc = "";
        ReportType reportType = null;
        if(typeString == null){
            reportType = ReportType.UNKNOWN;
        }else{
            try{
                reportType = ReportType.valueOf(typeString);
            }catch(Exception e){
                reportType = ReportType.UNKNOWN;  
            }
        }
        if(reportType == ReportType.RENTED){
            desc = desc + "举报原因: 已出租\n";
        }else if(reportType == ReportType.IS_BROKER){
            desc = desc + "举报原因: 中介房\n";
        }else if(reportType == ReportType.OTHER_REASON){
            desc = desc + "举报原因: 其他（见描述）\n用户描述: "+extraInfo+"\n";
        }else{
            desc = desc + "举报原因: 未知举报类型\n";
        }
        if(roomId != null){
            desc = desc + "详细信息: 分租或不限类型， houseId=" + houseId + "， roomId="+roomId+" ，userName= "+ user.getUsername()+"\n";  
        }else{
            desc = desc + "详细信息: 整租类型， houseId=" + houseId + "， userName= "+ user.getUsername()+"\n";
        }
        EmailUtils.sendAdminMail(title, desc);        
    }
    
    @Override
    public void updateRoomReportUsers(long houseId, Long roomId, User user, String reportType, String extralInfo) {
        Room room = houseDao.getRoom(roomId);
        Validate.notNull(room);
        String oldUsers = room.getReportUsers();
        String newUsers = doNewusers(oldUsers, user.getId().toString());
        room.setReportUsers(newUsers);
        UserBehaviorLog.HOUSE_UPDATE.info("Update room reportUsers: , roomId="
                + roomId + ", reportUsers=" + newUsers);
        houseDao.updateRoom(room);
        sendReportEmail(houseId, roomId, user, reportType, extralInfo);
    }

    private String doNewusers(String oldUsers, String userId) {
        String newUsers = "";
        if (oldUsers == null)
            oldUsers = "";
        if (oldUsers.indexOf(userId) == -1) {
            long timestamp = System.currentTimeMillis();
            if ("".equals(oldUsers)) {
                newUsers = oldUsers + userId + "@" + timestamp;
            } else {
                newUsers = oldUsers + "|" + userId + "@" + timestamp;
            }
        } else {
            long timesstamp = System.currentTimeMillis();
            int from = oldUsers.indexOf(userId);
            from = from + userId.length() + 1;
            int to = oldUsers.indexOf('|', from);
            if (to == -1)
                to = oldUsers.length();
            StringBuffer oldusersBuffer = new StringBuffer(oldUsers);
            oldusersBuffer.delete(from, to);
            oldusersBuffer.insert(from, timesstamp);
            newUsers = oldusersBuffer.toString();
        }
        return newUsers;
    }

    @Override
    public void deleteRoom(String username, long roomId) {
        Room room = houseDao.getRoom(roomId);
        if (room != null && room.getUsername().equals(username)) {
            room.setStatus(IDao.STATUS_DELETED);
            houseDao.updateRoom(room);
        }
    }

    /**
     * Update exclude: houseId
     */
    @Override
    public Room updateOrAddRoom(String username, Room room) {
        Validate.notNull(username);
        Validate.notNull(room);
        House house = houseDao.getHouse(room.getHouseId());
        AuthUtils.isTrue(
                username.equals(house.getUsername()),
                "Update room: username=" + username + ", houseId="
                        + house.getId());
        Date date = new Date();
        String imageIds = room.getImageIds();
        if (room.getId() == null) { // add
            EmailUtils.sendAdminMail(
                    "Update house: add a room",
                    "houseId=" + room.getHouseId() + ", addRoomType=" + RoomType.get(room.getType()));
            int roomType = room.getType();
            if (!RoomType.isBedroom(roomType)) {
                Room old = houseDao
                        .getNonBedroomByType(house.getId(), roomType);
                if (old != null) {
                    if (imageIds == null) { // delete
                        houseDao.deleteRoom(old);
                        UserBehaviorLog.HOUSE_DELETE
                                .info("Delete room: username=" + username
                                        + ", roomId=" + old.getId());
                    } else { // update
                        old.setModifiedTime(new Date());
                        old.setImageIds(room.getImageIds());
                        houseDao.updateRoom(old);
                        log.warn("Update room without roomId: roomId="
                                + old.getId());
                        UserBehaviorLog.HOUSE_UPDATE
                                .info("Update room: username=" + username
                                        + ", roomId=" + old.getId());
                    }
                    return old;
                }
            }
            room.setUsername(username);
            room.setHouseId(house.getId());
            room.setName(HouseUtils.getRoomType(room.getType()));
            room.setRank(house.getRank());
            room.setCreatedTime(date);
            room.setModifiedTime(date);
            long result = houseDao.saveRoom(room);
            UserBehaviorLog.HOUSE_ADD.info("Add room: username="
                    + room.getUsername() + ", roomId=" + result);
            return room;
        } else { // update
            Room oldRoom = houseDao.getRoom(room.getId());
            AuthUtils.isTrue(
                    username.equals(oldRoom.getUsername()),
                    "Update room: username=" + username + ", roomId="
                            + oldRoom.getId());
            if ((!RoomType.isBedroom(oldRoom.getType()))
                    && room.getImageIds() == null) { // delete
                UserBehaviorLog.HOUSE_DELETE.info("Delete room: username="
                        + username + ", roomId=" + oldRoom.getId());
                houseDao.deleteRoom(oldRoom);
            } else { // update
                boolean imageChanged = !StringUtils.equals(room.getImageIds(), oldRoom.getImageIds());
                if (imageChanged) {
                    EmailUtils.sendAdminMail(
                            "Update house: room image changed",
                            "houseId=" + room.getHouseId() + ", roomId=" + room.getId() +
                            ", username: " + username);
                }
                oldRoom.setArea(room.getArea());
                oldRoom.setImageIds(room.getImageIds());
                oldRoom.setName(HouseUtils.getRoomType(room.getType()));
                oldRoom.setOrientation(room.getOrientation());
                oldRoom.setPrice(room.getPrice());
                oldRoom.setStatus(room.getStatus());
                oldRoom.setType(room.getType());
                UserBehaviorLog.HOUSE_UPDATE.info("Update room: username="
                        + username + ", roomId=" + oldRoom.getId());
                houseDao.updateRoom(oldRoom);
            }
            return oldRoom;
        }
    }

    @Override
    public void updateHouseReportUsers(long houseId, User user, String reportType, String extraInfo) {
        House house = houseDao.getHouse(houseId);
        Validate.notNull(house);
        String oldUsers = house.getReportUsers();
        String newUsers = doNewusers(oldUsers, user.getId().toString());
        house.setReportUsers(newUsers);
        UserBehaviorLog.HOUSE_UPDATE
                .info("Update house reportUsers: , houseId=" + houseId
                        + ", reportUsers=" + newUsers);
        houseDao.updateHouse(house, false);
        sendReportEmail(houseId, null, user, reportType, extraInfo);
    }

    @Override
    public void updateHouseStatus(long houseId, int status) {
        House house = houseDao.getHouse(houseId);
        Validate.notNull(house);
        if (house.getStatus() != status) {
            house.setStatus(status);
            house.setModifiedTime(new Date());
            UserBehaviorLog.HOUSE_UPDATE.info("Update house status: , houseId="
                    + houseId + ", status=" + status);
            houseDao.updateHouse(house, false);
        }
    }

    @Override
    public void updateHouseStatus(String username, long houseId, int status) {
        Validate.notNull(username);
        House house = houseDao.getHouse(houseId);
        Validate.notNull(house);
        AuthUtils.isTrue(username.equals(house.getUsername()),
                "Update house status: username=" + username + ", houseId="
                        + houseId);
        if (house.getStatus() != status) {
            house.setStatus(status);
            house.setModifiedTime(new Date());
            UserBehaviorLog.HOUSE_UPDATE.info("Update house status: username="
                    + username + ", houseId=" + houseId + ", status=" + status);
            houseDao.updateHouse(house, false);
        }
    }
    
    @Override
    public void updateRoomStatus(long roomId, int status) {
        Room room = houseDao.getRoom(roomId);
        Validate.notNull(room);
        if (room.getStatus() != status) {
            room.setStatus(status);
            room.setModifiedTime(new Date());
            UserBehaviorLog.HOUSE_UPDATE.info("Update room status: roomId="
                    + roomId + ", status=" + status);
            houseDao.updateRoom(room);
        }
    }

    @Override
    public void updateRoomStatus(String username, long roomId, int status) {
        Validate.notNull(username);
        Room room = houseDao.getRoom(roomId);
        Validate.notNull(room);
        AuthUtils.isTrue(username.equals(room.getUsername()),
                "Update room status: username=" + username + ", roomId="
                        + roomId);
        if (room.getStatus() != status) {
            room.setStatus(status);
            room.setModifiedTime(new Date());
            UserBehaviorLog.HOUSE_UPDATE.info("Update room status: username="
                    + username + ", roomId=" + roomId + ", status=" + status);
            houseDao.updateRoom(room);
        }
    }

    @Override
    public Long getHouseId(String username) {
        List<House> houses = houseDao.getHouses(username);
        return houses == null || houses.isEmpty() ? null : houses.get(0)
                .getId();
    }

    @Override
    public void updateCover(long houseId, String imageId) {
        House house = houseDao.getHouse(houseId);
        Validate.notNull(house);
        house.setImageId(imageId);
        house.setModifiedTime(new Date());
        houseDao.updateHouse(house, false);
    }

    private List<HouseAndUser> fillHouseAndUser(List<House> houses) {
        Map<String, House> name2House = new HashMap<String, House>();
        for (House house : houses) {
            name2House.put(house.getUsername(), house);
        }
        List<User> users = userDao.getUsers(name2House.keySet());
        Map<Long, HouseAndUser> id2Result = new HashMap<Long, HouseAndUser>();
        for (User user : users) {
            HouseAndUser h = new HouseAndUser();
            House house = name2House.get(user.getUsername());
            h.house = house;
            h.user = user;
            id2Result.put(house.getId(), h);
        }
        List<ExternalHouse> ehs = externalHouseDao
                .getExternalHouseByHouseId(id2Result.keySet());
        for (ExternalHouse eh : ehs) {
            HouseAndUser h = id2Result.get(eh.getHouseId());
            h.externalHouse = eh;
        }

        List<HouseAndUser> result = new ArrayList<HouseAndUser>();
        result.addAll(id2Result.values());

        Collections.sort(result, new Comparator<HouseAndUser>() {
            @Override
            public int compare(HouseAndUser h1, HouseAndUser h2) {
                if (h1.house.getId() < h2.house.getId()) {
                    return 1;
                } else if (h1.house.getId() > h2.house.getId()) {
                    return -1;
                }
                return 0;
            }
        });

        return result;
    }

    @Override
    public List<HouseAndUser> getHouseAndUsers(int maxSize) {
        try {
            List<House> houses = houseDao.getAllHouses(maxSize);
            return fillHouseAndUser(houses);
        } catch (Exception e) {
            log.error(e, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<HouseAndUser> getHouseAndUsers(String searchKey) {
        List<HouseAndUser> result = new ArrayList<HouseAndUser>();
        if (StringUtils.isEmpty(searchKey))
            return result;
        try {
            List<House> houses = new ArrayList<House>();
            // by houseId
            try {
                Long id = Long.valueOf(searchKey);
                House house = houseDao.getHouse(id);
                if (house != null)
                    houses.add(house);
            } catch (Exception e) {
            }
            // by contact
            houses.addAll(getHousesByContact(searchKey, searchKey, searchKey));
            // by userKey
            List<User> users = userDao.getUserByKey(searchKey);
            if (CollectionUtils.isNotEmpty(users)) {
                for (User user : users) {
                    houses.addAll(houseDao.getHouses(user.getUsername()));
                }
            }
            // by encryptKey
            String start = "http://107room.com/user/k";
            List<ExternalHouse> ehs = null;
            if (searchKey.startsWith(start)) {
                String key = searchKey.substring(start.length());
                ehs = externalHouseDao.getExternalHouseByEncryptKey(key);

            } else if (searchKey.startsWith("k")) {
                String key = searchKey.substring(1);
                ehs = externalHouseDao.getExternalHouseByEncryptKey(key);
            }
            if (CollectionUtils.isNotEmpty(ehs)) {
                for (ExternalHouse eh : ehs) {
                    House house = houseDao.getHouse(eh.getHouseId());
                    if (house != null) {
                        houses.add(house);
                    }
                }
            }
            result = fillHouseAndUser(houses);
        } catch (Exception e) {
            log.error(e, e);
        }
        return result;
    }

    @Override
    public int getRejectedHouseCount() {
        return houseDao.getHouseCount(AuditStatus.REJECTED)
                + externalHouseDao.getHouseCount(AuditStatus.REJECTED);
    }

    @Override
    public void updateFavor(HouseFavor houseFavor) {
        Validate.notNull(houseFavor);
        Validate.notNull(houseFavor.getUsername());
        Date date = new Date();
        HouseFavor old = houseFavorDao.get(houseFavor);
        if (old == null) {
            houseFavor.setCreatedTime(date);
            houseFavor.setModifiedTime(date);
            houseFavorDao.save(houseFavor);
        } else {
            old.setUsername(houseFavor.getUsername());
            old.setChannel(houseFavor.getChannel());
            old.setType(houseFavor.getType());
            old.setItemId(houseFavor.getItemId());
            old.setStatus(houseFavor.getStatus());
            old.setModifiedTime(date);
            houseFavorDao.update(old);
        }
    }

    @Override
    public SearchInfo getFavors(String username) {
        SearchInfo searchInfo = new SearchInfo();
        List<HouseFavor> favors = houseFavorDao.getAll(username);
        if (favors.isEmpty()) {
            return searchInfo;
        }
        List<HouseResult> result = new ArrayList<HouseResult>(favors.size());
        for (HouseFavor favor : favors) {
            try {
                // TODO channel in future
                int type = favor.getType();
                long itemId = favor.getItemId();
                if (type == RentType.BY_HOUSE.ordinal()) {
                    result.add(new HouseResult(houseDao.getHouse(itemId)));
                } else if (type == RentType.BY_ROOM.ordinal()) {
                    Room room = houseDao.getRoom(itemId);
                    result.add(new RoomResult(houseDao.getHouse(room
                            .getHouseId()), room));
                } else {
                    log.error("Invliad house favor type: id=" + favor.getId()
                            + ", type=" + type);
                }
            } catch (Exception e) {
                log.error("Convert HouseFavor to HouseResult failed: id="
                        + favor.getId(), e);
            }
        }
        fillUser(result);
        searchInfo.setHouseResults(result);
        return searchInfo;
    }

    private void fillUser(List<HouseResult> result) {
        List<String> usernames = new ArrayList<String>();
        for (HouseResult houseResult : result) {
            String username = houseResult.getUserName();
            usernames.add(username);
        }
        List<User> users = userDao.getUsers(usernames);
        Map<String, User> name2User = new HashMap<String, User>();
        for (User user : users) {
            name2User.put(user.getUsername(), user);
        }
        for (HouseResult houseResult : result) {
            houseResult.setUser(name2User.get(houseResult.getUserName()));
        }
    }

    @Override
    public void closeDeprecated() {
        Date time = DateUtils.addWeeks(new Date(), -3);
        houseDao.close(UserStatus.NORMAL, time);
        time = DateUtils.addDays(new Date(), -5);
        houseDao.close(UserStatus.ANONYMOUS, time);
    }

    @Override
    public void remindDeprecated(int week) {
        Date from = new Date();
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);
        from = fromCalendar.getTime();
        from = DateUtils.addWeeks(new Date(), -week);
        Date to = DateUtils.addDays(from, 1);
        List<House> results = houseDao.getHousesByModifiedTime(from, to);
        List<String> usernames = new ArrayList<String>();
        for (House house : results) {
            String username = house.getUsername();
            usernames.add(username);
        }
        List<User> users = userDao.getUsers(usernames);
        if (week == 1 || week == 2) {
            for (User user : users) {
                if (!StringUtils.isEmpty(user.getEmail())) {
                    EmailUtils
                            .sendMail(
                                    user.getEmail(),
                                    "107间--关房提醒",
                                    "提醒邮件：<br/><br/>"
                                            + user.getUsername()
                                            + "你好，你的房间已发布"
                                            + week
                                            + "周。<br/><br/>"
                                            + "<span style=\"color:red;font-weight:bold;\">如已成功出租，请到理房页面关闭出租。</span><br/><br/>"
                                            + "为保证时效性，系统将在"
                                            + (3 - week)
                                            + "周后自动关闭该房间。<br/><br/>"
                                            + "107间：http://107room.com<br/><br/>"
                                            + "如"
                                            + (3 - week)
                                            + "周后尚未成功出租，也可在网站理房页面重新开放出租<br/><br/>"
                                            + "感谢你对107间的支持，让租房真实而简单！",
                                    HtmlEmail.class, EmailAccount.ADMIN);
                }
            }
        } else if (week == 3) {
            for (User user : users) {
                if (!StringUtils.isEmpty(user.getEmail())) {
                    EmailUtils
                            .sendMail(
                                    user.getEmail(),
                                    "107间--关房提醒",
                                    "提醒邮件：<br/><br/>"
                                            + user.getUsername()
                                            + "你好，你的房间已发布"
                                            + week
                                            + "周。<br/><br/>"
                                            + "<span style=\"color:red;font-weight:bold;\">为保证时效性，系统将自动关闭该房间。</span><br/><br/>"
                                            + "107间：http://107room.com<br/><br/>"
                                            + "如尚未成功出租，也可在网站理房页面重新开放出租。<br/><br/>"
                                            + "未来成功出租后也请到理房页面关闭出租，方便租客浏览有效房间。<br/><br/>"
                                            + "感谢你对107间的支持，让租房真实而简单！",
                                    HtmlEmail.class, EmailAccount.ADMIN);
                }
            }
        }
    }

    @Override
    public void closeAll(String username, long houseId) {
        Validate.notNull(username);
        House house = getHouse(houseId);
        // check auth
        if (StringUtils.equals(username, house.getUsername())) {
            houseDao.closeAll(houseId);
        }
    }

    @Override
    public List<House> getHousesByContact(String telephone, String wechat,
            String qq) {
        Set<Long> houseIds = new HashSet<Long>();
        List<House> result = new ArrayList<House>();
        if (StringUtils.isNotEmpty(telephone)) {
            List<House> houses = houseDao.getHousesByTelephone(telephone);
            if (houses != null) {
                for (House house : houses) {
                    if (!houseIds.contains(house.getId())) {
                        houseIds.add(house.getId());
                        result.add(house);
                    }
                }
            }
        }
        if (StringUtils.isNotEmpty(wechat)) {
            List<House> houses = houseDao.getHousesByWechat(wechat);
            if (houses != null) {
                for (House house : houses) {
                    if (!houseIds.contains(house.getId())) {
                        houseIds.add(house.getId());
                        result.add(house);
                    }
                }
            }
        }
        if (StringUtils.isNotEmpty(qq)) {
            List<House> houses = houseDao.getHousesByQq(qq);
            if (houses != null) {
                for (House house : houses) {
                    if (!houseIds.contains(house.getId())) {
                        houseIds.add(house.getId());
                        result.add(house);
                    }
                }
            }
        }
        return result;
    }

}
