package room107.service.api.weixin.response;

import lombok.ToString;

/**
 * @author WangXiao
 */
@ToString
public class WeiXinResponse {

    public static final int CODE_OK = 0;

    /*
     * Access token error.
     */
    public static final int CODE_INVALID_CREDENTIAL = 40001;

    public static final int CODE_INVALID_OPENID = 40003;

    public int errcode;

    public String errmsg;

}
