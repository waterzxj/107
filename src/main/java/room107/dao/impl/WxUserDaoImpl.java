package room107.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.IWxUserDao;
import room107.datamodel.User;
import room107.datamodel.WxUser;

/**
 * @author WangXiao
 */
@Repository
public class WxUserDaoImpl extends DaoImpl implements IWxUserDao {

    @Override
    public <T> int getCount(Class<T> clazz, Date start) {
        Criteria criteria = getSession().createCriteria(clazz);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        return ((Number) criteria.setProjection(Projections.count("openId"))
                .uniqueResult()).intValue();
    }

    @Override
    public WxUser getByUsername(String username) {
        if (username == null) {
            return null;
        }
        return (WxUser) getSession().createCriteria(WxUser.class)
                .add(Restrictions.eq("username", username).ignoreCase())
                .uniqueResult();
    }

    @Override
    public boolean exists(String openId) {
        if (openId == null) {
            return false;
        }
        Number count = (Number) getSession().createCriteria(WxUser.class)
                .add(Restrictions.eq("openId", openId))
                .setProjection(Projections.rowCount()).uniqueResult();
        return count.intValue() > 0;
    }

    @Override
    public List<String> getOpenIds(Date actionTimeFrom, Date actionTimeTo) {
        List<WxUser> users = getWxUsers(actionTimeFrom, actionTimeTo);
        List<String> openIds = new ArrayList<String>();
        for (WxUser user : users) {
            openIds.add(user.getOpenId());
        }
        return openIds;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<WxUser> getWxUsers(Date actionTimeFrom, Date actionTimeTo) {
        Session session = sessionFactory.openSession();
        try {
            Criteria criteria = session.createCriteria(WxUser.class);
            if (actionTimeFrom != null) {
                criteria.add(Restrictions.ge("actionTime", actionTimeFrom));
            }
            if (actionTimeTo != null) {
                criteria.add(Restrictions.lt("actionTime", actionTimeTo));
            }
            return criteria.list();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateSessionStatus(String openId, int sessionStatus) {
        Validate.notNull(openId);
        getSession()
                .createQuery(
                        "update WxUser set sessionStatus=:s where openId=:openId")
                .setParameter("s", sessionStatus)
                .setParameter("openId", openId).executeUpdate();
    }

    @Override
    public int getWxCount(Date start) {
        Criteria criteria = getSession().createCriteria(WxUser.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        return ((Number) criteria.setProjection(Projections.count("openId"))
                .uniqueResult()).intValue();
    }

    @Override
    public int getWxOnlyCount(Date start) {
        Criteria criteria = getSession().createCriteria(WxUser.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        criteria.add(Restrictions.isNull("username"));
        return ((Number) criteria.setProjection(Projections.count("openId"))
                .uniqueResult()).intValue();
    }
}
