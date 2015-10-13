package room107.web.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author WangXiao
 */
@Controller
public class AdminController {

    @RequestMapping({"/admin/dashboard"})
    public String login(HttpServletRequest request, Map<String, Object> map) {
        return "admin/dashboard";
    }
    
}
