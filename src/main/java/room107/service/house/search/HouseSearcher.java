package room107.service.house.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import room107.dao.IUserDao;
import room107.dao.house.HouseResult;
import room107.dao.house.IHouseDao;
import room107.datamodel.User;
import room107.service.house.search.line.ILineSearcher;
import room107.service.house.search.line.LineQuery;
import room107.service.house.search.position.IPositionSearcher;
import room107.service.house.search.position.PositionQuery;

/**
 * @author WangXiao
 */
@Service
@CommonsLog
public class HouseSearcher implements IHouseSearcher {

    @Autowired
    private IHouseDao houseDao;

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IPositionSearcher positionSearcher;

    @Autowired
    private ILineSearcher lineSearcher;

    @Override
    public SearchInfo search(HouseQuery query, int maxCount) {
        SearchInfo searchInfo = new SearchInfo();
    	
        Validate.notNull(query);
        if (maxCount <= 0) {
            maxCount = Integer.MAX_VALUE;
        }
        /*
         * by basic condition
         */

        List<HouseResult> candidates = houseDao.search(
                query.getRentType(), query.getPriceFrom(), query.getPriceTo(),
                query.getGender(), query.getModifiedTimeFrom());
        if (log.isDebugEnabled()) {
            log.debug("Condition result: " + candidates);
        }
        if (candidates.isEmpty()) {
            return searchInfo;
        }
        
        /*
         * by positions
         */
        SearchInfo searchResult = new SearchInfo();
        
        if (!ArrayUtils.isEmpty(query.getKeywords())
                || query.getLocation() != null) {
            searchResult = search(query, candidates);
        } else {
            searchResult.setHouseResults(candidates);
        }
        /*
         * sort
         */
        List<HouseResult> result = Collections.emptyList();
        if (!searchResult.getHouseResults().isEmpty()) {
            ArrayList<HouseResult> sorted = new ArrayList<HouseResult>(
                    searchResult.getHouseResults());
            Collections.sort(sorted);
            result = maxCount >= sorted.size() ? sorted : sorted.subList(0,
                    maxCount);
        }
        fillUser(result);
        log.debug("Final result: " + result);
        
        searchInfo.setHouseResults(result);
        searchInfo.setQueryLocations(searchResult.getQueryLocations());
        
        return searchInfo;
    }
    
    private void fillUser(List<HouseResult> result) {
        List<String> usernames = new ArrayList<String>();
        for (HouseResult houseResult : result) {
            String username = houseResult.getUserName();
            usernames.add(username);
        }
        List<User> users = userDao.getUsers(usernames);
        Map<String, User> name2User = new HashMap<String, User>();
        for (User user : users) {
            name2User.put(user.getUsername(), user);
        }
        for (HouseResult houseResult : result) {
            houseResult.setUser(name2User.get(houseResult.getUserName()));
        }
    }

    private SearchInfo search(HouseQuery query,
            Collection<HouseResult> candidates) {
    	SearchInfo result = new SearchInfo();
    	
        /*
         * non-empty keywords
         */
        List<String> positions = new ArrayList<String>(), lines = new ArrayList<String>();
        /*
         * normalize
         */
        if (!ArrayUtils.isEmpty(query.getKeywords())) {
            for (String keyword : query.getKeywords()) {
                String normal = LineQuery.normalize(keyword);
                if (normal == null) {
                    positions.add(keyword);
                } else {
                    lines.add(normal);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Nomailized: positions=" + positions + ", lines=" + lines);
        }
        
        /*
         * by position
         */
        PositionQuery positionQuery = new PositionQuery(positions);
        positionQuery.setLocation(query.getLocation());
        SearchInfo positionResult = positionSearcher.search(positionQuery, candidates);
        result.getHouseResults().addAll(positionResult.getHouseResults());
        /*
         * by line
         */
        result.getHouseResults().addAll(lineSearcher.search(lines, candidates));
        result.setQueryLocations(positionResult.getQueryLocations());
        
        return result;
    }

}
