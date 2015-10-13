package room107.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * @author WangXiao
 */
public class SqlHelper {

    /**
     * For a clause: select ... from ... where x in ("a", "b", "c").
     * 
     * @param values
     *            non-empty
     * @return ("a", "b", "c")
     */
    public static String getInClause(Collection<?> values) {
        Validate.notEmpty(values);
        List<String> ss = new ArrayList<String>(values.size());
        for (Object o : values) {
            ss.add("'" + o + "'");
        }
        return "(" + StringUtils.join(ss, ',') + ")";
    }
}
