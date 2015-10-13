package room107.service.house.search;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.StringUtils;

/**
 * @author WangXiao
 */
@RequiredArgsConstructor
public class PriceRange {

    /**
     * All included. Null means not limited.
     */
    @Getter
    @NonNull
    private Integer from, to;

    /**
     * 
     * @param rangeFormat
     *            for example, given a number "x", "y"
     *            <ul>
     *            <li>null or empty: range not limited</li>
     *            <li>x: [x, x]</li>
     *            <li>x+: >=x</li>
     *            <li>x-: <=x</li>
     *            <li>x-y: [x, y]</li>
     *            </ul>
     * @throws Exception
     *             when format is invalid
     */
    public PriceRange(String rangeFormat) throws Exception {
        rangeFormat = StringUtils.trimToNull(rangeFormat);
        if (rangeFormat != null) {
            if (rangeFormat.endsWith("+")) { // x+
                from = Integer.decode(rangeFormat.substring(0,
                        rangeFormat.length() - 1));
            } else if (rangeFormat.endsWith("-")) { // x-
                to = Integer.decode(rangeFormat.substring(0,
                        rangeFormat.length() - 1));
            } else {
                int i = rangeFormat.indexOf('-');
                if (i > 0) { // x-y
                    from = Integer.decode(rangeFormat.substring(0, i));
                    to = Integer.decode(rangeFormat.substring(i + 1));
                } else { // x
                    from = to = Integer.decode(rangeFormat);
                }
            }

        }
    }

    @Override
    public String toString() {
        return "[" + from + "," + to + "]";
    }

}
