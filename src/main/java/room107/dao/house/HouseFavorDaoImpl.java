package room107.dao.house;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.impl.DaoImpl;
import room107.datamodel.HouseFavor;

/**
 * @author WangXiao
 */
@SuppressWarnings("unchecked")
@Repository
public class HouseFavorDaoImpl extends DaoImpl implements IHouseFavorDao {

    @Override
    public HouseFavor get(HouseFavor houseFavor) {
        return (HouseFavor) getSession()
                .createCriteria(HouseFavor.class)
                .add(Restrictions.eq("username", houseFavor.getUsername())
                        .ignoreCase())
                .add(Restrictions.eq("channel", houseFavor.getChannel()))
                .add(Restrictions.eq("type", houseFavor.getType()))
                .add(Restrictions.eq("itemId", houseFavor.getItemId()))
                .uniqueResult();
    }

    @Override
    public List<HouseFavor> getAll(String username) {
        return getSession().createCriteria(HouseFavor.class)
                .add(Restrictions.eq("username", username).ignoreCase())
                .add(Restrictions.eq("status", STATUS_OK)).list();
    }
}
