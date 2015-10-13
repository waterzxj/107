package room107.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.util.UserBehaviorLog;
import room107.util.UserUtils;

/**
 * @author WangXiao
 */
@Controller
public class LabController {

    @RequestMapping("/weekly")
    public String weekly(HttpServletRequest request,
            HttpServletResponse response) {
        UserBehaviorLog.LAB_WEEKLY.info("View weekly: "
                + UserUtils.getUserSignature(request, response));
        return "lab/weekly";
    }

    @RequestMapping("/weekly/all")
    public String weeklyAll(HttpServletRequest request,
            HttpServletResponse response) {
        UserBehaviorLog.LAB_WEEKLY.info("View weekly-all: "
                + UserUtils.getUserSignature(request, response));
        return "lab/weekly-all";
    }

    @RequestMapping("/totoro")
    public String searchBroker(HttpServletRequest request,
            HttpServletResponse response) {
        return "lab/broker-search";
    }

    @RequestMapping("/square")
    public String square(HttpServletRequest request,
            HttpServletResponse response) {
        UserBehaviorLog.LAB_SQUARE.info("View square: "
                + UserUtils.getUserSignature(request, response));
        return "lab/square";
    }

    @RequestMapping("/stat")
    @ResponseBody
    public String stat(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String target) {
        UserBehaviorLog.STAT.info(UserUtils.getUserSignature(request, response)
                + ", target=" + target + ", action=" + action);
        return null;
    }

}
