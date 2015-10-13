package room107.mock;

import room107.datamodel.User;
import room107.datamodel.VerifyStatus;

/**
 * @author WangXiao
 */
public class MockUser extends User {

    public MockUser() {
        setId(107l);
        setTelephone("15112345678");
        setVerifyStatus(VerifyStatus.VERIFIED_EMAIL.ordinal());
    }

}
