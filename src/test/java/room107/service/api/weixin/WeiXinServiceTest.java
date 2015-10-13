package room107.service.api.weixin;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import room107.service.api.weixin.request.MenuEventRequest;
import room107.service.api.weixin.request.TextRequest;
import room107.tool.AutowiredTest;
import room107.wechat.Constants;

public class WeiXinServiceTest extends AutowiredTest {

    @Autowired
    private WeiXinService service;

    @Test
    public void testHandleMenuEventRequest() {
    }

    // @Test
    public void testHandleTextRequest() {
        Element e = DocumentHelper.createElement("xml");
        e.addElement("ToUserName").addCDATA("gh_cebb4e62e08e");
        e.addElement("FromUserName").addCDATA("ozt7RjqUY3aVJIG88o9xvdCdopWU");
        e.addElement("CreateTime").setText("123456");
        e.addElement("MsgType").addCDATA("text");
        e.addElement("Content").addCDATA("北京大学");
        TextRequest request = new TextRequest(e);
        System.out.println(service.handleTextRequest(request));
        ;
    }

    @Test
    public void testHandleLocationRequest() {
    }

    @Test
    public void testHandleUnsupportedRequest() {
    }

    @Test
    @Rollback(value = false)
    public void testhandleMenuEventRequest() {
        String openId = "ozt7RjqUY3aVJIG88o9xvdCdopWU";
        // String openId = "test";
        // // subscribe
        // SubscribeEventRequest subscribeRequest = new SubscribeEventRequest();
        // subscribeRequest.setFrom(openId);
        // subscribeRequest
        // .setEvent(Constants.EventType.subscribe.toString());
        // System.out.println(service.handleSubscribeEventRequest(subscribeRequest));
        // // refresh
        MenuEventRequest refreshRequest = new MenuEventRequest();
        refreshRequest.setFrom(openId);
        refreshRequest.setEvent(Constants.EventType.click.toString());
        refreshRequest.setEventKey(MenuEventRequest.EventKey.SUBSCRIBE
                .toString());
        System.out.println(service.handleMenuEventRequest(refreshRequest));
        // // search
        // TextRequest textRequest = new TextRequest();
        // textRequest.setFrom(openId);
        // textRequest.setContent("北大 清华");
        // System.out.println(service.handleTextRequest(textRequest));
        // // refresh
        // refreshRequest = new MenuEventRequest();
        // refreshRequest.setFrom(openId);
        // refreshRequest.setEvent(Constants.EventType.click.toString());
        // refreshRequest.setEventKey(MenuEventRequest.EventKey.SUBSCRIBE.toString());
        // System.out.println(service.handleMenuEventRequest(refreshRequest));
    }

}
