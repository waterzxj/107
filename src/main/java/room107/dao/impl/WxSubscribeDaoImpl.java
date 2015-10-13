package room107.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import room107.dao.IWxSubscribeDao;
import room107.dao.IWxUserDao;
import room107.datamodel.Location;
import room107.datamodel.User;
import room107.datamodel.WxSubscribe;
import room107.datamodel.WxUser;
import room107.service.api.weixin.BasicSubscriberInfo;
import room107.util.GeographyUtils;
import room107.util.StringUtils;

/**
 * @author WangXiao
 */
@Repository
public class WxSubscribeDaoImpl extends DaoImpl implements IWxSubscribeDao {

    @Autowired
    private IWxUserDao wxUserDao;

    @Override
    public void deleteByOpenId(String openId) {
        if (openId != null) {
            String hql = "delete from WxSubscribe where openId= :openId";
            getSession().createQuery(hql).setString("openId", openId)
                    .executeUpdate();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<WxSubscribe> getByOpenId(String openId) {
        return getSession().createCriteria(WxSubscribe.class)
                .add(Restrictions.eq("openId", openId)).list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<WxSubscribe> getByPosition(Location location,
            Set<String> features, int radius) {
        Validate.isTrue(location != null
                || CollectionUtils.isNotEmpty(features));
        /*
         * non-expired users
         */
        List<String> openIds = wxUserDao.getOpenIds(
                DateUtils.addHours(new Date(), -SUBSCRIBE_EXPIRE_HOURS), null);
        if (openIds.isEmpty()) {
            return Collections.emptyList();
        }
        /*
         * subscribes
         */
        List<WxSubscribe> subscribes = getSession()
                .createCriteria(WxSubscribe.class)
                .add(Restrictions.in("openId", openIds)).list();
        List<WxSubscribe> result = new ArrayList<WxSubscribe>();
        for (WxSubscribe subscribe : subscribes) {
            String feature = subscribe.getFeature();
            if (feature != null && CollectionUtils.isNotEmpty(features)
                    && features.contains(feature)) {
                // hit feature
                result.add(subscribe);
            } else if (subscribe.getLocationX() != null
                    && subscribe.getLocationY() != null && location != null) {
                double distance = GeographyUtils.getDistance(
                        subscribe.getLocationX(), subscribe.getLocationY(),
                        location.getX(), location.getY());
                if (distance <= radius) {
                    // hit location
                    result.add(subscribe);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BasicSubscriberInfo> getAllValidSubscribe() {
        List<BasicSubscriberInfo> subscribers = new ArrayList<BasicSubscriberInfo>();
        
        List<String> openIds = wxUserDao.getOpenIds(
                DateUtils.addHours(new Date(), -SUBSCRIBE_EXPIRE_HOURS), null);
        if (openIds.isEmpty()) {
            return subscribers;
        }
        Session session = sessionFactory.openSession();
        try {
            List<WxSubscribe> wxSubscribers = session
                    .createCriteria(WxSubscribe.class)
                    .add(Restrictions.in("openId", openIds)).list();
            Map<String, BasicSubscriberInfo> openId2BasicSubscriberInfo = new HashMap<String, BasicSubscriberInfo>();
            for (WxSubscribe wxSubscribe : wxSubscribers) {
                String openId = wxSubscribe.getOpenId();
                if (StringUtils.isNotEmpty(openId) && !openId2BasicSubscriberInfo.containsKey(openId)
                        && wxSubscribe.getLocationX() != null && wxSubscribe.getLocationY() != null) {
                    BasicSubscriberInfo subscriber = new BasicSubscriberInfo();
                    subscriber.openId = openId;
                    subscriber.x = wxSubscribe.getLocationX();
                    subscriber.y = wxSubscribe.getLocationY();
                    openId2BasicSubscriberInfo.put(openId, subscriber);
                }
            }
            List<WxUser> wxUsers = session.createCriteria(WxUser.class)
                    .add(Restrictions.in("openId", openId2BasicSubscriberInfo.keySet())).list();
            Map<String, BasicSubscriberInfo> username2BasicSubscriberInfo = new HashMap<String, BasicSubscriberInfo>();
            for (WxUser wxUser : wxUsers) {
                if (wxUser != null && StringUtils.isNotEmpty(wxUser.getOpenId())
                        && StringUtils.isNotEmpty(wxUser.getUsername())) {
                    BasicSubscriberInfo subscriber = openId2BasicSubscriberInfo.get(wxUser
                            .getOpenId());
                    if (subscriber != null) {
                        subscriber.username = wxUser.getUsername();
                        username2BasicSubscriberInfo.put(wxUser.getUsername(), subscriber);
                    }
                }
            }
            List<User> users = session.createCriteria(User.class)
                    .add(Restrictions.in("username", username2BasicSubscriberInfo.keySet())).list();
            for (User user : users) {
                if (user != null && StringUtils.isNotEmpty(user.getUsername())) {
                    BasicSubscriberInfo subscriber = username2BasicSubscriberInfo.get(user.getUsername());
                    if (subscriber != null) {
                        subscriber.faviconUrl = user.getFaviconUrl();
                        subscriber.nickname = user.getName();
                        subscribers.add(subscriber);
                    }
                }
            }
            return subscribers;
        } finally {
            session.close();
        }
    }

}
