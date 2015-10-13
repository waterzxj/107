package room107.service.house.index;

import java.util.Collection;

import room107.datamodel.Poi;
import room107.datamodel.PoiType;

/**
 * @author WangXiao
 */
public interface IHouseIndexer {

    /**
     * Index the surrounding POIs.
     * 
     * @param type
     *            non-null
     */
    void indexSurroundingPois(long houseId, PoiType type, Collection<Poi> pois);

}
