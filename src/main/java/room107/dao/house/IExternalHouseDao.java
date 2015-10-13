/**
 * 
 */
package room107.dao.house;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import room107.dao.IDao;
import room107.datamodel.AuditStatus;
import room107.datamodel.ExternalHouse;
import room107.datamodel.House;
import room107.datamodel.HouseSource;

/**
 * @author yanghao
 */
public interface IExternalHouseDao extends IDao {

    public List<ExternalHouse> getUnauditedExternalHouse();

    public List<ExternalHouse> getUnauditedForumHouse();

    public ExternalHouse getExternalHouseByHouseId(long id);

    public int getHouseCount(AuditStatus status);

    public List<ExternalHouse> getExternalHouseByEncryptKey(String key);

    public List<ExternalHouse> getExternalHouseByHouseId(
            Collection<Long> houseIds);

    public int getExternalHouseCountBySource(Date start, HouseSource source);

    public int getExternalEnteringHouseCountBySource(Date start,
            HouseSource source);

    public int getExternalEffectiveHouseCountBySource(Date start,
            HouseSource source);

}