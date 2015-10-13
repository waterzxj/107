package room107.dao.house;

import java.util.Date;
import java.util.List;

import room107.dao.IDao;
import room107.datamodel.AuditStatus;
import room107.datamodel.GenderType;
import room107.datamodel.House;
import room107.datamodel.RentType;
import room107.datamodel.Room;
import room107.datamodel.UserStatus;

/**
 * Handle {@link House} and {@link Room}.
 * 
 * @author WangXiao
 */
public interface IHouseDao extends IDao {

    House getHouse(long houseId);

    House getHouseByRoom(long roomId);

    Room getRoom(long roomId);

    Room getNonBedroomByType(long houseId, int roomType);

    List<House> getAllHouses();
    
    List<House> getAllHouses(int maxSize);

    List<String> getHousePhotos();

    /**
     * @param from
     *            null when no limit
     */
    List<House> getAvailableHousesByAuditTime(Date from);

    List<House> getHouses(String username);
    
    List<House> getHousesByTelephone(String telephone);
    
    List<House> getHousesByWechat(String wechat);
    
    List<House> getHousesByQq(String qq);

    List<House> getUnlocatedHouses();

    /**
     * @param maxResults
     *            no limit when <=0
     */
    List<House> getHousesByDistricts(List<String> districts, int maxResults);
    
    List<House> getHousesByModifiedTime(Date from, Date to);

    int getHouseCount(AuditStatus auditStatus);

    List<Room> getRooms(long houseId);

    List<Room> getImageUnbackupedRooms();

    /**
     * @param rentType
     *            no limit when null
     * @param modifiedTimeFrom
     *            no limit when null
     */
    List<HouseResult> search(RentType rentType, int priceFrom, int priceTo,
            GenderType gender, Date modifiedTimeFrom);

    long saveHouse(House house);

    long saveRoom(Room room);

    void saveOrUpdateRoom(Room room);

    /**
     * @param house
     *            house instance managed by Hibernate
     */
    void updateHouse(House house, boolean positionChanged);

    void updateRoom(Room room);

    void deleteRoom(long id);

    void deleteRoom(Room room);

    /**
     * @param to
     *            modified time, inclusive, non-null
     */
    void close(UserStatus status, Date to);

    /**
     * Close specified house and its rooms.
     */
    void closeAll(long houseId);
    
    void openAll(long houseId);
    
    int getAllHouseCount(Date start); 
    
    int getAllBedRoomCount();
    
    int getEffectiveHouseCount(Date start);
    
    int getHouseCountBySource(Date start,UserStatus userStatus);
    
    int getEffectiveHouseCountBySource(Date start,UserStatus userStatus);
    
 
}
