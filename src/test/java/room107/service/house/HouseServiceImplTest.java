package room107.service.house;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import room107.dao.IDao;
import room107.datamodel.HouseFavor;
import room107.tool.AutowiredTest;

/**
 * @author WangXiao
 */
public class HouseServiceImplTest extends AutowiredTest {

    @Autowired
    private IHouseService houseService;

    @Test
    public void testGetHouseAndUsers() {
        // System.out.println(houseService.getHouseAndUsers());
    }

    @Test
    @Rollback(value = false)
    public void testUpdateFavor() {
        HouseFavor houseFavor = new HouseFavor("test", 0, 1, 123,
                IDao.STATUS_OK, new Date(), new Date());
        houseService.updateFavor(houseFavor);
        houseFavor = new HouseFavor("test", 0, 1, 12, IDao.STATUS_DELETED,
                new Date(), new Date());
        houseService.updateFavor(houseFavor);
        houseFavor = new HouseFavor("test", 0, 1, 123, IDao.STATUS_DELETED,
                new Date(), new Date());
        houseService.updateFavor(houseFavor);
    }

}
