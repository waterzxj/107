package room107.service.api.weixin.request;

import lombok.Getter;
import lombok.Setter;

import org.dom4j.Element;

import room107.datamodel.Location;

/**
 * @author WangXiao
 */
@Setter
@Getter
public class LocationRequest extends AbstractRequest {

    double x, y;

    String label;

    public LocationRequest(Element root) {
        super(root);
        x = Double.parseDouble(root.elementText(LOCATION_Y));
        y = Double.parseDouble(root.elementText(LOCATION_X));
        label = getNonEmptyValue(root, LABEL);
    }

    public Location toLocation() {
        Location location = new Location(x, y);
        location.setAddress(label);
        return location;
    }

    @Override
    public String toString() {
        return super.toString() + ", x=" + x + ", y=" + y + ", label=" + label;
    }

}
