package room107.dao.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import room107.dao.IWxUserDao;
import room107.tool.AutowiredTest;

public class WxUserDaoImplTest extends AutowiredTest {

    @Autowired
    private IWxUserDao wxUserDao;

    @Test
    @Rollback(value = false)
    public void testUpdateSessionStatus() {
        wxUserDao.updateSessionStatus("ozt7Rjrx2vDRrX0SuXAJVkVZTCAM", 1);
    }

}
