package room107.service.user;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import room107.datamodel.User;
import room107.datamodel.VerifyEmailRecord;
import room107.datamodel.VerifyEmailStatus;
import room107.datamodel.VerifyEmailType;

/**
 * @author WangXiao
 */
public class VerifyServiceImplMock implements IVerifyService {

    @Override
    public VerifyEmailStatus verifyEmail(ConfirmData data) {
        return VerifyEmailStatus.values()[RandomUtils.nextInt(VerifyEmailStatus
                .values().length)];
    }

    @Override
    public boolean confirmEmail(ConfirmData data) {
        return RandomUtils.nextBoolean();
    }

    @Override
    public void confirmCredential(String username, String verifyName, String verifyId) {
    }

    @Override
    public void setEmailType(String email, VerifyEmailType type) {
    }

    @Override
    public List<VerifyEmailRecord> getVerifyEmailRecords(
            VerifyEmailStatus status) {
        return Collections.emptyList();
    }

    @Override
    public List<User> getUsersByVerifyName(String verifyName) {
        return null;
    }

}
