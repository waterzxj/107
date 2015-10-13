/**
 * 
 */
package room107.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import room107.util.UserBehaviorLog;
import room107.web.house.HouseController;

/**
 * @author yanghao
 */
@Controller
public class RootController {
    
    @Autowired
    private HouseController houseController;

    @RequestMapping("/share")
    public String share() {
        UserBehaviorLog.WEIXIN.info("View share");
        return "mobile/share";
    }

    @RequestMapping("{houseId:[\\d]+}")
    public String viewHouse(HttpServletRequest request,
            HttpServletResponse response, @PathVariable long houseId,
            Map<String, Object> map) {
        return houseController.houseDetail(request, response, houseId, map);
    }

    @RequestMapping("r{roomId}")
    public String viewRoom(HttpServletRequest request,
            HttpServletResponse response, @PathVariable long roomId,
            Map<String, Object> map) {
        return houseController.roomDetail(request, response, roomId, map);
    }

}
