package room107.dao.impl;

import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import room107.dao.ISniffUrlResultDao;
import room107.datamodel.SniffUrlResult;
import room107.datamodel.SniffUrlResultId;
import room107.tool.AutowiredTest;

/**
 * @author WangXiao
 */
public class SniffUrlResultDaoImplTest extends AutowiredTest {

    @Autowired
    private ISniffUrlResultDao sniffUrlResultDao;

    @Test
    public void getMaxSniffUrlId() throws Exception {
        // System.out.println(sniffUrlResultDao.getMaxSniffUrlId("test"));
    }

    // @Test
    // @Rollback(false)
    // public void saveOrUpdate() throws Exception {
    // SniffUrlResult result = new SniffUrlResult();
    // result.setId(new SniffUrlResultId("test", 1));
    // result.setVisited(false);
    // sniffUrlResultDao.saveOrUpdate(result);
    // result.setVisited(true);
    // sniffUrlResultDao.saveOrUpdate(result);
    // }
    //
    // @Test
    // public void getVisitedUrls() throws Exception {
    // assertTrue(sniffUrlResultDao.getVisitedUrls("notexistusername")
    // .isEmpty());
    // System.out.println(sniffUrlResultDao.getVisitedUrls("test"));
    // }

}
