package room107.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.datamodel.User;
import room107.service.totoro.ITotoroService;
import room107.util.JsonUtils;
import room107.util.UserUtils;
import room107.util.WebUtils;

/**
 * @author WangXiao
 */
@Controller
public class TotoroController {

    @Autowired
    private ITotoroService totoroService;
    
    @RequestMapping("/broker/judge")
    @ResponseBody
    public String judge(HttpServletRequest request, HttpServletResponse response) {
        User user = UserUtils.getUser(request, response);
        if (user == null) {
            return null;
        }
        boolean isAgency = totoroService.isAgency(user.getUsername());
        return JsonUtils.toJson("isAgency", isAgency);
    }

    @RequestMapping("/broker/search")
    @ResponseBody
    public String searchBroker(HttpServletRequest request,
            HttpServletResponse response) {
        String brokerId = normalizeBrokerId(request.getParameter("q"));
        return JsonUtils.toJson(totoroService.searchBrokers(
                UserUtils.getCookieId(request, response),
                WebUtils.getRealIp(request), brokerId));
    }

    @RequestMapping(value = "/broker/report", method = RequestMethod.POST)
    @ResponseBody
    public String reportBroker(HttpServletRequest request,
            HttpServletResponse response) {
        String brokerId = normalizeBrokerId(request.getParameter("q"));
        return JsonUtils.toJson(totoroService.reportBrokerId(
                UserUtils.getCookieId(request, response),
                WebUtils.getRealIp(request), brokerId));
    }

    private String normalizeBrokerId(String brokerId) {
        brokerId = StringUtils.trimToNull(brokerId);
        if (brokerId == null) {
            return null;
        }
        if (brokerId.startsWith("+86") && brokerId.length() > 3) {
            brokerId = brokerId.substring(3);
        }
        return brokerId;
    }

}
