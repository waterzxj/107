package room107.service.user;

import java.util.List;

import room107.dao.IUserDao;
import room107.dao.UsernameExistException;
import room107.datamodel.User;
import room107.datamodel.WxUser;

/**
 * See {@link IUserDao}.
 * 
 * @author WangXiao
 */
public interface IUserService {

    User createUser(User user) throws UsernameExistException;

    User grantUser(User anonymousUser, User user) throws UsernameExistException;

    void updateUser(User user);
    
    User updateInfo(User user);

    void deleteWxUser(WxUser wxUser);

    /**
     * @param user
     *            non-null
     * @return null if login failed
     */
    User login(User user);

    User getUser(String username);

    User getEncryptUser(String key);

    WxUser getWxUser(String openId);

    WxUser getWxUserByUsername(String username);

    List<User> getAll();

    List<User> getUsersByKey(String searchKey);

    /**
     * @param userKey
     *            see {@link IUserDao#getUser(String)}
     * @return email address to receive reset email, or empty string when user
     *         is valid but has NO email, or null when invalid user
     */
    String sendResetEmail(String userKey) throws Exception;
    
    List<User> search(String searchKey);

}
