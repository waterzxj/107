package room107.dao;

import java.util.List;
import java.util.Set;

import room107.datamodel.Location;
import room107.datamodel.WxSubscribe;
import room107.service.api.weixin.BasicSubscriberInfo;

/**
 * @author WangXiao
 */
public interface IWxSubscribeDao extends IDao {

    /**
     * Time of subscribe expiration.
     */
    int SUBSCRIBE_EXPIRE_HOURS = 48;
    
    int SUBSCRIBE_NOTIFY_TIME = SUBSCRIBE_EXPIRE_HOURS - 10;

    void deleteByOpenId(String openId);

    List<WxSubscribe> getByOpenId(String openId);

    List<WxSubscribe> getByPosition(Location location, Set<String> features, int radius);
    
    List<BasicSubscriberInfo> getAllValidSubscribe();

}
