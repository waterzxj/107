package room107.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import room107.datamodel.User;

/**
 * @author WangXiao
 */
public interface IUserDao extends IDao {

    User getUser(long userId);

    User getUser(String username);

    User getUserByName(String name);

    User getUserByEmail(String email);

    User getUserByVerifyEmail(String verifyEmail);

    User getUserByTelephone(String telephone);
    
    User getUserById(String id);
    
    User getUserByVerifyId(String id);
    
    User getUserByToken(String token);

    List<User> getUserByVerifyName(String verifyName);

    List<User> getUsers(Collection<String> usernames);

    int getAllCount(Date start);

    int getRegisterCount(Date start);

    int getAnonymousCount(Date start);

    int getAuthenticationCount(Date start);

    /**
     * @param userKey
     *            {@link User#getUsername()} or {@link User#getEmail() or
     *            User#getVerifyEmail()}
     */
    List<User> getUserByKey(String userKey);
    
    /**
     * search by username\name\email\verifyEmail\telephone\id\verifyName\token
     */
    List<User> search(String searchKey);

    /**
     * @param user
     *            non-null
     * @return newly created user
     * @throws UsernameExistException
     *             when user name has existed
     */
    User createUser(User user) throws UsernameExistException;

}
