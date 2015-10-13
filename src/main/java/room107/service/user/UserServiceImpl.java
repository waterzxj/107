package room107.service.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.IUserDao;
import room107.dao.IWxUserDao;
import room107.dao.UsernameExistException;
import room107.dao.house.IHouseDao;
import room107.datamodel.House;
import room107.datamodel.Room;
import room107.datamodel.User;
import room107.datamodel.UserStatus;
import room107.datamodel.UserType;
import room107.datamodel.WxUser;
import room107.util.EmailUtils;
import room107.util.EncryptUtils;
import room107.util.JsonUtils;
import room107.util.UserBehaviorLog;
import room107.util.UserUtils;
import room107.util.ValidateUtils;
import room107.web.data.url.ResetPassword;

/**
 * @author WangXiao
 */
@CommonsLog
@Service
@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IWxUserDao wxUserDao;

    @Autowired
    private IHouseDao houseDao;

    @Override
    public User createUser(User user) throws UsernameExistException {
        if (user == null) {
            return null;
        }
        return userDao.createUser(user);
    }

    @Override
    public User grantUser(User anonymousUser, User user)
            throws UsernameExistException {
        if (anonymousUser == null || user == null)
            return null;
        List<House> houses = houseDao.getHouses(anonymousUser.getUsername());
        Date now = new Date();
        for (House house : houses) {
            List<Room> rooms = houseDao.getRooms(house.getId());
            for (Room room : rooms) {
                room.setUsername(user.getUsername());
                room.setModifiedTime(now);
                houseDao.update(room);
            }
            house.setUsername(user.getUsername());
            house.setUserStatus(UserStatus.NORMAL.ordinal());
            house.setModifiedTime(now);
            houseDao.update(house);
            log.info("grant a house, id = " + house.getId() + ", username = "
                    + house.getUsername());
        }
        user.setType(anonymousUser.getType() | UserType.TENANT.ordinal());
        user.setVerifyStatus(anonymousUser.getVerifyStatus());
        user = userDao.createUser(user);
        log.info("create a user, username = " + user.getUsername());

        anonymousUser.setStatus(UserStatus.DISABLED.ordinal());
        userDao.update(anonymousUser);
        log.info("disable a user, username = " + anonymousUser.getUsername());

        return user;
    }   

    @Override
    public void updateUser(User user) {
        if (user == null) {
            return;
        }
        userDao.update(user);
    }

    @Override
    public User updateInfo(User info) {
        if (info == null || info.getUsername() == null)
            return null;
        User user = userDao.getUser(info.getUsername());
        if (user != null) {
            user.setAge(info.getAge());
            user.setCompany(info.getCompany());
            user.setConstellation(info.getConstellation());
            user.setDescription(info.getDescription());
            user.setGender(info.getGender());
            user.setMajor(info.getMajor());
            user.setName(info.getName());
            user.setProfession(info.getProfession());
            user.setSchool(info.getSchool());
            userDao.update(user);
        }
        return user;
    }

    @Override
    public void deleteWxUser(WxUser wxUser) {
        if (wxUser == null) {
            return;
        }
        wxUserDao.delete(wxUser);
    }

    @Override
    public User login(User user) {
        Validate.notNull(user);
        User u = userDao.getUser(user.getUsername());
        if (u != null) {
            // only normal user can login
            if (u.getStatus() != UserStatus.NORMAL.ordinal())
                return null;
            String password = UserUtils.encryptPassword(user.getPassword());
            if (log.isDebugEnabled()) {
                log.debug("Password: expect=" + u.getPassword() + ", actual="
                        + password);
            }
            return password.equals(u.getPassword()) ? u : null;
        }
        return null;
    }

    @Override
    public User getUser(String username) {
        return StringUtils.isEmpty(username) ? null : userDao.getUser(username);
    }

    @Override
    public WxUser getWxUserByUsername(String username) {
        return StringUtils.isEmpty(username) ? null : wxUserDao
                .getByUsername(username);
    }

    @Override
    public WxUser getWxUser(String openId) {
        return StringUtils.isEmpty(openId) ? null : wxUserDao.get(WxUser.class,
                openId);
    }

    @Override
    public List<User> getAll() {
        return userDao.getAll(User.class);
    }

    @Override
    public String sendResetEmail(String userKey) throws Exception {
        userKey = StringUtils.trimToNull(userKey);
        if (userKey == null) {
            return null;
        }
        List<User> users = userDao.getUserByKey(userKey);
        if (CollectionUtils.isNotEmpty(users)) {
            User user = users.get(0);
            /*
             * prepare email
             */
            String email = userKey;
            if (!ValidateUtils.validateEmail(userKey)) {
                if (!StringUtils.isEmpty(user.getEmail())) {
                    email = user.getEmail();
                } else if (!StringUtils.isEmpty(user.getVerifyEmail())) {
                    email = user.getVerifyEmail();
                } else { // no email set
                    return "";
                }
            }
            /*
             * send
             */
            String data = JsonUtils.encrypt(new ResetPassword(user
                    .getUsername(), System.currentTimeMillis()));
            UserBehaviorLog.AUTH.info("Send reset email: userKey=" + userKey
                    + ", to=" + email);
            VelocityContext context = new VelocityContext();
            String title = "107间邮箱认证";
            context.put("title", title);
            context.put("user", user);
            context.put("data", data);
            EmailUtils.sendMailByTemplate(email, title, "reset-password.vm",
                    context, HtmlEmail.class, EmailUtils.EmailAccount.NO_REPLY);
            return email;
        }
        return null;
    }

    @Override
    public User getEncryptUser(String key) {
        if (key != null) {
            try {
                String username = EncryptUtils.decryptAnomynous(key);
                return getUser(username);
            } catch (Exception e) {
            }
        }
        return null;
    }

    @Override
    public List<User> search(String searchKey) {
        if (StringUtils.isEmpty(searchKey)) return new ArrayList<User>();
        return userDao.search(searchKey);
    }

    @Override
    public List<User> getUsersByKey(String searchKey) {
        return null;
    }
}
