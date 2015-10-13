package room107.web.api.weixin;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.Validate;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.service.api.weixin.WeiXinService;
import room107.service.api.weixin.request.AbstractRequest;
import room107.service.api.weixin.request.LocationEventRequest;
import room107.service.api.weixin.request.LocationRequest;
import room107.service.api.weixin.request.MenuEventRequest;
import room107.service.api.weixin.request.RequestFactory;
import room107.service.api.weixin.request.ScanEventRequest;
import room107.service.api.weixin.request.SubscribeEventRequest;
import room107.service.api.weixin.request.TextRequest;
import room107.service.api.weixin.request.UnsubscribeEventRequest;
import room107.service.api.weixin.request.VoiceRequest;
import room107.util.UserBehaviorLog;
import room107.util.XmlUtils;

/**
 * @author WangXiao
 */
@CommonsLog
@Controller
public class WeiXinController {

    @Autowired
    private WeiXinService weiXinService;

    @RequestMapping("/api/wx")
    @ResponseBody
    public String handle(HttpServletRequest request,
            HttpServletResponse response) throws UnsupportedEncodingException {
        /*
         * auth
         */
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (request.getRequestURL().indexOf("localhost") < 0) {
            try {
                weiXinService.auth(request);
            } catch (Exception e) {
                log.error("Auth failed: " + e.getMessage());
                return null;
            }
        }
        /*
         * ACK when verify
         */
        if (request.getParameter("echostr") != null) {
            String echo = request.getParameter("echostr");
            log.info("echostr: " + echo);
            return echo;
        }
        /*
         * parse message
         */
        Element root;
        try {
            root = XmlUtils.getRootElement(request);
        } catch (Exception e) {
            log.error("Invalid format request", e);
            return null;
        }
        /*
         * handle message
         */
        AbstractRequest message;
        try {
            // convert
            message = RequestFactory.getRequest(root);
            Validate.notNull(message, "null message");
            // log
            UserBehaviorLog.WEIXIN.info(message);
            // handle
            weiXinService.createIfNotExist(message.getFrom());
            if (message instanceof SubscribeEventRequest) {
                return weiXinService
                        .handleSubscribeEventRequest((SubscribeEventRequest) message);
            }
            if (message instanceof ScanEventRequest) {
                return weiXinService
                        .handleScanEventRequest((ScanEventRequest) message);
            }
            if (message instanceof UnsubscribeEventRequest) {
                return weiXinService
                        .handleUnsubscribeEventRequest((UnsubscribeEventRequest) message);
            }
            if (message instanceof MenuEventRequest) {
                return weiXinService
                        .handleMenuEventRequest((MenuEventRequest) message);
            }
            if (message instanceof LocationEventRequest) {
                return weiXinService.handleLocationEventRequest((LocationEventRequest) message);
            }
            if (message instanceof TextRequest) {
                return weiXinService.handleTextRequest((TextRequest) message);
            }
            if (message instanceof VoiceRequest) {
                return weiXinService.handleVoiceRequest((VoiceRequest) message);
            }
            if (message instanceof LocationRequest) {
                return weiXinService
                        .handleLocationRequest((LocationRequest) message);
            }
            log.info("Unsupported message:\n" + message);
            return weiXinService.handleUnsupportedMessage(message);
        } catch (Exception e) {
            log.error("Handle message failed: " + root.asXML(), e);
            return null;
        }
    }

}
