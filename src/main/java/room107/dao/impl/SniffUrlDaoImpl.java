package room107.dao.impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.BooleanType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;

import room107.dao.ISniffUrlDao;
import room107.datamodel.SniffUrl;

/**
 * @author WangXiao
 */
@Repository
@SuppressWarnings("unchecked")
public class SniffUrlDaoImpl extends DaoImpl implements ISniffUrlDao {

    @Override
    public List<SniffUrl> getSniffUrls(String username, int maxCount,
            int confidentLevel) {
        Validate.notNull(username);
        if (maxCount <= 0) {
            return Collections.emptyList();
        }
        String sql = "SELECT urlId, url, type, (totalCount<"
                + confidentLevel
                + " OR totalCount IS NULL) AS unconfident  FROM (SELECT u.urlId, u.url, u.TYPE, u.weight, r.username, r.totalCount FROM sniff_url u LEFT JOIN sniff_url_result r ON r.username='"
                + username
                + "' AND u.urlId=r.urlId) AS t ORDER BY unconfident DESC, weight DESC, TYPE LIMIT "
                + maxCount;
        SQLQuery query = (SQLQuery) getSession().createSQLQuery(sql)
                .setResultTransformer(Transformers.aliasToBean(SniffUrl.class));
        query.addScalar("urlId", LongType.INSTANCE);
        query.addScalar("url", StringType.INSTANCE);
        query.addScalar("type", IntegerType.INSTANCE);
        query.addScalar("unconfident", BooleanType.INSTANCE);
        List<SniffUrl> result = query.list();
        if (!result.isEmpty() && !result.get(0).getUnconfident()) {
            return ALL_CONFIDENT;
        }
        return result;
    }
}
