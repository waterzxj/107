package room107.service.api.weixin.request;

import lombok.Getter;
import lombok.Setter;

import org.dom4j.Element;

/**
 * @author WangXiao
 */
@Setter
@Getter
public class VoiceRequest extends AbstractRequest {

    String recognition;

    public VoiceRequest(Element root) {
        super(root);
        recognition = getNonEmptyValue(root, RECOGNITION);
    }

    @Override
    public String toString() {
        return super.toString() + ", recognition=" + recognition;
    }

}
