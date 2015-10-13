/**
 * 
 */
package room107.dao;

import java.util.List;

import room107.datamodel.LogStatResult;

/**
 * @author yanghao
 */
public interface ILogStatResultDao extends IDao {
    
    public List<LogStatResult> getLatestDistrictStat();

}
