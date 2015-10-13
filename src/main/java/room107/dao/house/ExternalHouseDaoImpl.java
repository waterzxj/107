package room107.dao.house;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.impl.DaoImpl;
import room107.datamodel.AuditStatus;
import room107.datamodel.ExternalHouse;
import room107.datamodel.HouseSource;

/**
 * @author yanghao
 */
@Repository
@SuppressWarnings("unchecked")
public class ExternalHouseDaoImpl extends DaoImpl implements IExternalHouseDao {

    @Override
    public List<ExternalHouse> getUnauditedExternalHouse() {
        return getSession()
                .createCriteria(ExternalHouse.class)
                .add(Restrictions.eq("operationStatus",
                        AuditStatus.PROCESSING.ordinal()))
                .add(Restrictions.in("source",
                        new Integer[] { HouseSource.LOCAL58.ordinal(),
                                HouseSource.GANJI.ordinal() }))
                .addOrder(Order.desc("lastModifiedTime")).list();
    }

    @Override
    public ExternalHouse getExternalHouseByHouseId(long id) {
        return (ExternalHouse) getSession().createCriteria(ExternalHouse.class)
                .add(Restrictions.eq("houseId", id)).uniqueResult();
    }

    @Override
    public int getHouseCount(AuditStatus status) {
        return ((BigInteger) (getSession().createSQLQuery(
                "select count(*) from external_house where operationStatus = "
                        + status.ordinal()).uniqueResult())).intValue();
    }

    @Override
    public List<ExternalHouse> getUnauditedForumHouse() {
        return getSession()
                .createCriteria(ExternalHouse.class)
                .add(Restrictions.in("operationStatus",
                        new Integer[] { AuditStatus.PROCESSING.ordinal(),
                                AuditStatus.DELAY.ordinal() }))
                .add(Restrictions.in("source",
                        new Integer[] { HouseSource.DOUBAN.ordinal(),
                                HouseSource.SHUIMU.ordinal() }))
                .addOrder(Order.asc("operationStatus"))
                .addOrder(Order.desc("lastModifiedTime")).list();
    }

    @Override
    public List<ExternalHouse> getExternalHouseByEncryptKey(String key) {
        return getSession().createCriteria(ExternalHouse.class)
                .add(Restrictions.eq("encryptKey", key)).list();
    }

    @Override
    public List<ExternalHouse> getExternalHouseByHouseId(
            Collection<Long> houseIds) {
        return getSession().createCriteria(ExternalHouse.class)
                .add(Restrictions.in("houseId", houseIds)).list();
    }

    @Override
    public int getExternalHouseCountBySource(Date start, HouseSource source) {
        Criteria criteria = getSession().createCriteria(ExternalHouse.class);
        if (start != null) {
            criteria.add(Restrictions.ge("lastModifiedTime", start));
        }
        if (source != null) {
            criteria.add(Restrictions.eq("source", source.ordinal()));
        }
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();

    }

    @Override
    public int getExternalEnteringHouseCountBySource(Date start,
            HouseSource source) {
        Criteria criteria = getSession().createCriteria(ExternalHouse.class);
        if (start != null) {
            criteria.add(Restrictions.ge("operationTime", start));
        }
        criteria.add(Restrictions.isNotNull("houseId"));
        if (source != null) {
            criteria.add(Restrictions.eq("source", source.ordinal()));
        }
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();
    }

    @Override
    public int getExternalEffectiveHouseCountBySource(Date start,
            HouseSource source) {
        String sql = "SELECT count(*) FROM house,external_house WHERE house.`id`=external_house.`houseId` AND house.`status`='0' AND house.`auditStatus`='0'";
        if (start != null) {
            String time = new SimpleDateFormat("yyyy-MM-dd").format(start);
            sql += "AND external_house.operationTime >='" + time + "'";
        }
        if (source != null) {
            sql += " AND external_house.`source`='" + source.ordinal() + "'";
            System.out.println("the sql is "+sql);
        }
        return ((Number) getSession().createSQLQuery(sql).uniqueResult())
                .intValue();
    }

}
