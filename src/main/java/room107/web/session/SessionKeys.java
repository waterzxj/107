package room107.web.session;

import room107.datamodel.User;

/**
 * @author WangXiao
 */
public interface SessionKeys {

    String USER = User.class.getSimpleName();

    String ADMIN = "ADMIN";

    String BROWSER = "BROWSER";

}
