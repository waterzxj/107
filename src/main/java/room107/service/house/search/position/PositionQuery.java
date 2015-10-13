package room107.service.house.search.position;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import room107.datamodel.District;
import room107.datamodel.Location;

/**
 * Represented by {@link #location}, {@link #positions} or {@link #districts}.
 * An empty query if {@link #isEmpty()}.
 * 
 * @author WangXiao
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PositionQuery {

    private static final String[] EMPTY = new String[0];

    private Location location;

    private String[] districts = EMPTY;

    private String[] positions = EMPTY;

    public PositionQuery(Location location) {
        this.location = location;
    }

    /**
     * @param positionsDesc
     *            separated by whitespace, e.g. "清华大学 朝阳区"
     */
    public PositionQuery(String positionsDesc) {
        init(StringUtils.split(positionsDesc));
    }

    /**
     * @param positionDescs
     *            each description of position is either a precise position
     *            (e.g. "北京大学") or a district (e.g. "海淀")
     */
    public PositionQuery(Collection<String> positionDescs) {
        if (positionDescs == null || positionDescs.isEmpty()) {
            return;
        }
        init(positionDescs.toArray(new String[positionDescs.size()]));
    }

    /**
     * See {@link #PositionQuery(Collection)}.
     */
    public PositionQuery(String[] positionDescs) {
        init(positionDescs);
    }

    /**
     * @return true when no any limit
     */
    public boolean isEmpty() {
        return location == null && ArrayUtils.isEmpty(districts)
                && ArrayUtils.isEmpty(positions);
    }

    private void init(String[] ss) {
        if (ArrayUtils.isEmpty(ss)) {
            return;
        }
        Set<String> ds = new LinkedHashSet<String>();
        Set<String> ps = new LinkedHashSet<String>();
        for (int i = 0; i < ss.length; i++) {
            String district = District.getDistrictName(ss[i]);
            if (district != null) { // district
                ds.add(district);
            } else { // normal position
                ps.add(ss[i]);
            }
        }
        if (!ds.isEmpty()) {
            districts = ds.toArray(new String[ds.size()]);
        }
        if (!ps.isEmpty()) {
            positions = ps.toArray(new String[ps.size()]);
        }

    }

}
