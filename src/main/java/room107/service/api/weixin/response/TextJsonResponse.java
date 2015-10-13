package room107.service.api.weixin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author WangXiao
 */
public class TextJsonResponse extends AbstractJsonResponse {

    @AllArgsConstructor
    @ToString
    public static class Text {
        public String content;
    }

    @Getter
    private Text text;

    public TextJsonResponse(String to, String content) {
        super("text", to);
        text = new Text(content);
    }

}
