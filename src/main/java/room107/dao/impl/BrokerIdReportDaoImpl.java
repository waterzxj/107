package room107.dao.impl;

import java.util.Date;

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.IBrokerIdReportDao;
import room107.datamodel.BrokerIdReport;

/**
 * @author WangXiao
 */
@Repository
public class BrokerIdReportDaoImpl extends DaoImpl implements
        IBrokerIdReportDao {

    @Override
    public Date getReportTime(String cookieId, int type, String brokerId) {
        if (cookieId == null || brokerId == null) {
            return null;
        }
        return (Date) getSession().createCriteria(BrokerIdReport.class)
                .add(Restrictions.eq("cookieId", cookieId))
                .add(Restrictions.eq("brokerIdType", type))
                .add(Restrictions.eq("brokerId", brokerId).ignoreCase())
                .setProjection(Projections.property("modifiedTime"))
                .uniqueResult();

    }

}
