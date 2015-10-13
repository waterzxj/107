package room107.web.admin;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.datamodel.House;
import room107.datamodel.web.JsonResponse;
import room107.service.house.IHouseService;
import room107.service.message.MessageService;
import room107.service.message.type.HousePositionChanged;
import room107.util.WebUtils;

/**
 * @author WangXiao
 */
@Controller
@RequestMapping("/admin/message")
public class AdminMessageController {

    @Autowired
    private IHouseService houseService;

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/HousePositionChanged")
    @ResponseBody
    public String housePositionChanged(HttpServletRequest request)
            throws InterruptedException {
        List<House> houses = null;
        Integer id = WebUtils.getInt(request, "houseId", null);
        if (id == null) { // all
            houses = houseService.getHouses();
        } else {
            houses = Arrays.asList(houseService.getHouse(id));

        }
        for (House house : houses) {
            messageService.send(new HousePositionChanged(house));
            Thread.sleep(1000);
        }
        return JsonResponse.message(houses.size());
    }

}
