package room107.service.house;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import room107.datamodel.Poi;
import room107.datamodel.PoiHouse;
import room107.datamodel.PoiType;

/**
 * @author WangXiao
 */
public interface IPoiService {

    /**
     * @param names
     *            POI names
     */
    Map<Long, Collection<PoiHouse>> getHousePoiMap(Collection<String> names);

    List<Poi> getPois(long houseId);

    /**
     * Remove old records and add the new ones, if parameter is not empty.
     * 
     * @param type
     *            non-null
     */
    void updatePoiHouses(long houseId, PoiType type,
            Collection<PoiHouse> poiHouses);

}
