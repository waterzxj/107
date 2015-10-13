package room107.dao.house;

import java.util.List;

import room107.dao.IDao;
import room107.datamodel.HouseFavor;

/**
 * @author WangXiao
 */
public interface IHouseFavorDao extends IDao {

    /**
     * @return by IDs in {@link HouseFavor}
     */
    HouseFavor get(HouseFavor houseFavor);

    List<HouseFavor> getAll(String username);

}
