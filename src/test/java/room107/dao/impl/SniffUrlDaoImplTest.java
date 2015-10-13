package room107.dao.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import room107.dao.ISniffUrlDao;
import room107.datamodel.SniffUrl;
import room107.tool.AutowiredTest;

/**
 * @author WangXiao
 */
public class SniffUrlDaoImplTest extends AutowiredTest {

    @Autowired
    private ISniffUrlDao sniffUrlDao;

    @Test
    public void getSniffUrls() {
        // for (SniffUrl url : sniffUrlDao.getSniffUrls("test", 1020, 0)) {
        // System.out.println(url);
        // }
    }

}
