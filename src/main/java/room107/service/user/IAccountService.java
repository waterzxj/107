package room107.service.user;

/**
 * @author WangXiao
 */
public interface IAccountService {

    int DEFAULT_HOUSE_ACCOUNT = 17;

    public void decreaseHouseBalance(String username, long houseId);

}
