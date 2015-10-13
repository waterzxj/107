package room107.dao;

import java.util.Date;

/**
 * @author WangXiao
 */
public interface IBrokerIdReportDao extends IDao {

    /**
     * @return null when not reported
     */
    Date getReportTime(String cookieId, int type, String brokerId);

}
