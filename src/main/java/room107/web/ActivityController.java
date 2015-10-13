package room107.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ActivityController {
    
    @RequestMapping(value = "/pku")
    public String pkuActivity(){
        return "activity/pku";
    }
    
    @RequestMapping(value = "/m/pku")
    public String pkuMobileActivity(){
        return "activity/pku-mobile";
    }
}
