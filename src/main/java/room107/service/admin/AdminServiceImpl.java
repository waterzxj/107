package room107.service.admin;

import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.Validate;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.IDoubanPosterDao;
import room107.dao.IUserDao;
import room107.dao.UsernameExistException;
import room107.dao.house.IHouseDao;
import room107.datamodel.DoubanPoster;
import room107.datamodel.House;
import room107.datamodel.Room;
import room107.datamodel.RoomType;
import room107.datamodel.User;
import room107.util.HouseUtils;

/**
 * @author WangXiao
 */
@CommonsLog
@Service
@Transactional
public class AdminServiceImpl implements IAdminService {

    private Scheduler scheduler;

    @Autowired
    private IHouseDao houseDao;

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IDoubanPosterDao doubanPosterDao;

    public AdminServiceImpl() throws Exception {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
    }

    @Override
    public void save(User user, House house, List<Room> rooms) throws UsernameExistException {
        Validate.notNull(user);
        Validate.notNull(house);
        Validate.notNull(rooms);
        userDao.createUser(user);
        String username = user.getUsername();
        log.debug("Saved: username=" + username);
        house.setUsername(username);
        Long houseId = houseDao.saveHouse(house);
        log.debug("Saved: houseId=" + houseId);
        // for handle room name
        int[] roomTypeCounts = new int[RoomType.values().length];
        for (Room room : rooms) {
            roomTypeCounts[room.getType()]++;
        }
        int[] roomTypeIndex = new int[roomTypeCounts.length];
        for (Room room : rooms) {
            room.setUsername(username);
            room.setHouseId(houseId);
            int type = room.getType();
            String typeString = HouseUtils.getRoomType(type);
            if (roomTypeCounts[type] > 1) {
                room.setName(typeString + (++roomTypeIndex[type]));
            } else {
                room.setName(typeString);
            }
            Long roomId = houseDao.saveRoom(room);
            log.debug("Saved: roomId=" + roomId);
        }
    }

    @Override
    public List<DoubanPoster> getDoubanPosters() {
        return doubanPosterDao.getAll(DoubanPoster.class);
    }

}
