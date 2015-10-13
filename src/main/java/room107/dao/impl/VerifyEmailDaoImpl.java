package room107.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.IVerifyEmailDao;
import room107.datamodel.VerifyEmail;

/**
 * @author WangXiao
 */
@Repository
public class VerifyEmailDaoImpl extends DaoImpl implements IVerifyEmailDao {

    @Override
    public VerifyEmail getVerifyEmail(String domain) {
        domain = StringUtils.trimToNull(domain);
        if (domain == null) {
            return null;
        }
        return (VerifyEmail) getSession().createCriteria(VerifyEmail.class)
                .add(Restrictions.eq("domain", domain).ignoreCase())
                .uniqueResult();
    }

    @Override
    public EmailType getEmailType(String domain) {
        VerifyEmail verifyEmail = getVerifyEmail(domain);
        return verifyEmail == null ? null : EmailType.values()[verifyEmail
                .getType()];
    }

}
