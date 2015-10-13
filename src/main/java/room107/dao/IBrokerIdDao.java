package room107.dao;

import java.util.List;

import room107.datamodel.BrokerId;

/**
 * @author WangXiao
 */
public interface IBrokerIdDao extends IDao {

    BrokerId getBrokerId(int type, String value);

    List<BrokerId> getBrokerIds(String value);

}
