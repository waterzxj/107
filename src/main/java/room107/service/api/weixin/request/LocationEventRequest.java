/**
 * 
 */
package room107.service.api.weixin.request;

import lombok.Getter;
import lombok.Setter;

import org.dom4j.Element;

import room107.datamodel.Location;

/**
 * @author yanghao
 */
@Setter
@Getter
public class LocationEventRequest extends AbstractRequest {

    double x, y;

    public LocationEventRequest(Element root) {
        super(root);
        x = Double.parseDouble(root.elementText(LONGITUDE));
        y = Double.parseDouble(root.elementText(LATITUDE));
    }

    public Location toLocation() {
        return new Location(x, y);
    }

    @Override
    public String toString() {
        return super.toString() + ", x=" + x + ", y=" + y;
    }

}
