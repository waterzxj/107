package room107.dao.impl;

import java.util.List;

import org.hibernate.criterion.Order;
import org.springframework.stereotype.Repository;

import room107.dao.IContactDao;
import room107.datamodel.Contact;

/**
 * @author WangXiao
 */
@Repository
@SuppressWarnings("unchecked")
public class ContactDaoImpl extends DaoImpl implements IContactDao {

    @Override
    public List<Contact> getLatest(int maxCount) {
        return getSession().createCriteria(Contact.class)
                .addOrder(Order.desc("createdTime")).setMaxResults(maxCount)
                .list();
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz) {
        return getSession().createCriteria(Contact.class)
                .addOrder(Order.desc("id")).list();
    }

}
