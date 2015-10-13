package room107.service.house.search.line;

import java.util.HashMap;
import java.util.Map;

import room107.datamodel.PoiType;

/**
 * @author WangXiao
 */
public class LineQuery {

    /**
     * @return null when not a line query
     */
    public static String normalize(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        // 355 -> 355路
        try {
            Integer.parseInt(query);
            return query + "路";
        } catch (Exception e) {
        }
        // try subway
        if (query.endsWith("号线")) {
            String subway = query.startsWith("地铁") ? query.substring(2,
                    query.length() - 2) : query
                    .substring(0, query.length() - 2);
            try {
                Integer.parseInt(subway);
                return "地铁" + subway + "号线";
            } catch (Exception e) {
            }
            String number = SUBWAY_MAP.get(subway);
            if (number == null) {
                if (subway.startsWith("十")) {
                    try {
                        number = SUBWAY_MAP.get(subway.substring(1));
                        if (number != null) {
                            number = 1 + number;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
            return number == null ? null : ("地铁" + number + "号线");
        }
        // pattern
        if (PoiType.BUS.matches(query) || PoiType.SUBWAY.matches(query)) {
            return query;
        }
        return null;
    }

    private static Map<String, String> SUBWAY_MAP = new HashMap<String, String>();
    static {
        SUBWAY_MAP.put("一", "1");
        SUBWAY_MAP.put("二", "2");
        SUBWAY_MAP.put("三", "3");
        SUBWAY_MAP.put("四", "4");
        SUBWAY_MAP.put("五", "5");
        SUBWAY_MAP.put("六", "6");
        SUBWAY_MAP.put("七", "7");
        SUBWAY_MAP.put("八", "8");
        SUBWAY_MAP.put("九", "9");
        SUBWAY_MAP.put("十", "10");
    }

}
