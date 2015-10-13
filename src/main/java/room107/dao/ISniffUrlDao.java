package room107.dao;

import java.util.ArrayList;
import java.util.List;

import room107.datamodel.SniffUrl;
import room107.datamodel.SniffUrlResult;

/**
 * @author WangXiao
 */
public interface ISniffUrlDao extends IDao {

    List<SniffUrl> ALL_CONFIDENT = new ArrayList<SniffUrl>(0);

    /**
     * @param username
     *            non-null
     * @param maxCount
     *            >0
     * @param confidentLevel
     *            when {@link SniffUrlResult#getTotalCount()} >= this value,
     *            it's regard as confident
     * @return non-null, if {@link #ALL_CONFIDENT} means all URLs with the
     *         specified confident level have been sniffed
     */
    List<SniffUrl> getSniffUrls(String username, int maxCount,
            int confidentLevel);

}
