package room107.service.totoro;

import java.util.Arrays;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;
import room107.datamodel.SniffUrl;

/**
 * @author WangXiao
 */
@CommonsLog
public class SniffHistoryServiceImplMock implements ISniffHistoryService {

    @Override
    public void saveHistory(String cookie, String username,
            SingleSniffResult result) {
        result.setUsername(username);
        if (log.isDebugEnabled()) {
            log.debug("Record sniff history: cookie=" + cookie + ", username="
                    + username + ", result=" + result);
        }
    }

    @Override
    public List<SniffUrl> getSniffUrls(String cookie, String username,
            int maxCount, int confidentCount) {
        return Arrays.asList(mockUrl(1, "www.baidu.com"),
                mockUrl(2, "www.taobao.com"), mockUrl(3, "www.xxxxx.com"));
    }

    @Override
    public Boolean isVisited(int hitCount, int missedCount, int amendHitCount) {
        return hitCount > missedCount;
    }

    private SniffUrl mockUrl(long urlId, String url) {
        SniffUrl result = new SniffUrl();
        result.setUrlId(urlId);
        result.setUrl(url);
        return result;
    }

}
