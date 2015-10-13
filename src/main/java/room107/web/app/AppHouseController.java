/**
 * 
 */
package room107.web.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.datamodel.LogType;
import room107.datamodel.Suite;
import room107.datamodel.web.JsonStatus;
import room107.datamodel.web.SuiteItem;
import room107.service.house.IHouseService;
import room107.util.JsonUtils;
import room107.util.UserBehaviorLog;
import room107.util.UserStatLog;
import room107.util.UserUtils;

/**
 * @author yanghao
 */
@Controller
@RequestMapping("/app/house")
public class AppHouseController {
    
    @Autowired
    private IHouseService houseService;

    @RequestMapping(value = "/h{houseId}")
    @ResponseBody
    public String houseDetail(HttpServletRequest request,
            HttpServletResponse response, @PathVariable long houseId) {
        UserBehaviorLog.HOUSE_VIEW.info("View house by app: "
                + UserUtils.getUserSignature(request, response) + ", houseId="
                + houseId);
        Suite suite = houseService.getSuiteByHouse(houseId);
        if (suite == null) {
            return JsonUtils.toJson(JsonStatus.PROPERTY_NAME, JsonStatus.EMPTY);
        }
        UserStatLog.log(request, response, LogType.detail, houseId, null);
        
        return JsonUtils.toJson(JsonStatus.PROPERTY_NAME, JsonStatus.SUCCESS, "suite", new SuiteItem(suite));
    }

    @RequestMapping(value = "/r{roomId}")
    @ResponseBody
    public String roomDetail(HttpServletRequest request,
            HttpServletResponse response, @PathVariable long roomId) {
        UserBehaviorLog.HOUSE_VIEW.info("View house by app: "
                + UserUtils.getUserSignature(request, response) + ", roomId="
                + roomId);
        Suite suite = houseService.getSuiteByRoom(roomId);
        if (suite == null) {
            return JsonUtils.toJson(JsonStatus.PROPERTY_NAME, JsonStatus.EMPTY);
        }
        UserStatLog.log(request, response, LogType.detail, suite.house.getId(), roomId);
        
        return JsonUtils.toJson(JsonStatus.PROPERTY_NAME, JsonStatus.SUCCESS, "suite", new SuiteItem(suite));
    }
    
}
