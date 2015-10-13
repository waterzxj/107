package room107.web.mobile;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import room107.datamodel.User;
import room107.service.api.weixin.Code2TicketManager;
import room107.util.UserUtils;

/**
 * @author WangXiao
 */
@Controller
public class IntroductionController {

    @Autowired
    private Code2TicketManager code2TicketManager;
    
    @RequestMapping(value = "/weixin")
    public String home(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> map) {
        /*
         * ticket
         */
        User user = UserUtils.getUser(request, response);
        if (user != null) {
            String ticket = code2TicketManager.getTicketNoneBlocked(user
                    .getId());
            if (ticket != null) {
                map.put("ticket", ticket);
            }
        }
        return "weixin-intro";
    }

}
