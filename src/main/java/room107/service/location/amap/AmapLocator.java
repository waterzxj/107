/**
 * 
 */
package room107.service.location.amap;

import java.util.List;

import org.springframework.stereotype.Component;

import room107.datamodel.Location;
import room107.datamodel.Poi;
import room107.datamodel.PoiType;
import room107.service.location.ILocator;
import room107.util.AmapUtils;

/**
 * @author yanghao
 */
@Component
public class AmapLocator implements ILocator {
    
    @Override
    public Location getLocation(String position) throws Exception {
        return AmapUtils.getLocation(position);
    }

    @Override
    public List<Poi> getNearbyPois(Location location, int radius, PoiType type)
            throws Exception {
        return null;
    }

}
