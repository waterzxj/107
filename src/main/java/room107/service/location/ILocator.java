package room107.service.location;

import java.util.List;

import room107.datamodel.Location;
import room107.datamodel.Poi;
import room107.datamodel.PoiType;

/**
 * @author WangXiao
 */
public interface ILocator {

    /**
     * Translate position to location.
     * 
     * @return null when not available
     */
    Location getLocation(String position) throws Exception;

    /**
     * Get POIs of specified type and area.
     * 
     * @param location
     *            non-null
     * @param radius
     *            by meter, >0
     * @param type
     *            non-null
     * @return non-null
     */
    List<Poi> getNearbyPois(Location location, int radius, PoiType type)
            throws Exception;

}
