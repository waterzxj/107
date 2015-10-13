package room107.dao.house;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import room107.datamodel.PoiHouse;
import room107.datamodel.PoiType;
import room107.tool.AutowiredTest;

public class PoiHouseDaoImplTest extends AutowiredTest {

    @Autowired
    private IPoiHouseDao poiHouseDao;

    @Test
    public void test() {
        // List<PoiHouse> poiHouses = Arrays.asList(
        // new PoiHouse(PoiType.SUBWAY.ordinal(), "地铁10号线", 1, 1.1, 2.2,
        // 1000, "知春路", new Date()),
        // new PoiHouse(PoiType.SUBWAY.ordinal(), "地铁10号线", 2, 1.2, 2.3,
        // 1000, "海淀黄庄", new Date()),
        // new PoiHouse(PoiType.SUBWAY.ordinal(), "地铁13号线", 1, 1.3, 2.4,
        // 1000, "知春路", new Date()));
        // poiHouseDao.addAll(poiHouses);
        // poiHouseDao.removeByHouse(1);
        // poiHouseDao.removeByHouse(2);
        System.out.println(poiHouseDao.getByNames(Arrays.asList("地铁10号线")));
    }

}
