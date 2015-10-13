/**
 * 
 */
package room107.dao.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.ILogStatResultDao;
import room107.datamodel.LogStatResult;

/**
 * @author yanghao
 */
@Repository
public class LogStatResultDaoImpl extends DaoImpl implements ILogStatResultDao {

    @Override
    public List<LogStatResult> getLatestDistrictStat() {
        Session session = sessionFactory.openSession();
        Date date = DateUtils.addDays(new Date(), -2); 
        @SuppressWarnings("unchecked")
        List<LogStatResult> results = session.createCriteria(LogStatResult.class).add(Restrictions.gt("timestamp", date))
                .add(Restrictions.like("name", "District_%%")).addOrder(Order.desc("timestamp")).list();
        session.close();
        Set<String> districts = new HashSet<String>();
        Iterator<LogStatResult> it = results.iterator();
        while (it.hasNext()) {
            LogStatResult result = it.next();
            if (districts.contains(result.getName())) {
                it.remove();
                continue;
            }
            districts.add(result.getName());
        }
        return results;
    }

}
