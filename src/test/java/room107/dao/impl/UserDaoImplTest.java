package room107.dao.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import room107.dao.IUserDao;
import room107.tool.AutowiredTest;

/**
 * @author WangXiao
 */
public class UserDaoImplTest extends AutowiredTest {

    @Autowired
    private IUserDao userDao;

    @Test
    public void getBalance() throws Exception {
        // System.out.println(userDao.getBalance("20130706001"));
        // System.out.println(userDao.getBalance("null"));
    }

}
