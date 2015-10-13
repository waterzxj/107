package room107.web.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.datamodel.House;
import room107.datamodel.HouseAndRoom;
import room107.datamodel.HouseAndUser;
import room107.datamodel.LogType;
import room107.datamodel.Room;
import room107.datamodel.web.JsonResponse;
import room107.service.admin.IAdminService;
import room107.service.admin.IExternalHouseService;
import room107.service.house.IHouseService;
import room107.service.message.MessageService;
import room107.service.message.type.HouseAuditChanged;
import room107.util.JsonUtils;
import room107.util.StringUtils;
import room107.util.UserStatLog;
import room107.util.WebUtils;

/**
 * @author WangXiao
 */
@Controller
@RequestMapping("/admin/house")
public class AdminHouseController {

    @Autowired
    private IAdminService adminService;
    @Autowired
    private IHouseService houseService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private IExternalHouseService externalHouseService;

    @RequestMapping(value = "/")
    public String home(HttpServletRequest request, Map<String, Object> map) {
        List<HouseAndUser> houses = houseService.getHouseAndUsers(500);
        map.put("houses", houses);
        return "admin/house";
    }
    
    @RequestMapping(value = "/search")
    public String search(HttpServletRequest request, Map<String, Object> map) {
        List<HouseAndUser> houses = new ArrayList<HouseAndUser>();
        String searchKey = WebUtils.getNullIfEmpty(request, "searchKey");
        if (searchKey != null) {
            houses = houseService.getHouseAndUsers(searchKey);
        }
        map.put("houses", houses);
        return "admin/house";
    }

    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    @ResponseBody
    public void audit(HttpServletRequest request, HttpServletResponse response,
            @RequestParam long houseId, @RequestParam int auditStatus) {
        House house = houseService.getHouse(houseId);
        if (auditStatus != house.getAuditStatus()) {
            house.setAuditStatus(auditStatus);
            house.setAuditTime(new Date());
            houseService.updateHouse(house);
            messageService.send(new HouseAuditChanged(houseId, house.getUsername(), auditStatus));
        }
        UserStatLog.log(request, response,
                LogType.adminAudit, house.getId(), null, JsonUtils.toJson("auditStatus", auditStatus));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public void update(HttpServletRequest request, HttpServletResponse response,
            @RequestParam long houseId, @RequestParam int status, @RequestParam float rank)
            throws IOException {
        HouseAndRoom houseAndRoom = houseService.getHouseAndRoom(houseId);
        House house = houseAndRoom.house;
        List<Room> rooms = houseAndRoom.rooms;
        // description
        String desc = StringUtils.trimToNull(request.getParameter("desc"));
        Validate.notNull(desc);
        house.setDescription(desc);
        // rank
        house.setRank(rank);
        for (Room room : rooms) {
            room.setStatus(status);
            room.setRank(rank);
            houseService.updateRoom(room);
        }
        // comment
        house.setComment(request.getParameter("comment"));
        // audit status
        house.setStatus(status);
        houseService.updateHouse(house);
        UserStatLog.log(request, response,
                LogType.updateHouse, houseId, null, JsonUtils.toJson("status", status, "rank", rank));
    }

    @RequestMapping(value = "/close")
    @ResponseBody
    public String close() {
        houseService.closeDeprecated();
        return JsonResponse.OK;
    }


}
