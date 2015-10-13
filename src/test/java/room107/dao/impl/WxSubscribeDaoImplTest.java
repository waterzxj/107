package room107.dao.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import room107.dao.IWxSubscribeDao;
import room107.datamodel.Location;
import room107.service.house.search.position.IPositionSearcher;
import room107.tool.AutowiredTest;

/**
 * @author WangXiao
 */
public class WxSubscribeDaoImplTest extends AutowiredTest {

    @Autowired
    private IWxSubscribeDao dao;

    @Rollback(value = true)
    @Test
    public void testDeleteByUsername() {
        dao.deleteByOpenId("test");
    }

    @Test
    public void testGetByPosition() {
        // System.out.println(dao.getByPosition(null, new
        // HashSet<String>(Arrays.asList("地铁10号线"))));
        // System.out.println(dao.getByPosition(new Location(1,2), null));
        // System.out.println(dao.getByPosition(new Location(1.00001,2.00002),
        // new HashSet<String>(Arrays.asList("地铁10号线"))));
        System.out.println(dao.getByPosition(new Location(116.32212430138,
                39.997982077069), null, IPositionSearcher.SEARCH_POSITION_RADIUS));
    }

}
