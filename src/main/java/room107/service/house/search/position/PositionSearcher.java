package room107.service.house.search.position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import room107.dao.house.HouseResult;
import room107.datamodel.House;
import room107.datamodel.Location;
import room107.service.house.search.SearchInfo;
import room107.service.location.ILocator;
import room107.util.GeographyUtils;

/**
 * @author yanghao
 */
@CommonsLog
@Component
public class PositionSearcher implements IPositionSearcher {

    @Qualifier(value = "cachedLocator")
    @Autowired
    private ILocator locator;

    @Override
    public SearchInfo search(PositionQuery query,
            Collection<HouseResult> candidates) {
    	SearchInfo resultInfo = new SearchInfo();
    	
        if (log.isDebugEnabled()) {
            log.debug("Query: " + query);
        }
        /*
         * search
         */
        if (query == null || query.isEmpty()
                || CollectionUtils.isEmpty(candidates)) {
            return resultInfo;
        }
        Set<HouseResult> resultSet = new LinkedHashSet<HouseResult>(
                candidates.size());
        /*
         * location
         */
        searchByLocation(0, query.getLocation(), candidates, resultSet);
        if (log.isDebugEnabled()) {
            log.debug("By location: " + resultSet);
        }
        /*
         * position
         */
        searchByPosition(query, candidates, resultSet, resultInfo.getQueryLocations());
        if (log.isDebugEnabled()) {
            log.debug("By positions: " + resultSet);
        }
        /*
         * district
         */
        searchByDistrict(query, candidates, resultSet);
        if (log.isDebugEnabled()) {
            log.debug("By districts: " + resultSet);
        }

        List<HouseResult> result = new ArrayList<HouseResult>(resultSet);
        if (log.isDebugEnabled()) {
            log.debug("Position result: " + result);
        }
        resultInfo.setHouseResults(result);
        
        return resultInfo;
    }

    private void searchByLocation(int scorePriority, Location location,
            Collection<HouseResult> candidates, Collection<HouseResult> result) {
        if (location == null) {
            return;
        }
        for (HouseResult candidate : candidates) {
            House house = candidate.getHouse();
            if (house.getLocationX() != null && house.getLocationY() != null) {
                double distance = GeographyUtils.getDistance(location.getX(),
                        location.getY(), house.getLocationX(),
                        house.getLocationY());
                if (distance <= SEARCH_POSITION_RADIUS) {
                    //scorer.score(scorePriority, distance);
                    if (candidate.getDistance() > distance) {
                        candidate.setDistance((long)distance);
                    }
                    result.add(candidate);
                }
            }
        }
    }

    private void searchByPosition(PositionQuery query,
            Collection<HouseResult> candidates, Collection<HouseResult> result, List<Location> locations) {
        if (ArrayUtils.isEmpty(query.getPositions())) {
            return;
        }

        int i = 1;
        for (String position : query.getPositions()) {
            try {
                Location location = locator.getLocation(position);
                if (location == null) {
                    log.warn("empty location, position = " + position);
                    continue;
                }
                location.setAddress(position);
                if (location != null) {
                    // search by location
                    locations.add(location);
                    searchByLocation(i++, location, candidates, result);
                }
            } catch (Exception e) {
                log.error("Search by positoin failed: position=" + position, e);
            }
        }
        
    }

    private void searchByDistrict(PositionQuery query,
            Collection<HouseResult> candidates, Set<HouseResult> result) {
        if (ArrayUtils.isEmpty(query.getDistricts())) {
            return;
        }
        for (HouseResult candidate : candidates) {
            if (!result.contains(candidate)
                    && ArrayUtils.contains(query.getDistricts(), candidate
                            .getHouse().getCity())) {
                result.add(candidate);
            }
        }
    }

}
