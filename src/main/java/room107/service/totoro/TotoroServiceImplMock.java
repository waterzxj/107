package room107.service.totoro;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import room107.datamodel.BrokerId;
import room107.datamodel.BrokerIdType;
import room107.datamodel.SniffUrl;

/**
 * @author WangXiao
 */
public class TotoroServiceImplMock implements ITotoroService {

    @Override
    public boolean isAgency(String username) {
        return false;
    }

    @Override
    public List<SniffUrl> getVisitedUrls(String username) {
        return Collections.emptyList();
    }

    @Override
    public BrokerId reportBrokerId(String cookieId, String ip, int type,
            String brokerId) {
        return reportBrokerId(cookieId, ip, brokerId);
    }

    @Override
    public BrokerId reportBrokerId(String cookieId, String ip, String brokerId) {
        return mockBrokerId();
    }

    @Override
    public List<BrokerSearchResult> searchBrokers(String cookieId, String ip,
            String brokerId) {
        return Arrays
                .asList(mockBrokerSearchResult(), mockBrokerSearchResult());
    }

    private BrokerId mockBrokerId() {
        return new BrokerId(RandomUtils.nextInt(BrokerIdType.values().length),
                Long.toString(System.currentTimeMillis()),
                RandomUtils.nextInt(10), new Date());
    }

    private BrokerSearchResult mockBrokerSearchResult() {
        return new BrokerSearchResult(mockBrokerId(),
                RandomUtils.nextBoolean() ? new Date() : null);
    }

}
