package room107.service.house;

import java.util.List;

import room107.dao.house.IHouseDao;
import room107.datamodel.House;
import room107.datamodel.HouseAndRoom;
import room107.datamodel.HouseAndUser;
import room107.datamodel.HouseFavor;
import room107.datamodel.Room;
import room107.datamodel.Suite;
import room107.datamodel.User;
import room107.service.house.search.SearchInfo;
import room107.service.user.InsufficientBalanceException;
import room107.web.house.HouseContact;

/**
 * Usually validation is needed for methods with "username" as a parameter.
 * 
 * @author WangXiao
 */
public interface IHouseService {

    Suite getSuiteByRoom(long roomId);

    Suite getSuiteByHouse(long houseId);

    List<HouseAndUser> getHouseAndUsers(int maxSize);
    
    List<HouseAndUser> getHouseAndUsers(String searchKey);

    House getHouse(long houseId);

    List<House> getHouses();

    List<Room> getRooms(long houseId);

    List<Room> getImageUnbackupedRooms();

    HouseAndRoom getHouseAndRoom(long houseId);

    /**
     * Get only when the specified user is the owner of the specified house.
     */
    HouseAndRoom getHouseAndRoom(String username, long houseId);

    Long addHouseAndRooms(User user, House house, List<Room> rooms);

    void addRooms(String username, long houseId, List<Room> rooms);

    void updateHouse(House house);

    void updateHouse(String username, House house);

    void updateRoom(Room room);
    
    void updateRoomReportUsers(long houseId, Long roomId, User user, String reportType, String extralInfo);

    void deleteRoom(String username, long roomId);

    /**
     * @see IHouseDao#closeAll(long)
     */
    void closeAll(String username, long houseId);

    /**
     * Add when no room ID.
     */
    Room updateOrAddRoom(String username, Room room);
    
    void updateHouseReportUsers(long houseId, User user, String reportType, String extraInfo);

    /**
     * update house status without validation.
     */
    void updateHouseStatus(long houseId, int status);

    /**
     * update house status with username validation.
     */
    void updateHouseStatus(String username, long houseId, int status);

    /**
     * update room status without validation.
     */
    void updateRoomStatus(long roomId, int status);
    
    /**
     * update room status with username validation.
     */
    void updateRoomStatus(String username, long roomId, int status);

    void updateCover(long houseId, String imageId);

    /**
     * Raised by system not user.
     */
    void update(Room room);

    SearchInfo getFavors(String username);

    /**
     * Add when not exist.
     */
    void updateFavor(HouseFavor houseFavor);

    /**
     * Temp.
     */
    Long getHouseId(String username);
    
    /**
     * get houses by telephone, wechat, qq.
     * suppose none or only one house should returned.
     */
    List<House> getHousesByContact(String telephone, String wechat, String qq);

    /**
     * @param user
     *            non-null
     * @return house contact of the house landlord
     * @throws InsufficientBalanceException
     *             when user hasn't applied for this house, and has no
     *             sufficient balance at present
     */
    HouseContact applyForHouse(User user, long houseId)
            throws InsufficientBalanceException;

    int getRejectedHouseCount();

    void closeDeprecated();
    
    void remindDeprecated(int week);
    
    public static enum ReportType{
        UNKNOWN, RENTED, IS_BROKER, OTHER_REASON;        
    }
}
