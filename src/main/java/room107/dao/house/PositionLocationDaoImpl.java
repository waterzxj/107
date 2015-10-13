package room107.dao.house;

import org.springframework.stereotype.Repository;

import room107.dao.impl.DaoImpl;
import room107.datamodel.Location;
import room107.datamodel.PositionLocation;

/**
 * @author WangXiao
 */
@Repository
public class PositionLocationDaoImpl extends DaoImpl implements
        IPositionLocationDao {

    @Override
    public Location getLocation(String position) {
        if (position == null) {
            return null;
        }
        PositionLocation pl = get(PositionLocation.class, position);
        return pl == null ? null : new Location(pl.getX(), pl.getY());
    }

}
