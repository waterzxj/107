package room107.service.totoro;

import java.util.Date;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.ISniffUrlDao;
import room107.dao.ISniffUrlResultDao;
import room107.datamodel.SniffUrl;
import room107.datamodel.SniffUrlResult;
import room107.datamodel.SniffUrlResultId;

/**
 * @author WangXiao
 */
@CommonsLog
@Service
@Transactional
public class SniffHistoryServiceImpl implements ISniffHistoryService {

    @Autowired
    private ISniffUrlDao sniffUrlDao;

    @Autowired
    private ISniffUrlResultDao sniffUrlResultDao;

    @Override
    public List<SniffUrl> getSniffUrls(String cookie, String username,
            int maxCount, int confidentLevel) {
        if (log.isDebugEnabled()) {
            log.debug("Get sniff URLs: username=" + username + ", maxCount="
                    + maxCount + ", confidentCount=" + confidentLevel);
        }
        List<SniffUrl> result = sniffUrlDao.getSniffUrls(username, maxCount,
                confidentLevel);
        return result;
    }

    @Override
    public void saveHistory(String cookie, String username,
            SingleSniffResult result) {
        if (username == null || result == null) {
            if (log.isDebugEnabled()) {
                log.debug("Discard invalid sniff result: cookie=" + cookie
                        + ", result=" + result);
            }
            return;
        }
        // amend
        result.setUsername(username);
        if (log.isDebugEnabled()) {
            log.debug("Single sniff result: cookie=" + cookie + ", username="
                    + username + ", result=" + result);
        }
        /*
         * merge result
         */
        SniffUrlResultId id = new SniffUrlResultId(username, result.getUrlId());
        SniffUrlResult oldResult = sniffUrlResultDao.get(SniffUrlResult.class,
                id);
        int hit = 0, missed = 0, amendHit = 0;
        if (result.isAmend()) {
            amendHit = result.isVisited() ? 1 : 0;
        } else {
            hit = result.isVisited() ? 1 : 0;
            missed = 1 - hit;
        }
        boolean isNew = oldResult == null;
        int total = hit + missed + amendHit;
        if (isNew) {
            oldResult = new SniffUrlResult(id,
                    isVisited(hit, missed, amendHit), new Date(), hit, missed,
                    amendHit, total);
            sniffUrlResultDao.save(oldResult);
        } else {
            oldResult.setHitCount(oldResult.getHitCount() + hit);
            oldResult.setMissedCount(oldResult.getMissedCount() + missed);
            oldResult.setAmendHitCount(oldResult.getAmendHitCount() + amendHit);
            oldResult.setTotalCount(oldResult.getTotalCount() + total);
            oldResult.setVisited(isVisited(oldResult.getHitCount(),
                    oldResult.getMissedCount(), oldResult.getAmendHitCount()));
            oldResult.setModifiedTime(new Date());
            sniffUrlResultDao.update(oldResult);
        }
        if (log.isDebugEnabled()) {
            log.debug((isNew ? "Save: " : "Update: ") + oldResult);
        }
    }

    @Override
    public Boolean isVisited(int hitCount, int missedCount, int amendHitCount) {
        if (hitCount == 0 && hitCount == missedCount
                && hitCount == amendHitCount) {
            return null;
        }
        hitCount += amendHitCount;
        float rate = ((float) hitCount - missedCount)
                / (hitCount + missedCount);
        if (Math.abs(rate) >= 0.1) { // (h+a) >> or << m
            return rate > 0;
        } else { // check amend
            rate = ((float) amendHitCount - missedCount)
                    / (amendHitCount + missedCount);
            if (Math.abs(rate) <= 0.1) { // m ~ a
                return true;
            } else { // h ~ m
                return null;
            }
        }
    }

}
