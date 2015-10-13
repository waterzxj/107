package room107.dao;

import room107.datamodel.VerifyEmail;

/**
 * @author WangXiao
 */
public interface IVerifyEmailDao extends IDao {

    enum EmailType {
        WHITE, BLACK
    }

    VerifyEmail getVerifyEmail(String domain);

    EmailType getEmailType(String domain);

}
