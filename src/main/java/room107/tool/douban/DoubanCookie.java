package room107.tool.douban;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author WangXiao
 */
@Data
@AllArgsConstructor
public class DoubanCookie {

    private String dbcl2, bid;

    public Map<String, String> getCookies() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("dbcl2", dbcl2);
        result.put("bid", bid);
        return result;
    }

}
