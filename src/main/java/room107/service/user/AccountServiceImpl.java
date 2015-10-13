package room107.service.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author WangXiao
 */
@Service
@Transactional
public class AccountServiceImpl implements IAccountService {

    @Override
    public void decreaseHouseBalance(String username, long houseId) {
        // TODO
    }

}
