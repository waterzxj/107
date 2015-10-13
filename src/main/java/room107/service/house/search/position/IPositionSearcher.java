package room107.service.house.search.position;

import java.util.Collection;

import room107.dao.house.HouseResult;
import room107.service.house.search.SearchInfo;

/**
 * Search houses by position.
 * 
 * @author WangXiao
 */
public interface IPositionSearcher {

    /**
     * By meter.
     */
    int SEARCH_POSITION_RADIUS = 3000;
    
    int SHOW_SUBSCRIBER_RADIUS = 4000;
    
    int SEARCH_SURROUNDING_SUBWAY_RADIUS = 1000;
    
    int SEARCH_SURROUNDING_BUS_RADIUS = 1000;

    SearchInfo search(PositionQuery query,
            Collection<HouseResult> candidates);

}
