/**
 * 
 */
package room107.service.admin;

import java.util.Date;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.house.IExternalHouseDao;
import room107.datamodel.AuditStatus;
import room107.datamodel.ExternalHouse;
import room107.datamodel.House;
import room107.datamodel.HouseSource;
import room107.util.EncryptUtils;

/**
 * @author yanghao
 */
@Service
@CommonsLog
@Transactional
public class ExternalHouseServiceImpl implements IExternalHouseService {

    @Autowired
    private IExternalHouseDao externalHouseDao;

    @Override
    public List<ExternalHouse> getUnauditedExternalHouse() {
        return externalHouseDao.getUnauditedExternalHouse();
    }

    public ExternalHouse getHouse(long id) {
        return externalHouseDao.get(ExternalHouse.class, id);
    }

    @Override
    public void bindHouse(long id, House house) {
        ExternalHouse externalHouse = externalHouseDao.get(ExternalHouse.class,
                id);
        externalHouse.setOperationStatus(AuditStatus.ACCEPTED.ordinal());
        externalHouse.setHouseId(house.getId());
        externalHouse.setOperationTime(new Date());
        try {
            externalHouse.setEncryptKey(EncryptUtils.encryptAnomynous(house
                    .getUsername()));
        } catch (Exception e) {
            log.error(e, e);
        }
        externalHouseDao.saveOrUpdate(externalHouse);
    }

    @Override
    public void discardHouse(long id) {
        ExternalHouse externalHouse = externalHouseDao.get(ExternalHouse.class,
                id);
        externalHouse.setOperationStatus(AuditStatus.REJECTED.ordinal());
        externalHouse.setOperationTime(new Date());
        externalHouseDao.saveOrUpdate(externalHouse);
    }

    @Override
    public void createHouse(String url, House house) {
        ExternalHouse externalHouse = new ExternalHouse(house);
        externalHouse.setUrl(url);
        externalHouse.setSource(HouseSource.OPERATION.ordinal());
        externalHouse.setExternalHouseId(HouseSource.OPERATION.toString() + "_"
                + house.getId());
        externalHouse.setOperationStatus(AuditStatus.ACCEPTED.ordinal());
        externalHouse.setHouseId(house.getId());
        externalHouse.setOperationTime(new Date());
        try {
            externalHouse.setEncryptKey(EncryptUtils.encryptAnomynous(house
                    .getUsername()));
        } catch (Exception e) {
            log.error(e, e);
        }
        externalHouseDao.saveOrUpdate(externalHouse);
    }

    @Override
    public ExternalHouse getHouseByHouseId(long id) {
        return externalHouseDao.getExternalHouseByHouseId(id);
    }

    @Override
    public List<ExternalHouse> getUnauditedForumHouse() {
        return externalHouseDao.getUnauditedForumHouse();
    }

    @Override
    public void delayHouse(long id) {
        ExternalHouse externalHouse = externalHouseDao.get(ExternalHouse.class,
                id);
        externalHouse.setOperationStatus(AuditStatus.DELAY.ordinal());
        externalHouse.setOperationTime(new Date());
        externalHouseDao.saveOrUpdate(externalHouse);
    }
}
