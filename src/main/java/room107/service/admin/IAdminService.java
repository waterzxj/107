package room107.service.admin;

import java.util.List;

import room107.dao.UsernameExistException;
import room107.datamodel.DoubanPoster;
import room107.datamodel.House;
import room107.datamodel.Room;
import room107.datamodel.User;

/**
 * @author WangXiao
 */
public interface IAdminService {

    /**
     * @param user
     *            non-null
     * @param house
     *            non-null
     * @param rooms
     *            non-null
     */
    void save(User user, House house, List<Room> rooms) throws UsernameExistException;

    List<DoubanPoster> getDoubanPosters();

}
