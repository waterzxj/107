/**
 * 
 */
package room107.service.admin;

import java.util.List;

import room107.datamodel.ExternalHouse;
import room107.datamodel.House;

/**
 * @author yanghao
 */
public interface IExternalHouseService {
    
    public List<ExternalHouse> getUnauditedExternalHouse();
    
    public List<ExternalHouse> getUnauditedForumHouse();
    
    public ExternalHouse getHouse(long id);
    
    public ExternalHouse getHouseByHouseId(long id);
    
    public void bindHouse(long id, House house);
    
    public void createHouse(String url, House house);
    
    public void discardHouse(long id);
    
    public void delayHouse(long id);

}
