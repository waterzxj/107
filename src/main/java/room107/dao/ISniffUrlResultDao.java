package room107.dao;

import java.util.List;

import room107.datamodel.SniffUrl;

/**
 * @author WangXiao
 */
public interface ISniffUrlResultDao extends IDao {

    /**
     * Get the maximum URL ID in some user's sniff result.
     * 
     * @return null when NO sniff result for this user
     */
    Long getMaxSniffUrlId(String username);

    int getVisitedBlackUrlCount(String username, float fromWeight,
            float toWeight);

    List<SniffUrl> getVisitedUrls(String username);

}
