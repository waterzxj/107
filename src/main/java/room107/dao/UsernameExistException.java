package room107.dao;

/**
 * @author WangXiao
 */
@SuppressWarnings("serial")
public class UsernameExistException extends Exception {

    public UsernameExistException(String username) {
        super(username);
    }

}
