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
public class TextRequest extends AbstractRequest {

    private String content;

    public TextRequest(Element root) {
        super(root);
        content = getNonEmptyValue(root, CONTENT);
    }

    @Override
    public String toString() {
        return super.toString() + ", content=" + content;
    }

}
