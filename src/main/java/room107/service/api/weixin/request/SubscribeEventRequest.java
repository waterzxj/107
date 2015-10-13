package room107.service.api.weixin.request;

import lombok.NoArgsConstructor;

import org.dom4j.Element;

/**
 * @author WangXiao
 */
@NoArgsConstructor
public class SubscribeEventRequest extends ScanEventRequest {

    public SubscribeEventRequest(Element root) {
        super(root);
    }

}
