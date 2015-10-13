package room107.dao.impl;

import java.util.Collections;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.springframework.stereotype.Repository;

import room107.dao.ISniffUrlResultDao;
import room107.datamodel.SniffUrl;
import room107.datamodel.SniffUrlResult;

/**
 * @author WangXiao
 */
@Repository
public class SniffUrlResultDaoImpl extends DaoImpl implements
        ISniffUrlResultDao {

    @Override
    public Long getMaxSniffUrlId(String username) {
        if (username == null) {
            return null;
        }
        return (Long) getSession().createCriteria(SniffUrlResult.class)
                .add(Restrictions.eq("id.username", username))
                .setProjection(Projections.max("id.urlId")).uniqueResult();
    }

    /*
     * "SELECT * FROM sniff_url u, sniff_url_result r WHERE r.username='ww135' AND u.urlId=r.urlId AND r.visited=1 AND u.TYPE =0 AND u.weight >= 0.9999 AND u.weight<=1.0001"
     */
    @Override
    public int getVisitedBlackUrlCount(String username, float fromWeight,
            float toWeight) {
        if (username == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM sniff_url u, sniff_url_result r WHERE r.username='"
                + username
                + "' AND r.visited=1 AND u.urlId=r.urlId AND u.TYPE =0 AND u.weight>="
                + fromWeight + " AND u.weight<=" + toWeight;
        SQLQuery query = (SQLQuery) getSession().createSQLQuery(sql);
        return ((Number) query.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SniffUrl> getVisitedUrls(String username) {
        if (username == null) {
            return Collections.emptyList();
        }
        String sql = "SELECT u.urlId, u.url, u.type, u.weight, u.modifiedTime FROM sniff_url u, sniff_url_result r WHERE r.username='"
                + username
                + "' AND r.visited=1 AND u.urlId=r.urlId order by u.type asc, u.weight desc";

        SQLQuery query = (SQLQuery) getSession().createSQLQuery(sql)
                .setResultTransformer(Transformers.aliasToBean(SniffUrl.class));
        query.addScalar("urlId", LongType.INSTANCE);
        query.addScalar("url", StringType.INSTANCE);
        query.addScalar("type", IntegerType.INSTANCE);
        query.addScalar("weight", FloatType.INSTANCE);
        query.addScalar("modifiedTime", TimestampType.INSTANCE);
        return query.list();
    }

}
