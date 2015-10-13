package room107.dao;

import java.util.Date;
import java.util.List;

import room107.datamodel.WxUser;

/**
 * @author WangXiao
 */
public interface IWxUserDao extends IDao {

    WxUser getByUsername(String username);

    boolean exists(String openId);

    /**
     * @param actionTimeFrom
     *            inclusive, null when no limit
     * @param actionTimeTo
     *            not inclusive, null when no limit
     */
    List<String> getOpenIds(Date actionTimeFrom, Date actionTimeTo);
    
    /**
     * @param actionTimeFrom
     *            inclusive, null when no limit
     * @param actionTimeTo
     *            not inclusive, null when no limit
     */
    List<WxUser> getWxUsers(Date actionTimeFrom, Date actionTimeTo);

    void updateSessionStatus(String openId, int sessionStatus);
    
    int getWxCount(Date start);
    
    int getWxOnlyCount(Date start);

}
