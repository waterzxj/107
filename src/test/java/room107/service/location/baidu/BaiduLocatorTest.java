package room107.service.location.baidu;

import org.junit.Test;

import room107.datamodel.Location;
import room107.datamodel.PoiType;
import room107.service.house.search.position.IPositionSearcher;

public class BaiduLocatorTest {

    private BaiduLocator locator = new BaiduLocator();

    @Test
    public void test() throws Exception {
        Location location = locator.getLocation("知春路");
        System.out.println(location);
        System.out.println(PoiType.SUBWAY);
        System.out.println(locator.getNearbyPois(location,
                IPositionSearcher.SEARCH_SURROUNDING_SUBWAY_RADIUS,
                PoiType.SUBWAY));
        System.out.println(PoiType.BUS);
        System.out.println(locator.getNearbyPois(location,
                IPositionSearcher.SEARCH_SURROUNDING_BUS_RADIUS, PoiType.BUS));
    }

}
