package room107.service.api.weixin.request;


import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.dom4j.Element;

import room107.wechat.Constants;
import room107.wechat.Constants.ElementName;
import room107.wechat.Constants.MessageType;

/**
 * @author WangXiao
 */
@CommonsLog
public class RequestFactory {

    /**
     * @return non-null
     */
    public static AbstractRequest getRequest(Element root) {
        Validate.notNull(root, "null root element");
        if (log.isDebugEnabled()) {
            log.debug(root.asXML());
        }
        String messageType = StringUtils.trimToNull(
                root.elementText(ElementName.MESSAGE_TYPE)).toLowerCase();
        // parse type
        MessageType type = MessageType.valueOf(messageType);
        switch (type) {
        case event:
            String event = StringUtils.trimToEmpty(
                    root.elementText(ElementName.EVENT)).toLowerCase();
            Constants.EventType eventType = Constants.EventType.valueOf(event);
            switch (eventType) {
            case subscribe:
                return new SubscribeEventRequest(root);
            case scan:
                return new ScanEventRequest(root);
            case click:
                return new MenuEventRequest(root);
            case unsubscribe:
                return new UnsubscribeEventRequest(root);
            case location:
                return new LocationEventRequest(root);
            }    
            break;
        case text:
            return new TextRequest(root);
        case location:
            return new LocationRequest(root);
        case voice:
            return new VoiceRequest(root);
        default:
            break;
        }
        return new AbstractRequest(root);
    }

}
