package room107.service.api.weixin.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.dom4j.Element;

/**
 * @author WangXiao
 */
@Setter
@Getter
@NoArgsConstructor
public abstract class AbstractEventRequest extends AbstractRequest {

    protected String event, eventKey;

    public AbstractEventRequest(Element root) {
        super(root);
        event = getNonEmptyValue(root, EVENT);
        eventKey = getNonEmptyValue(root, EVENT_KEY);
    }

    @Override
    public String toString() {
        return super.toString() + ", event=" + event + ", eventKey=" + eventKey;
    }

}
