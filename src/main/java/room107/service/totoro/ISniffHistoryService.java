package room107.service.totoro;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import room107.dao.ISniffUrlDao;
import room107.datamodel.SniffUrl;

/**
 * @author WangXiao
 */
public interface ISniffHistoryService {

    /**
     * The simple sniff result from web layer.
     * 
     * @author WangXiao
     */
    @ToString
    public static class SingleSniffResult {

        @Getter
        @Setter
        private String username;

        @Getter
        @Setter
        private long urlId;

        @Getter
        @Setter
        private int type;

        @Getter
        @Setter
        private boolean visited;

        public static final int TYPE_AMEND_URL = -1;

        public boolean isAmend() {
            return type == TYPE_AMEND_URL;
        }

    }

    /**
     * See {@link ISniffUrlDao#getSniffUrls(String, int, int)}
     * 
     * @param cookie
     *            non-null
     */
    List<SniffUrl> getSniffUrls(String cookie, String username, int maxCount,
            int confidentLevel);

    /**
     * @param cookie
     *            non-null
     * @param username
     *            null-able
     */
    void saveHistory(String cookie, String username, SingleSniffResult result);

    /**
     * @param hitCount
     *            >=0
     * @param missedCount
     *            >=0
     * @param amendHitCount
     *            >=0
     * @return whether a URL is visited by its hit and missed count, or null if
     *         cann't decide
     */
    Boolean isVisited(int hitCount, int missedCount, int amendHitCount);

}
