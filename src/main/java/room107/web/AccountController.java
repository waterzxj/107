package room107.web;

import javax.servlet.http.HttpServletRequest;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.service.user.IAccountService;

/**
 * @author WangXiao
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    @Setter
    private IAccountService accountService;

    @RequestMapping(value = "/house/apply/{houseId}", method = RequestMethod.POST)
    @ResponseBody
    public String applyForHouse(HttpServletRequest request,
            @PathVariable long houseId) {
        // User user = WebUtils.getSessionValue(request, SessionKeys.USER);
        // Account account = Account.getInstance(request);
        // if (user != null && account != null) {
        // int balance = account.decreaseApplyForHouseBalance();
        // accountService.decreaseHouseBalance(user.getUsername(), houseId);
        // return WebUtils.toJson("balance", balance);
        // }
        return null;
    }
}
