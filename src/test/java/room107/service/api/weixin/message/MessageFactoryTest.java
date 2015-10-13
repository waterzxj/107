package room107.service.api.weixin.message;

import org.junit.Test;

import room107.service.api.weixin.request.RequestFactory;
import room107.util.XmlUtils;

/**
 * @author WangXiao
 */
public class MessageFactoryTest {

    @Test
    public void testGetMessage() throws Exception {
        System.out
                .println(RequestFactory.getRequest(XmlUtils
                        .getRootElement("<xml> <ToUserName><![CDATA[toUser]]></ToUserName> <FromUserName><![CDATA[fromUser]]></FromUserName>  <CreateTime>1348831860</CreateTime> <MsgType><![CDATA[text]]></MsgType> <Content><![CDATA[this is a test]]></Content> <MsgId>1234567890123456</MsgId> </xml>")));
        System.out
                .println(RequestFactory.getRequest(XmlUtils
                        .getRootElement("<xml><ToUserName><![CDATA[toUser]]></ToUserName><FromUserName><![CDATA[fromUser]]></FromUserName><CreateTime>1351776360</CreateTime><MsgType><![CDATA[location]]></MsgType><Location_X>23.134521</Location_X><Location_Y>113.358803</Location_Y><Scale>20</Scale><Label><![CDATA[位置信息]]></Label><MsgId>1234567890123456</MsgId></xml>")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMessageFail() throws Exception {
        System.out
                .println(RequestFactory.getRequest(XmlUtils
                        .getRootElement("<xml> <ToUserName><![CDATA[toUser]]></ToUserName> <FromUserName><![CDATA[fromUser]]></FromUserName> <CreateTime>1348831860</CreateTime> <MsgType><![CDATA[image]]></MsgType> <PicUrl><![CDATA[this is a url]]></PicUrl> <MediaId><![CDATA[media_id]]></MediaId> <MsgId>1234567890123456</MsgId> </xml>")));
        System.out
                .println(RequestFactory.getRequest(XmlUtils
                        .getRootElement("<xml><ToUserName><![CDATA[toUser]]></ToUserName><FromUserName><![CDATA[FromUser]]></FromUserName><CreateTime>123456789</CreateTime><MsgType><![CDATA[event]]></MsgType><Event><![CDATA[subscribe]]></Event></xml>")));
    }

}
