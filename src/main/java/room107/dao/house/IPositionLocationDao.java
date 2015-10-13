package room107.dao.house;

import room107.dao.IDao;
import room107.datamodel.Location;

/**
 * @author WangXiao
 */
public interface IPositionLocationDao extends IDao {

    Location getLocation(String position);

}
