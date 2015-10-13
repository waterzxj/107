package room107.service.house.search;

import java.util.List;

import room107.dao.house.HouseResult;
import room107.datamodel.Location;
import room107.service.house.search.SearchInfo;

/**
 * @author WangXiao
 */
public interface IHouseSearcher {

    /**
     * @param query
     *            non-null
     * @param maxCount
     *            no limit when <=0
     * @return non-null
     */
    SearchInfo search(HouseQuery query, int maxCount);

}
