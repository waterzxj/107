package room107.service.api.weixin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * @author WangXiao
 */
@Getter
@Setter
@CommonsLog
@AllArgsConstructor
public abstract class AbstractResponse {

    protected String from, to;

    protected abstract String getMessageType();

    protected Document toXml() {
        Document result = DocumentHelper.createDocument();
        Element root = result.addElement("xml");
        root.addElement("ToUserName").addCDATA(to);
        root.addElement("FromUserName").addCDATA(from);
        root.addElement("CreateTime").setText(
                String.valueOf(System.currentTimeMillis()));
        root.addElement("MsgType").addCDATA(getMessageType());
        return result;
    }

    @Override
    public String toString() {
        String result = toXml().asXML();
        log.debug(result);
        return result;
    }

}
