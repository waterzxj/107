package room107.dao.house;

import java.util.Collection;
import java.util.List;

import room107.datamodel.PoiHouse;
import room107.datamodel.PoiType;

/**
 * @author WangXiao
 */
public interface IPoiHouseDao {

    List<PoiHouse> getByNames(Collection<String> names);

    List<PoiHouse> getByHouse(long houseId);

    /**
     * @param type
     *            no limit when null
     */
    void remove(long houseId, PoiType type);

    void addAll(Collection<PoiHouse> poiHouses);

}
