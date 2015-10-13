package room107.service.house.search;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import room107.datamodel.GenderType;
import room107.datamodel.Location;
import room107.datamodel.RentType;
import room107.util.StringUtils;

/**
 * @author WangXiao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseQuery {

    /**
     * No limit when null.
     */
    private RentType rentType;

    /**
     * Included.
     */
    private int priceFrom, priceTo = Integer.MAX_VALUE;

    /**
     * Null-able. For a keyword:
     * <ul>
     * <li>position: like "北京大学"</li>
     * <li>subway line: like "十号线" "10号线"</li>
     * <li>subway line: like "355" "355路"</li>
     * </ul>
     */
    private String[] keywords;

    private Location location;
    
    /**
     * tenant gender.
     */
    private GenderType gender;

    /**
     * Included.
     */
    private Date modifiedTimeFrom;

    public HouseQuery(String[] keywords) {
        this.keywords = keywords;
    }

    public HouseQuery(String keywords) {
        this.keywords = StringUtils.split(keywords, " \t\\/,;.，；。、");
    }

    public HouseQuery(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return StringUtils.toString(rentType, priceFrom, priceTo, gender,
                StringUtils.join(keywords, ' '));
    }

}
