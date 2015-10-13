package room107.web.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import room107.service.totoro.ITotoroService;

/**
 * @author WangXiao
 */
@Controller
@RequestMapping("/admin/totoro")
public class AdminTotoroController {

    @Autowired
    private ITotoroService totoroService;

    @RequestMapping(value = "/")
    public String home(HttpServletRequest request, Map<String, Object> map) {
        return "admin/totoro";
    }

    @RequestMapping(value = "/query")
    public String query(HttpServletRequest request, Map<String, Object> map) {
        String username = request.getParameter("username");
        map.put("username", username);
        map.put("sniffs", totoroService.getVisitedUrls(username));
        return "admin/totoro";
    }

}
