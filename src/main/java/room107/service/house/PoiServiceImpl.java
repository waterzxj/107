package room107.service.house;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import room107.dao.house.IPoiHouseDao;
import room107.datamodel.Poi;
import room107.datamodel.PoiHouse;
import room107.datamodel.PoiType;

/**
 * @author WangXiao
 */
@Service
public class PoiServiceImpl implements IPoiService {

    @Autowired
    private IPoiHouseDao poiHouseDao;

    @Override
    public List<Poi> getPois(long houseId) {
        List<PoiHouse> poiHouses = poiHouseDao.getByHouse(houseId);
        if (poiHouses.isEmpty()) {
            return Collections.emptyList();
        }
        List<Poi> pois = new ArrayList<Poi>();
        for (PoiHouse poiHouse : poiHouses) {
            pois.add(new Poi(poiHouse));
        }
        return pois;
    }

    @Override
    public Map<Long, Collection<PoiHouse>> getHousePoiMap(
            Collection<String> names) {
        List<PoiHouse> poiHouses = poiHouseDao.getByNames(names);
        if (poiHouses.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Collection<PoiHouse>> result = new HashMap<Long, Collection<PoiHouse>>();
        for (PoiHouse poiHouse : poiHouses) {
            Long houseId = poiHouse.getHouseId();
            Collection<PoiHouse> c = result.get(houseId);
            if (c == null) {
                result.put(houseId, c = new ArrayList<PoiHouse>());
            }
            c.add(poiHouse);
        }
        return result;
    }

    @Override
    public void updatePoiHouses(long houseId, PoiType type,
            Collection<PoiHouse> poiHouses) {
        Validate.notNull(type);
        if (poiHouses.isEmpty()) {
            return;
        }
        /*
         * remove
         */
        poiHouseDao.remove(houseId, type);
        /*
         * add
         */
        poiHouseDao.addAll(poiHouses);
    }

}
