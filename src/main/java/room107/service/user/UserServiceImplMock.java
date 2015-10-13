package room107.service.user;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import room107.dao.UsernameExistException;
import room107.datamodel.User;
import room107.datamodel.WxUser;
import room107.datamodel.web.UserInfo;
import room107.mock.MockUser;
import room107.service.api.weixin.SessionStatus;

/**
 * @author WangXiao
 */
public class UserServiceImplMock implements IUserService {

    @Override
    public void deleteWxUser(WxUser wxUser) {
    }

    @Override
    public User createUser(User user) {
        return getUser(user.getUsername());
    }

    @Override
    public void updateUser(User user) {
    }
    
    public void updateUserInfo(UserInfo userinfo) {
    }

    @Override
    public User login(User user) {
        return getUser(user.getUsername());
    }

    @Override
    public String sendResetEmail(String userKey) {
        String[] s = { "test@107room.com", null, "" };
        return s[RandomUtils.nextInt(s.length + 1)];
    }

    @Override
    public List<User> getAll() {
        return Collections.emptyList();
    }

    @Override
    public User getUser(String username) {
        User user = new MockUser();
        user.setUsername(username);
        return RandomUtils.nextBoolean() ? user : null;
    }

    @Override
    public WxUser getWxUserByUsername(String username) {
        return new WxUser("testOpenId", username, new Date(), new Date(), null,
                null, SessionStatus.NORMAL.ordinal());
    }

    @Override
    public WxUser getWxUser(String openId) {
        return new WxUser(openId, "test", new Date(), new Date(), null, null,
                SessionStatus.NORMAL.ordinal());
    }

    @Override
    public User grantUser(User anonymousUser, User user)
            throws UsernameExistException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User getEncryptUser(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<User> getUsersByKey(String searchKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User updateInfo(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<User> search(String searchKey) {
        // TODO Auto-generated method stub
        return null;
    }

}
