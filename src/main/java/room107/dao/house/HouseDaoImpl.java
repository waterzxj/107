package room107.dao.house;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import room107.Constants;
import room107.dao.impl.DaoImpl;
import room107.datamodel.AuditStatus;
import room107.datamodel.GenderType;
import room107.datamodel.House;
import room107.datamodel.RentStatus;
import room107.datamodel.RentType;
import room107.datamodel.Room;
import room107.datamodel.RoomType;
import room107.datamodel.UserStatus;
import room107.service.message.MessageService;
import room107.service.message.type.HousePositionChanged;
import room107.service.message.type.RoomStatusChanged;
import room107.util.UserBehaviorLog;

/**
 * @author WangXiao
 */
@CommonsLog
@Repository
@SuppressWarnings("unchecked")
public class HouseDaoImpl extends DaoImpl implements IHouseDao {

    @Autowired
    private MessageService messageService;

    @Override
    public House getHouse(long houseId) {
        return get(House.class, houseId);
    }

    @Override
    public Room getRoom(long roomId) {
        return get(Room.class, roomId);
    }

    @Override
    public House getHouseByRoom(long roomId) {
        Long houseId = (Long) getSession().createCriteria(Room.class)
                .add(Restrictions.eq("id", roomId))
                .setProjection(Projections.property("houseId")).uniqueResult();
        return houseId == null ? null : getHouse(houseId);
    }

    @Override
    public List<House> getAllHouses() {
        return getAllHouses(500);
    }

    @Override
    public List<House> getAllHouses(int maxSize) {
        return getSession().createCriteria(House.class)
                .add(Restrictions.ne("status", STATUS_DELETED))
                .addOrder(Order.desc("id")).setMaxResults(maxSize).list();
    }

    @Override
    public List<House> getHouses(String username) {
        Validate.notNull(username);
        return getSession().createCriteria(House.class)
                .add(Restrictions.eq("username", username))
                .add(Restrictions.ne("status", STATUS_DELETED))
                .addOrder(Order.desc("id")).list();
    }

    @Override
    public List<String> getHousePhotos() {
        List<String> imageIds = getSession().createCriteria(House.class)
                .setProjection(Projections.property("imageId")).list();
        imageIds.addAll(getSession().createCriteria(Room.class)
                .setProjection(Projections.property("imageIds")).list());
        List<String> result = new ArrayList<String>(imageIds.size() * 2);
        for (String imgId : imageIds) {
            String[] ss = StringUtils.split(imgId,
                    Constants.MULTI_VALUE_SEPARATOR);
            if (!ArrayUtils.isEmpty(ss)) {
                for (String s : ss) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    @Override
    public List<House> getAvailableHousesByAuditTime(Date from) {
        Criteria criteria = getSession()
                .createCriteria(House.class)
                .add(Restrictions.eq("status", RentStatus.OPEN.ordinal()))
                .add(Restrictions.eq("auditStatus",
                        AuditStatus.ACCEPTED.ordinal()))
                .addOrder(Order.desc("id"));
        if (from != null) {
            criteria.add(Restrictions.ge("auditTime", from));
        }
        return criteria.list();
    }

    @Override
    public List<House> getUnlocatedHouses() {
        return getSession()
                .createCriteria(House.class)
                .add(Restrictions.or(Restrictions.isNull("locationX"),
                        Restrictions.isNull("locationY")))
                .addOrder(Order.desc("id")).list();
    }

    @Override
    public List<House> getHousesByDistricts(List<String> districts,
            int maxResults) {
        Criteria criteria = getSession()
                .createCriteria(House.class)
                .add(Restrictions.eq("status", RentStatus.OPEN.ordinal()))
                .add(Restrictions.eq("auditStatus",
                        AuditStatus.ACCEPTED.ordinal()))
                .add(Restrictions.in("city", districts))
                .addOrder(Order.desc("rank"));
        if (maxResults > 0) {
            criteria.setMaxResults(maxResults);
        }
        return criteria.list();
    }

    @Override
    public List<House> getHousesByModifiedTime(Date from, Date to) {
        Criteria criteria = getSession()
                .createCriteria(House.class)
                .add(Restrictions.eq("userStatus", UserStatus.NORMAL.ordinal()))
                .add(Restrictions.eq("status", RentStatus.OPEN.ordinal()))
                .add(Restrictions.eq("auditStatus",
                        AuditStatus.ACCEPTED.ordinal()))
                .add(Restrictions.ge("modifiedTime", from))
                .add(Restrictions.le("modifiedTime", to));
        return criteria.list();
    }

    @Override
    public int getHouseCount(AuditStatus auditStatus) {
        Criteria criteria = getSession().createCriteria(House.class);
        if (criteria != null) {
            criteria.add(Restrictions.eq("auditStatus", auditStatus.ordinal()));
        }
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();

    }

    @Override
    public List<Room> getRooms(long houseId) {
        return getSession().createCriteria(Room.class)
                .add(Restrictions.ne("status", STATUS_DELETED))
                .add(Restrictions.eq("houseId", houseId))
                .addOrder(Order.asc("type")).list();
    }

    @Override
    public List<Room> getImageUnbackupedRooms() {
        return getSession().createCriteria(Room.class)
                .add(Restrictions.eq("imageBackuped", false)).list();
    }

    @Override
    public long saveHouse(House house) {
        long result = (Long) getSession().save(house);
        messageService.send(new HousePositionChanged(house));
        return result;
    }

    @Override
    public long saveRoom(Room room) {
        long result = (Long) getSession().save(room);
        messageService.send(new RoomStatusChanged(room.getHouseId(), room
                .getId(), room.getStatus()));
        return result;
    }

    @Override
    public void saveOrUpdateRoom(Room room) {
        getSession().saveOrUpdate(room);
        messageService.send(new RoomStatusChanged(room.getHouseId(), room
                .getId(), room.getStatus()));
    }

    @Override
    public List<HouseResult> search(RentType rentType, int priceFrom, int priceTo,
            GenderType gender, Date modifiedTimeFrom) {
        Validate.isTrue(priceFrom <= priceTo);
        List<HouseResult> result = new ArrayList<HouseResult>(512);

        Integer[] genderTypes;
        if (gender == null || gender == GenderType.UNKNOWN) {
            genderTypes = new Integer[]{GenderType.UNKNOWN.ordinal(), GenderType.FEMALE.ordinal(),
                    GenderType.MALE.ordinal(), GenderType.MALE_AND_FEMALE.ordinal()};
        } else if (gender == GenderType.MALE) {
            genderTypes = new Integer[]{GenderType.UNKNOWN.ordinal(), GenderType.MALE.ordinal(),
                    GenderType.MALE_AND_FEMALE.ordinal()};
        } else if (gender == GenderType.FEMALE) {
            genderTypes = new Integer[]{GenderType.UNKNOWN.ordinal(), GenderType.FEMALE.ordinal(),
                    GenderType.MALE_AND_FEMALE.ordinal()};
        } else {
            genderTypes = new Integer[]{GenderType.UNKNOWN.ordinal(), GenderType.MALE_AND_FEMALE.ordinal()};
        }
        
        /*
         * by house
         */
        if (rentType == null || rentType.isRentByHouse()) {
            List<House> houses = searchHouses(RentType.BY_HOUSE_VALUES,
                    priceFrom, priceTo, genderTypes, modifiedTimeFrom);
            for (House house : houses) {
                result.add(new HouseResult(house));
            }
        }
        /*
         * by room
         */
        if (rentType == null || rentType.isRentByRoom()) {
            List<House> houses = searchHouses(RentType.BY_ROOM_VALUES, null,
                    null, genderTypes, null);
            if (!houses.isEmpty()) { // search rooms
                Map<Long, House> houseMap = new HashMap<Long, House>(
                        houses.size());
                for (House house : houses) {
                    houseMap.put(house.getId(), house);
                }
                Criteria criteria = getSession()
                        .createCriteria(Room.class)
                        .add(Restrictions.eq("status",
                                RentStatus.OPEN.ordinal()))
                        .add(Restrictions.in("houseId", houseMap.keySet()))
                        .add(Restrictions.le("type", RoomType.次卧.ordinal()))
                        .add(Restrictions.between("price", priceFrom, priceTo));
                if (modifiedTimeFrom != null) {
                    criteria.add(Restrictions.ge("modifiedTime",
                            modifiedTimeFrom));
                }
                List<Room> rooms = criteria.addOrder(Order.desc("id")).list();
                for (Room room : rooms) {
                    House house = houseMap.get(room.getHouseId());
                    result.add(new RoomResult(house, room));
                }
            }
        }
        return result;
    }

    /**
     * @param rentType
     *            assert non-null
     */
    private List<House> searchHouses(Integer[] rentTypes, Integer priceFrom,
            Integer priceTo, Integer[] genderTypes, Date modifiedTimeFrom) {
        Criteria criteria = getSession()
                .createCriteria(House.class)
                .add(Restrictions.eq("status", RentStatus.OPEN.ordinal()))
                .add(Restrictions.eq("auditStatus",
                        AuditStatus.ACCEPTED.ordinal()))
                .add(Restrictions.in("rentType", rentTypes))
                .add(Restrictions.or(
                        Restrictions.in("requiredGender", genderTypes),
                        Restrictions.isNull("requiredGender")));
        // optional price
        if (priceFrom != null || priceTo != null) {
            if (priceFrom == null) {
                priceFrom = 0;
            } else if (priceTo == null) {
                priceTo = Integer.MAX_VALUE;
            }
            criteria.add(Restrictions.between("price", priceFrom, priceTo));
        }
        if (modifiedTimeFrom != null) {
            criteria.add(Restrictions.ge("modifiedTime", modifiedTimeFrom));
        }
        return criteria.addOrder(Order.desc("id")).list();
    }

    @Override
    public void updateHouse(House house, boolean positionChanged) {
        UserBehaviorLog.HOUSE_UPDATE.info("Update house: username="
                + house.getUsername() + ", houseId=" + house.getId());
        house.setModifiedTime(new Date());
        getSession().merge(house);
        if (positionChanged) {
            messageService.send(new HousePositionChanged(house));
        }
    }

    @Override
    public void updateRoom(Room room) {
        UserBehaviorLog.HOUSE_UPDATE.info("Update room: username="
                + room.getUsername() + ", roomId=" + room.getId());
        room.setModifiedTime(new Date());
        getSession().update(room);
        messageService.send(new RoomStatusChanged(room.getHouseId(), room
                .getId(), room.getStatus()));
    }

    @Override
    public void deleteRoom(long id) {
        delete(getRoom(id));
    }

    @Override
    public void deleteRoom(Room room) {
        if (room == null) {
            return;
        }
        delete(getRoom(room.getId()));
    }

    /**
     * Soft delete.
     */
    @Override
    public void delete(Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof House) {
            ((House) object).setStatus(STATUS_DELETED);
        } else {
            ((Room) object).setStatus(STATUS_DELETED);
        }
        update(object);
    }

    @Override
    public Room getNonBedroomByType(long houseId, int roomType) {
        return (Room) getSession().createCriteria(Room.class)
                .add(Restrictions.ne("status", STATUS_DELETED))
                .add(Restrictions.eq("houseId", houseId))
                .add(Restrictions.eq("type", roomType)).uniqueResult();
    }

    @Override
    public void close(UserStatus status, Date to) {
        Validate.notNull(to);
        String time = new SimpleDateFormat("yyyy-MM-dd").format(to);
        String sql = "update house set status=1 where status=0 and modifiedTime<='"
                + time + "' and userStatus=" + status.ordinal();
        int houseCount = getSession().createSQLQuery(sql).executeUpdate();
        String roomSql = "UPDATE room, house SET room.status=1 "
                + "WHERE house.rentType=1 and house.status=1 AND room.status=0 AND room.houseId=house.id";
        int roomCount = getSession().createSQLQuery(roomSql).executeUpdate();
        log.info("Close: time=" + time + ", houseCount=" + houseCount
                + ", roomCount=" + roomCount);
    }

    @Override
    public void closeAll(long houseId) {
        int closed = RentStatus.CLOSED.ordinal();
        getSession().createSQLQuery(
                "update house set status=" + closed + " where id=" + houseId)
                .executeUpdate();
        getSession().createSQLQuery(
                "update room set status=" + closed + " where houseId="
                        + houseId + " and status=0").executeUpdate();
    }

    @Override
    public void openAll(long houseId) {
        int opened = RentStatus.OPEN.ordinal();
        getSession().createSQLQuery(
                "update house set status=" + opened + " where id=" + houseId)
                .executeUpdate();
        List<Room> rooms = getRooms(houseId);
        for (Room room : rooms) {
            if (room.isRentable()) {
                getSession().createSQLQuery(
                        "update room set status=" + opened + " where id="
                                + room.getId()).executeUpdate();
            }
        }
    }

    @Override
    public List<House> getHousesByTelephone(String telephone) {
        return getSession().createCriteria(House.class)
                .add(Restrictions.eq("telephone", telephone)).list();
    }

    @Override
    public List<House> getHousesByWechat(String wechat) {
        return getSession().createCriteria(House.class)
                .add(Restrictions.eq("wechat", wechat)).list();
    }

    @Override
    public List<House> getHousesByQq(String qq) {
        return getSession().createCriteria(House.class)
                .add(Restrictions.eq("qq", qq)).list();
    }

    @Override
    public int getAllHouseCount(Date start) {
        Criteria criteria = getSession().createCriteria(House.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();
    }

    @Override
    public int getAllBedRoomCount() {
        Criteria criteria = getSession().createCriteria(Room.class).add(
                Restrictions.in("type", new Integer[] { RoomType.主卧.ordinal(), RoomType.次卧.ordinal() }));
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();
    }

    @Override
    public int getEffectiveHouseCount(Date start) {
        Criteria criteria = getSession().createCriteria(House.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        criteria.add(Restrictions.eq("status", RentStatus.OPEN.ordinal()))
            .add(Restrictions.eq("auditStatus", AuditStatus.ACCEPTED.ordinal()));
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();

    }

    @Override
    public int getHouseCountBySource(Date start,UserStatus userStatus) {
        Criteria criteria = getSession().createCriteria(House.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        criteria.add(Restrictions.eq("userStatus",userStatus.ordinal()));
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();
    }

    @Override
    public int getEffectiveHouseCountBySource(Date start,UserStatus userStatus) {
        Criteria criteria = getSession().createCriteria(House.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        criteria.add(Restrictions.eq("userStatus", userStatus.ordinal()))
            .add(Restrictions.eq("status", RentStatus.OPEN.ordinal()))
            .add(Restrictions.eq("auditStatus", AuditStatus.ACCEPTED.ordinal()));
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();
    }

}
