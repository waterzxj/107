package room107.web;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author WangXiao
 */
@Controller
@RequestMapping("/error")
public class ErrorController {

    @RequestMapping("/404")
    public String error404(Map<String, Object> map) {
        map.put("code", 404);
        return "error";
    }

    @RequestMapping("/500")
    public String error500(Map<String, Object> map) {
        map.put("code", 500);
        return "error";
    }

}
