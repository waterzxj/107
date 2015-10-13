package room107.service.totoro;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import room107.datamodel.BrokerId;
import room107.datamodel.SniffUrl;

/**
 * @author WangXiao
 */
public interface ITotoroService {

    boolean isAgency(String username);

    List<SniffUrl> getVisitedUrls(String username);

    /**
     * @return created or updated instance
     */
    BrokerId reportBrokerId(String cookieId, String ip, int type,
            String brokerId);

    BrokerId reportBrokerId(String cookieId, String ip, String brokerId);

    List<BrokerSearchResult> searchBrokers(String cookieId, String ip,
            String brokerId);

    @AllArgsConstructor
    static class BrokerSearchResult {

        @Getter
        private String brokerId;

        @Getter
        private int brokerIdType, reportCount;

        @Getter
        private Date modifiedTime, reportTime;

        public BrokerSearchResult(BrokerId id, Date reportTime) {
            this(id.getValue(), id.getType(), id.getReportCount(), id
                    .getModifiedTime(), reportTime);
        }

    }

}
