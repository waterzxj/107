package room107.dao.house;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import room107.dao.impl.DaoImpl;
import room107.datamodel.PoiHouse;
import room107.datamodel.PoiType;

/**
 * @author WangXiao
 */
@SuppressWarnings("unchecked")
@Repository
public class PoiHouseDaoImpl extends DaoImpl implements IPoiHouseDao {

    @Override
    public List<PoiHouse> getByNames(Collection<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Collections.emptyList();
        }
        return getSession().createCriteria(PoiHouse.class)
                .add(Restrictions.in("name", names)).list();
    }

    @Override
    public List<PoiHouse> getByHouse(long houseId) {
        return getSession().createCriteria(PoiHouse.class)
                .add(Restrictions.eq("houseId", houseId)).list();
    }

    @Override
    public void remove(long houseId, PoiType type) {
        String sql = "delete PoiHouse where houseId=" + houseId;
        if (type != null) {
            sql += " and type=" + type.ordinal();
        }
        getSession().createQuery(sql).executeUpdate();
    }

    @Override
    public void addAll(Collection<PoiHouse> poiHouses) {
        for (PoiHouse poiHouse : poiHouses) {
            save(poiHouse);
        }
    }

}
