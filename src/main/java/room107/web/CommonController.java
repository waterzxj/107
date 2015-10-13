package room107.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import room107.util.WebUtils;

/**
 * @author WangXiao
 */
@RequestMapping("/common")
@Controller
public class CommonController {

    @RequestMapping(value = "/result")
    public String result(HttpServletRequest request, Map<String, Object> map) {
        /*
         * page
         */
        map.put("pageTitle", request.getParameter("pageTitle"));
        map.put("bannerTitle", request.getParameter("bannerTitle"));
        map.put("bannerDesc", request.getParameter("bannerDesc"));
        /*
         * UI
         */
        map.put("title", request.getParameter("title"));
        map.put("desc", request.getParameter("desc"));
        map.put("button", request.getParameter("button"));
        map.put("url", request.getParameter("url"));
        map.put("timing", WebUtils.getInt(request, "timing", null));
        map.put("error", WebUtils.getBoolean(request, "error", false));
        map.put("help", WebUtils.getBoolean(request, "help", false));
        return "result";
    }

}
