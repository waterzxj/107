package room107.service.api.weixin.response;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * @author WangXiao
 */
public class TextResponse extends AbstractResponse {

    /**
     * Like '{XXX}'
     * 
     * @author WangXiao
     */
    public interface Macro {

        String QUERY = "{QUERY}";

        String SUBSCRIPTION = "{SUBSCRIPTION}";

    }

    // menu

    public static final String MENU_REFRESH_NOT_SUBSCRIBE = "尚未订阅。输入关键词（地址/地铁/公交）即可完成订阅（多个词以空格隔开）。";
    public static final String MENU_REFRESH_RESUBSCRIBE = "续订成功！有新房时将自动推送。";
    public static final String MENU_REFRESH_NO_RESULT = "当前订阅关键词：“"
            + Macro.SUBSCRIPTION + "”，有新房时将自动推送。重新输入即可更新订阅。";

    public static final String SIMPLE_HELP = "【订阅&推送】\n输入关键词（地址/地铁/公交），多个时以空格隔开，有新房时将自动推送。重新输入则更新订阅。点击下方菜单「订阅」-「停止订阅」则不再收取新房。";
    
    public static final String MENU_HELP = "官网：107room.com\n\n" + SIMPLE_HELP + "\n\n【语音&位置】使用“语音”说出关键词，或使用“位置”上传地理信息，即可获得附近的房子信息。此操作不影响订阅。\n\n如有问题或建议，欢迎联系我们：support@107room.com";
    
    public static final String MENU_HOUSE_MANAGE = "你的房间将继续出租。未来成功出租后请及时关闭，方便租客浏览有效房间。感谢支持！";
    
    public static final String MENU_UNSUBSCRIBE = "已停止订阅「%s」附近的房子。输入关键词即可重新订阅。";
    
    public static final String MENU_UNSUBSCRIBE_EMPTY = "尚未订阅任何位置。输入关键词即可订阅。";
    
    // house manager
    public static final String HOUSE_VIEW_NULL = "尚未绑定已发房账号。可在电脑端登录已发房账号后扫码绑定。";
    
    public static final String HOUSE_VIEW_EMPTY = "尚未发布房间。";
        
    
    // subscribe
    
    public static final String CALLER = "你好 ，%s!\n\n";

    public static final String SUBSCRIPTION = "欢迎来到107间！\n\n" + SIMPLE_HELP + "\n\n官网：107room.com";

    public static final String BIND = SUBSCRIPTION; // same as subscribe

    // query

    public static final String TEXT_QUERY_INVALID = "未输入任何地址";
    public static final String TEXT_QUERY_NO_RESULT = "已成功订阅关键词：“"
            + Macro.SUBSCRIPTION + "，目前暂无信息，有新房时将自动推送。重新输入即可更新订阅。";

    public static final String VOICE_QUERY_INVALID = "未识别出地址信息。再试一次，或者使用微信“位置”功能进行找房。";
    public static final String VOICE_QUERY_NO_RESULT = "“" + Macro.QUERY
            + "”附近暂无房子信息。";

    public static final String LOCATION_QUERY_NO_RESULT = VOICE_QUERY_NO_RESULT;

    public static final String UNSUPPORTED_REQUEST = "不支持该搜索方式。请尝试：\n\n"
            + MENU_HELP;

    // message

    public static final String MESSAGE_CONFIRM_RESUBSCRIBE = "受微信限制，“"
            + Macro.SUBSCRIPTION + "”附近的新房推送即将过期。点击「订阅」-「刷新信息」即可续订。";
    
    public static final String MESSAGE_MANAGE_HOUSE = "你发布的房间是否已租出？点击“关闭出租”即可关闭房间。尚未出租点击「订阅」-「刷新信息」以继续出租";
    
    private String content;

    public TextResponse(String from, String to, String content) {
        super(from, to);
        this.content = content;
    }

    @Override
    protected String getMessageType() {
        return "text";
    }

    @Override
    protected Document toXml() {
        Document result = super.toXml();
        Element root = result.getRootElement();
        root.addElement("Content").addCDATA(content);
        return result;
    }

}
