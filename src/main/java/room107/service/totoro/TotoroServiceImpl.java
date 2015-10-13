package room107.service.totoro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.IBrokerIdDao;
import room107.dao.IBrokerIdReportDao;
import room107.dao.ISniffUrlResultDao;
import room107.datamodel.BrokerId;
import room107.datamodel.BrokerIdReport;
import room107.datamodel.BrokerIdType;
import room107.datamodel.SniffUrl;
import room107.util.UserBehaviorLog;

/**
 * @author WangXiao
 */
@CommonsLog
@Service
public class TotoroServiceImpl implements ITotoroService {

    @Autowired
    private ISniffUrlResultDao sniffUrlResultDao;
    @Autowired
    private IBrokerIdDao brokerIdDao;
    @Autowired
    private IBrokerIdReportDao brokerIdReportDao;

    @Override
    public boolean isAgency(String username) {
        return sniffUrlResultDao.getVisitedBlackUrlCount(username, 0.9999f,
                1.0001f) >= 3;
    }

    @Override
    public List<SniffUrl> getVisitedUrls(String username) {
        return sniffUrlResultDao.getVisitedUrls(username);
    }

    @Transactional
    @Override
    public BrokerId reportBrokerId(String cookieId, String ip, int type,
            String brokerId) {
        brokerId = StringUtils.trimToNull(brokerId);
        if (brokerId == null) {
            return null;
        }
        Date time = brokerIdReportDao.getReportTime(cookieId, type, brokerId);
        if (time != null) { // have reported
            log.warn("Report broker more than once: cookieId=" + cookieId
                    + ", ip=" + ip + ", brokerId=" + brokerId + ", type="
                    + type);
            return brokerIdDao.getBrokerId(type, brokerId);
        }
        UserBehaviorLog.LAB_BROKER.info("Report broker: cookieId=" + cookieId
                + ", ip=" + ip + ", brokerId=" + brokerId + ", type=" + type);
        // update broker ID
        BrokerId result = brokerIdDao.getBrokerId(type, brokerId);
        if (result == null) {
            result = new BrokerId(type, brokerId, 1, new Date());
            brokerIdDao.save(result);
        } else {
            result.setReportCount(result.getReportCount() + 1);
            result.setModifiedTime(new Date());
            brokerIdDao.update(result);
        }
        // update report history
        brokerIdReportDao.save(new BrokerIdReport(brokerId, type, cookieId, ip,
                new Date()));
        return result;
    }

    @Override
    public BrokerId reportBrokerId(String cookieId, String ip, String brokerId) {
        brokerId = StringUtils.trimToNull(brokerId);
        if (brokerId == null) {
            return null;
        }
        return reportBrokerId(cookieId, ip, getBrokerIdType(brokerId), brokerId);
    }

    @Override
    public List<BrokerSearchResult> searchBrokers(String cookieId, String ip,
            String brokerId) {
        brokerId = StringUtils.trimToNull(brokerId);
        if (brokerId == null) {
            return null;
        }
        UserBehaviorLog.LAB_BROKER.info("Search broker: cookieId=" + cookieId
                + ", ip=" + ip + ", brokerId=" + brokerId);
        List<BrokerId> brokerIds = brokerIdDao.getBrokerIds(brokerId);
        if (brokerIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<BrokerSearchResult> result = new ArrayList<BrokerSearchResult>();
        for (BrokerId id : brokerIds) {
            Date reportTime = brokerIdReportDao.getReportTime(cookieId,
                    getBrokerIdType(brokerId), brokerId);
            result.add(new BrokerSearchResult(id, reportTime));
        }
        return result;
    }

    private int getBrokerIdType(String brokerId) {
        int type = BrokerIdType.DOUBAN.ordinal();
        if (StringUtils.isNumeric(brokerId)) {
            if ((brokerId.startsWith("1") && brokerId.length() == 11)
                    || (brokerId.replace("-", "").length() == 8)) {
                type = BrokerIdType.TELEPHONE.ordinal();
            }
        }
        return type;
    }

}
