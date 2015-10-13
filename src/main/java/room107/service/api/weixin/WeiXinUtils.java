package room107.service.api.weixin;

import org.apache.commons.lang.Validate;

import room107.service.api.weixin.response.WeiXinResponse;
import room107.util.JsonUtils;
import room107.util.StringUtils;
import room107.util.WebUtils;

/**
 * @author WangXiao
 */
public class WeiXinUtils {

    public static WeiXinResponse post(String url, String content)
            throws Exception {
        Validate.notNull(url);
        String s = WebUtils.postJson(url, content);
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        return JsonUtils.fromJson(content, WeiXinResponse.class);
    }

}
