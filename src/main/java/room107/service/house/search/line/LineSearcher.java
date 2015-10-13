package room107.service.house.search.line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.dao.house.HouseResult;
import room107.datamodel.PoiHouse;
import room107.service.house.IPoiService;

/**
 * @author WangXiao
 */
@CommonsLog
@Component
public class LineSearcher implements ILineSearcher {

    @Autowired
    private IPoiService poiService;

    @Override
    public List<HouseResult> search(Collection<String> lines,
            Collection<HouseResult> candidates) {
        if (log.isDebugEnabled()) {
            log.debug("Lines: " + lines);
        }
        if (CollectionUtils.isEmpty(lines)
                || CollectionUtils.isEmpty(candidates)) {
            return Collections.emptyList();
        }
        Map<Long, Collection<PoiHouse>> map = poiService.getHousePoiMap(lines);
        if (log.isDebugEnabled()) {
            log.debug("POI map: " + map);
        }
        if (map.isEmpty()) {
            return Collections.emptyList();
        }
        List<HouseResult> result = new ArrayList<HouseResult>();
        for (HouseResult houseResult : candidates) {
            long houseId = houseResult.getHouse().getId();
            Collection<PoiHouse> pois = map.get(houseId);
            if (pois != null) { // TODO add POI
                result.add(houseResult);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Line result: " + result);
        }
        return result;
    }

}
