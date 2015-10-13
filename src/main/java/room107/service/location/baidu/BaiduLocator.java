package room107.service.location.baidu;

import java.util.List;

import org.springframework.stereotype.Component;

import room107.datamodel.Location;
import room107.datamodel.Poi;
import room107.datamodel.PoiType;
import room107.service.location.ILocator;
import room107.util.BaiduMapUtils;

/**
 * @author WangXiao
 */
@Component
public class BaiduLocator implements ILocator {

    @Override
    public Location getLocation(String position) throws Exception {
        return BaiduMapUtils.getLocation(position);
    }

    @Override
    public List<Poi> getNearbyPois(Location location, int radius, PoiType type)
            throws Exception {
        return BaiduMapUtils.getNearbyPois(location, radius, type);
    }

}
