package room107.service.location;

import java.util.Date;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import room107.dao.house.IPositionLocationDao;
import room107.datamodel.Location;
import room107.datamodel.Poi;
import room107.datamodel.PoiType;
import room107.datamodel.PositionLocation;

/**
 * @author WangXiao
 */
@CommonsLog
@Component
public class CachedLocator implements ILocator {

    @Qualifier(value = "baiduLocator")
    @Autowired
    private ILocator baiduLocator;
    
    @Qualifier(value = "amapLocator")
    @Autowired
    private ILocator amapLocator;

    /**
     * As cache.
     */
    @Autowired
    private IPositionLocationDao positionLocationDao;

    @Override
    public Location getLocation(String position) throws Exception {
        Location location = positionLocationDao.getLocation(position);
        if (location == null || !location.isValid()) {
            location = baiduLocator.getLocation(position);
            if (location == null || !location.isValid()) {
                location = amapLocator.getLocation(position);
            }
            if (location != null && location.isValid()) {
                PositionLocation pl = positionLocationDao.get(PositionLocation.class, position);
                if (pl != null) {
                    pl.setX(location.getX());
                    pl.setY(location.getY());
                    positionLocationDao.update(pl);
                } else {
                    positionLocationDao.save(new PositionLocation(
                            position, location.getX(), location.getY(), new Date()));
                }
            } else {
                log.warn("Locate failed: position=" + position + ", location="
                        + location);
            }
        }
        return location;
    }

    @Override
    public List<Poi> getNearbyPois(Location location, int radius, PoiType type)
            throws Exception {
        return baiduLocator.getNearbyPois(location, radius, type);
    }

}
