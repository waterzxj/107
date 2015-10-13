package room107.service.user;

import lombok.extern.apachecommons.CommonsLog;

/**
 * @author WangXiao
 */
@CommonsLog
public class AccountServiceImplMock implements IAccountService {

    @Override
    public void decreaseHouseBalance(String username, long houseId) {
        log.info("decreaseApplyForHouseAccount: username=" + username
                + ", houseId=" + houseId);
    }

}
