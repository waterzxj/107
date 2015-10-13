package room107.service.house.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.datamodel.Poi;
import room107.datamodel.PoiHouse;
import room107.datamodel.PoiType;
import room107.service.house.IPoiService;

/**
 * @author WangXiao
 */
@Component
public class HouseIndexer implements IHouseIndexer {

    @Autowired
    private IPoiService poiService;

    @Override
    public void indexSurroundingPois(long houseId, PoiType type,
            Collection<Poi> pois) {
        Validate.notNull(type);
        if (pois.isEmpty()) {
            return;
        }
        List<PoiHouse> poiHouses = new ArrayList<PoiHouse>(pois.size());
        for (Poi poi : pois) {
            poiHouses.add(new PoiHouse(poi.getType().ordinal(), poi.getName(),
                    houseId, poi.getLocation().getX(),
                    poi.getLocation().getY(), poi.getDistance(), poi
                            .getDisplayName(), new Date()));
        }
        poiService.updatePoiHouses(houseId, type, poiHouses);
    }
}
