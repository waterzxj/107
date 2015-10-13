package room107.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.IUserDao;
import room107.dao.UsernameExistException;
import room107.datamodel.User;
import room107.datamodel.UserStatus;
import room107.datamodel.VerifyStatus;
import room107.util.Md5Utils;
import room107.util.UserUtils;

/**
 * @author yanghao
 */
@CommonsLog
@Repository
@SuppressWarnings("unchecked")
public class UserDaoImpl extends DaoImpl implements IUserDao {

    @Override
    public User getUser(long userId) {
        return get(User.class, userId);
    }

    @Override
    public List<User> getUserByKey(String userKey) {
        userKey = StringUtils.trimToNull(userKey);
        if (userKey == null) {
            return null;
        }
        Criterion c = Restrictions.or(
                Restrictions.eq("username", userKey).ignoreCase(),
                Restrictions.eq("email", userKey).ignoreCase(),
                Restrictions.eq("verifyEmail", userKey).ignoreCase());
        List<User> users = getSession().createCriteria(User.class).add(c)
                .list();
        return users;
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz) {
        return getSession().createCriteria(User.class)
                .addOrder(Order.desc("createdTime")).setMaxResults(/*
                                                                    * too many
                                                                    * will make
                                                                    * admin page
                                                                    * crached
                                                                    */500)
                .list();
    }

    @Override
    public User createUser(User user) throws UsernameExistException {
        Validate.notNull(user);
        if (log.isDebugEnabled()) {
            log.debug("Before create: " + user);
        }
        // check name
        User exist = getUser(user.getUsername());
        if (exist != null) {
            throw new UsernameExistException(user.getUsername());
        }
        // init
        Date date = new Date();
        user.setCreatedTime(date);
        user.setModifiedTime(date);
        // encrypt password
        user.setPassword(UserUtils.encryptPassword(user.getPassword()));
        // token
        user.setToken(Md5Utils.number2String(Md5Utils.sign32bit(user
                .getUsername().getBytes())));
        // save
        user = getUser((Long) getSession().save(user));
        if (log.isDebugEnabled()) {
            log.debug("After create: " + user);
        }
        return user;
    }

    @Override
    public User getUser(String username) {
        username = StringUtils.trimToNull(username);
        if (username == null) {
            return null;
        }
        List<User> users = (List<User>) getSession().createCriteria(User.class)
                .add(Restrictions.eq("username", username).ignoreCase()).list();
        if (users.isEmpty()) {
            return null;
        }
        if (users.size() > 1) {
            log.warn("Duplicated usernames: " + username);
        }
        return users.get(0);
    }

    @Override
    public List<User> getUsers(Collection<String> usernames) {
        if (usernames.isEmpty()) {
            return new ArrayList<User>();
        }

        List<User> users = (List<User>) getSession().createCriteria(User.class)
                .add(Restrictions.in("username", usernames)).list();
        return users;
    }

    @Override
    public List<User> getUserByVerifyName(String verifyName) {
        List<User> users = (List<User>) getSession().createCriteria(User.class)
                .add(Restrictions.eq("verifyName", verifyName)).list();
        return users;
    }

    @Override
    public int getAllCount(Date start) {
        Criteria criteria = getSession().createCriteria(User.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();
    }

    @Override
    public int getRegisterCount(Date start) {
        Criteria criteria = getSession().createCriteria(User.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        criteria.add(Restrictions.in("status", new Integer[]{
                UserStatus.NORMAL.ordinal(), UserStatus.DOUBAN.ordinal(),
                UserStatus.QQ.ordinal(), UserStatus.WEIBO.ordinal()}));
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();
    }

    @Override
    public int getAnonymousCount(Date start) {
        Criteria criteria = getSession().createCriteria(User.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        criteria.add(Restrictions.eq("status", UserStatus.ANONYMOUS.ordinal()));
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();
    }

    @Override
    public int getAuthenticationCount(Date start) {
        Criteria criteria = getSession().createCriteria(User.class);
        if (start != null) {
            criteria.add(Restrictions.ge("createdTime", start));
        }
        criteria.add(Restrictions.gt("verifyStatus",
                VerifyStatus.UNVERIFIED.ordinal()));
        return ((Number) criteria.setProjection(Projections.count("id"))
                .uniqueResult()).intValue();
    }

    @Override

    public List<User> search(String searchKey) {
        if (StringUtils.isEmpty(searchKey)) {
            return new ArrayList<User>();
        }
        Criterion c = Restrictions.or(
                Restrictions.eq("username", searchKey).ignoreCase(),
                Restrictions.eq("name", searchKey).ignoreCase(),
                Restrictions.eq("email", searchKey).ignoreCase(),
                Restrictions.eq("verifyEmail", searchKey).ignoreCase(),
                Restrictions.eq("telephone", searchKey).ignoreCase(),
                Restrictions.eq("id", NumberUtils.toLong(searchKey, -1)),
                Restrictions.eq("verifyName", searchKey).ignoreCase(),
                Restrictions.eq("token", searchKey).ignoreCase());
        List<User> users = getSession().createCriteria(User.class).add(c)
                .list();
        return users;
    }
    
    @Override
    public User getUserByEmail(String email) {
        String str = StringUtils.trimToNull(email);
        List<User> users = new ArrayList<User>();
        if (str != null) {
            users = getSession().createCriteria(User.class)
                    .add(Restrictions.eq("email", str)).list();
        }
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);

    }

    @Override
    public User getUserByVerifyEmail(String email) {
        String str = StringUtils.trimToNull(email);
        List<User> users = new ArrayList<User>();
        if (str != null) {
            users = getSession().createCriteria(User.class)
                    .add(Restrictions.eq("verifyEmail", str)).list();

        }
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);

    }

    @Override
    public User getUserByTelephone(String telephone) {
        String str = StringUtils.trimToNull(telephone);
        List<User> users = new ArrayList<User>();
        if (str != null) {
            users = getSession().createCriteria(User.class)
                    .add(Restrictions.eq("telephone", str)).list();
        }
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public User getUserById(String id) {
        List<User> users = new ArrayList<User>();
        try {
            long uid = Long.valueOf(id);
            users = getSession().createCriteria(User.class)
                    .add(Restrictions.eq("id", uid)).list();
        } catch (Exception e) {
            return null;
        }
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public User getUserByVerifyId(String verifyId) {

        String str = StringUtils.trimToNull(verifyId);
        List<User> users = new ArrayList<User>();
        if (str != null) {
            users = getSession().createCriteria(User.class)
                    .add(Restrictions.eq("verifyId", str)).list();
        }
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public User getUserByToken(String token) {
        String str = StringUtils.trimToNull(token);
        List<User> users = new ArrayList<User>();
        if (str != null) {
            users = getSession().createCriteria(User.class)
                    .add(Restrictions.eq("token", str)).list();
        }
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public User getUserByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }
}
