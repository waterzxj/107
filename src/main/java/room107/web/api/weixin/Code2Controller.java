package room107.web.api.weixin;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.datamodel.User;
import room107.datamodel.web.UserInfo;
import room107.service.api.weixin.Code2TicketManager;
import room107.service.api.weixin.WechatBindManager;
import room107.service.user.IUserService;
import room107.util.JsonUtils;
import room107.util.UserUtils;

/**
 * @author HaoYang
 */
@Controller
public class Code2Controller {

    @Autowired
    private Code2TicketManager code2TicketManager;
    
    @Autowired
    private WechatBindManager wechatBindManager;
    
    @Autowired
    private IUserService userService;

    @RequestMapping("/api/wx/ticket")
    @ResponseBody
    public String get(HttpServletRequest request, HttpServletResponse response)
            throws UnsupportedEncodingException {
        /*
         * ticket
         */
        User user = UserUtils.getUser(request, response);
        if (user != null) {
            return code2TicketManager.getTicketBlocked(user.getId());
        }
        return null;
    }
    
    @RequestMapping("/api/wx/bind")
    @ResponseBody
    public String bind(HttpServletRequest request, HttpServletResponse response)
            throws UnsupportedEncodingException {
        User user = UserUtils.getUser(request, response);
        if (user != null) {
            user = wechatBindManager.wait(user.getUsername());
            if (user != null) {
                UserInfo userInfo = new UserInfo(user);
                return JsonUtils.toJson(userInfo);
            }
        }
        return null;
    }

}
