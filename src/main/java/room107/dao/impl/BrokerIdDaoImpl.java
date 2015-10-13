package room107.dao.impl;

import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.IBrokerIdDao;
import room107.datamodel.BrokerId;

/**
 * @author WangXiao
 */
@Repository
@SuppressWarnings("unchecked")
public class BrokerIdDaoImpl extends DaoImpl implements IBrokerIdDao {

    @Override
    public List<BrokerId> getBrokerIds(String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        return getSession().createCriteria(BrokerId.class)
                .add(Restrictions.eq("value", value).ignoreCase())
                .addOrder(Order.asc("type")).list();
    }

    @Override
    public BrokerId getBrokerId(int type, String value) {
        if (value == null) {
            return null;
        }
        return (BrokerId) getSession().createCriteria(BrokerId.class)
                .add(Restrictions.eq("type", type))
                .add(Restrictions.eq("value", value).ignoreCase())
                .uniqueResult();
    }

}
