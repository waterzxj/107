package room107.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import room107.datamodel.LogType;
import room107.util.UserStatLog;
import room107.web.house.HouseController;

/**
 * @author WangXiao
 */
@Controller
public class IndexController {

    @Autowired
    private HouseController houseController;

    @RequestMapping(value = "/m")
    public String mobile() {
        return "redirect:/m/";
    }

    @RequestMapping({ "/", "/index" })
    public String index(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        UserStatLog.log(request, response, LogType.home);
        return "index";
    }

}