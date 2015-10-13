package room107.service.api.weixin.request;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.dom4j.Element;

import room107.wechat.Constants;

/**
 * @author WangXiao
 */
@Setter
@Getter
@CommonsLog
@NoArgsConstructor
public class AbstractRequest implements Constants.ElementName {

    protected String id, from, to;

    protected Date time;

    protected Element root;

    private static final String DATE_FORMAT = "yyyyMMdd-HHmmss";

    public AbstractRequest(Element root) {
        Validate.notNull(root);
        this.root = root;
        id = getNonEmptyValue(root, MESSAGE_ID);
        from = getNonEmptyValue(root, FROM_USER_NAME);
        to = getNonEmptyValue(root, TO_USER_NAME);
        try {
            Date d = new Date();
            d.setTime(Long.parseLong(root.element(CREATE_TIME).getText()) * 1000);
            time = d;
        } catch (Exception e) {
            log.error("Failed to parse time: "
                    + root.element(CREATE_TIME).getText(), e);
        }
    }

    protected String getNonEmptyValue(Element root, String elementName) {
        return StringUtils.trimToNull(root.elementText(elementName));
    }

    public String toXml() {
        return root.asXML();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": id=" + id + ", from=" + from
                + ", to=" + to + ", time="
                + new SimpleDateFormat(DATE_FORMAT).format(time);
    }

}
