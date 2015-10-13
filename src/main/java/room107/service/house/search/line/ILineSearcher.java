package room107.service.house.search.line;

import java.util.Collection;
import java.util.List;

import room107.dao.house.HouseResult;

/**
 * @author WangXiao
 */
public interface ILineSearcher {

    List<HouseResult> search(Collection<String> lines,
            Collection<HouseResult> candidates);

}
