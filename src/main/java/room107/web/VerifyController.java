package room107.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import room107.datamodel.User;
import room107.datamodel.WxUser;
import room107.service.user.IUserService;
import room107.service.user.IVerifyService;
import room107.service.user.IVerifyService.ConfirmData;
import room107.service.user.IVerifyService.ConfirmSource;
import room107.util.UserUtils;

/**
 * @author WangXiao
 */
@Controller
public class VerifyController {

    @Autowired
    private IVerifyService verifyService;

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/verify/{code}")
    public String confirm(HttpServletRequest request,
            HttpServletResponse response, @PathVariable String code,
            Map<String, Object> map) {
        ConfirmData data = null;
        /*
         * for debug
         */
        if (code.equals("mobile")) {
            data = new ConfirmData("test", "xxx@yyy.com", ConfirmSource.MOBILE);
        } else {
            data = ConfirmData.decrypt(code);
        }
        if (data != null) { // confirm
            /*
             * mobile
             */
            if (data.getSource() == ConfirmSource.MOBILE) {
                return confirmMobile(data);
            }
            /*
             * PC
             */
            if (verifyService.confirmEmail(data)) {
                map.put("confirm", true);
                map.put("title", "恭喜你，认证成功");
                map.put("desc", "现在你可以查看房东联系方式了！");
                map.put("button", "开始找房");
                map.put("url", "/house/search");
                map.put("timing", 5);
            } else {
                map.put("error", true);
                map.put("help", true);
                map.put("title", "邮箱已被认证");
                map.put("desc", "请尝试其他邮箱");
                map.put("button", "重新认证");
                map.put("url", "/user/auth/verify");
            }
            // auto login (force update)
            User user = userService.getUser(data.getUsername());
            WxUser wxUser = userService.getWxUserByUsername(data.getUsername());
            UserUtils.login(user, wxUser, request, response);
        } else { // invalid code
            map.put("error", true);
            map.put("help", true);
            map.put("title", "验证码无效");
            map.put("desc", "验证码无法被识别，请检查URL是否完整");
            map.put("button", "重新认证");
            map.put("url", "/user/auth/verify");
        }
        map.put("pageTitle", "邮箱认证");
        map.put("bannerTitle", "认证");
        map.put("bannerDesc", "认证后即可查看房东联系方式");
        map.put("data", data);
        return "result";
    }

    private String confirmMobile(ConfirmData data) {
        if (verifyService.confirmEmail(data)) {
            return "mobile/verify-success";
        } else {
            return "mobile/verify-fail";
        }
    }

    public static void main(String[] args) {
        ConfirmData data = ConfirmData.decrypt("CczGQdqWZHEk632XsqRuqrZIdcZb0PLjLR1wcs8ZUil-tcEs1NhT7UnVi7uUFFDDKyCYC_oXtY-XNqxzT0PXygLssBvyIRpFh0kYLATNCL2kMBbUzPuFIS3AomhE3PkI7meWADRwoTk");
                //new ConfirmData("user", "xu.x.wang@daimler.com", ConfirmSource.PC);
        System.out.println(data.getEmail());
    }
    
}
