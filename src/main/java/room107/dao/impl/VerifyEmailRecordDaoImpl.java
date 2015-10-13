package room107.dao.impl;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.IVerifyEmailRecordDao;
import room107.datamodel.VerifyEmailRecord;
import room107.datamodel.VerifyEmailStatus;

/**
 * @author WangXiao
 */
@Repository
@SuppressWarnings("unchecked")
public class VerifyEmailRecordDaoImpl extends DaoImpl implements
        IVerifyEmailRecordDao {

    @Override
    public List<VerifyEmailRecord> getRecords(String email,
            VerifyEmailStatus status) {
        Criteria criteria = getSession()
                .createCriteria(VerifyEmailRecord.class);
        if (email != null) {
            criteria.add(Restrictions.eq("email", email).ignoreCase());
        }
        if (status != null) {
            criteria.add(Restrictions.eq("status", status.ordinal()));
        }
        return criteria.list();
    }

    @Override
    public VerifyEmailRecord getRecord(String username, String email) {
        return (VerifyEmailRecord) getSession()
                .createCriteria(VerifyEmailRecord.class)
                .add(Restrictions.eq("username", username).ignoreCase())
                .add(Restrictions.eq("email", email).ignoreCase())
                .uniqueResult();
    }

    @Override
    public List<VerifyEmailRecord> updateRecords(String emailDomain,
            VerifyEmailStatus status) {
        Validate.notNull(emailDomain);
        Validate.notNull(status);
        List<VerifyEmailRecord> records = (List<VerifyEmailRecord>) getSession()
                .createCriteria(VerifyEmailRecord.class)
                .add(Restrictions.like("email", "%@" + emailDomain)
                        .ignoreCase()).list();
        for (VerifyEmailRecord r : records) {
            r.setStatus(status.ordinal());
            update(r);
        }
        return records;
    }

}
