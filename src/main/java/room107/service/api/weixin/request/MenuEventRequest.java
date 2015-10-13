package room107.service.api.weixin.request;

import lombok.NoArgsConstructor;

import org.dom4j.Element;

/**
 * @author WangXiao
 */
@NoArgsConstructor
public class MenuEventRequest extends AbstractEventRequest {

    public MenuEventRequest(Element root) {
        super(root);
    }

    public static enum EventKey {
        SUBSCRIBE, HELP, REFRESH, HOUSE_OPEN, HOUSE_CLOSE, HOUSE_VIEW, UNSUBSCRIBE
    }

}
