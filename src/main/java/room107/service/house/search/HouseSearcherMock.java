package room107.service.house.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import room107.dao.house.HouseResult;
import room107.dao.house.RoomResult;
import room107.service.house.search.SearchInfo;
import room107.mock.MockHouse;
import room107.mock.MockRoom;

/**
 * @author WangXiao
 */
public class HouseSearcherMock implements IHouseSearcher {

    @Override
    public SearchInfo search(HouseQuery query, int maxCount) {
        SearchInfo searchInfo = new SearchInfo();
        maxCount = RandomUtils.nextInt(60);
        List<HouseResult> result = new ArrayList<HouseResult>();
        for (int i = 0; i < maxCount; i++) {
            result.add(RandomUtils.nextBoolean() ? new HouseResult(
                    new MockHouse()) : new RoomResult(new MockHouse(),
                    new MockRoom()));
        }
        searchInfo.setHouseResults(result);
        return searchInfo;
    }
}
