package room107.web.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.datamodel.House;
import room107.datamodel.User;
import room107.datamodel.VerifyEmailStatus;
import room107.datamodel.VerifyEmailType;
import room107.service.house.IHouseService;
import room107.service.user.IUserService;
import room107.service.user.IVerifyService;
import room107.util.JsonUtils;
import room107.util.StringUtils;
import room107.util.UserUtils;
import room107.util.WebUtils;
import room107.web.data.url.ResetPassword;
import room107.web.session.SessionKeys;
import room107.web.session.SessionManager;
import room107.web.session.UnsupportedTypeException;

/**
 * @author WangXiao
 */
@Controller
@RequestMapping("/admin/user")
@CommonsLog
public class AdminUserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IVerifyService verifyService;

    @Autowired
    private IHouseService houseService;
    
    @RequestMapping(value = "/login")
    public String login(HttpServletRequest request, Map<String, Object> map) {
        return "admin/user-login";
    }

    @RequestMapping(value = "/login/submit", method = RequestMethod.POST)
    public String loginSubmit(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> map) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if ("107room".equals(username) && "107admin".equals(password)) {
            try {
                SessionManager.setSessionValue(request, response, SessionKeys.ADMIN, SessionKeys.ADMIN);
            } catch (UnsupportedTypeException e) {
                //never happened
            }
            return "redirect:/admin/dashboard";
        } else {
            map.put("error", "用户名或密码错误");
            return login(request, map);
        }
    }

    @RequestMapping(value = "/super-login")
    public String superLogin(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> map) {
        String username = WebUtils.getNullIfEmpty(request, "username");
        Long houseId = WebUtils.getLong(request, "houseId", null);
        if (StringUtils.isEmpty(username) && houseId != null) {
            House house = houseService.getHouse(houseId);
            username = house.getUsername();
        }
        User user = userService.getUser(username);
        UserUtils.login(user, userService.getWxUserByUsername(username),
                request, response);
        log.info("Super login: username=" + username + ", ip="
                + WebUtils.getRealIp(request));
        return "redirect:/house/manage";
    }

    @RequestMapping(value = "/super-reset")
    public String superReset(HttpServletRequest request,
            HttpServletResponse response, @RequestParam String username,
            Map<String, Object> map) throws Exception {
        String data = JsonUtils.encrypt(new ResetPassword(username, System
                .currentTimeMillis()));
        log.info("Super reset: username=" + username + ", ip="
                + WebUtils.getRealIp(request));
        return "redirect:/user/auth/reset/verify/" + data;
    }

    @RequestMapping(value = "/list")
    public String list(HttpServletRequest request, Map<String, Object> map) {
        List<User> users = userService.getAll();
        map.put("users", users);
        return "admin/user";
    }

    @RequestMapping(value = "/verify")
    @ResponseBody
    public String verify(HttpServletRequest request) {
        String username = WebUtils.getNullIfEmpty(request, "username");
        String verifyName = WebUtils.getNullIfEmpty(request, "verifyName");
        String verifyId = WebUtils.getNullIfEmpty(request, "verifyId");
        if (StringUtils.isEmpty(verifyId) || StringUtils.isEmpty(verifyName)) {
            return "no verifyId";
        } else {
            verifyService.confirmCredential(username, verifyName, verifyId);
            return "ok";
        }
    }

    @RequestMapping(value = "/verify-name")
    @ResponseBody
    public String verifyName(HttpServletRequest request) {
        String verifyName = WebUtils.getNullIfEmpty(request, "verifyName");
        if (StringUtils.isEmpty(verifyName)) {
            return "姓名为空";
        } else {
            List<User> users = verifyService.getUsersByVerifyName(verifyName);
            if (CollectionUtils.isEmpty(users)) {
                return "验证通过，没有相同姓名的证件";
            }
            StringBuilder sb = new StringBuilder("相同姓名证件如下：");
            for (User user : users) {
                sb.append("\n\n").append('【').append(user.getUsername())
                        .append('】').append('\n').append(verifyName)
                        .append(' ').append(user.getVerifyId());
            }
            return sb.toString();
        }
    }

    @RequestMapping(value = "/verify-email")
    public String verifyEmail(HttpServletRequest request,
            Map<String, Object> map) {
        map.put("records",
                verifyService.getVerifyEmailRecords(VerifyEmailStatus.GRAY));
        return "admin/verify-email";
    }

    @RequestMapping(value = "/verify-email/submit")
    @ResponseBody
    public void verifyEmailSubmit(HttpServletRequest request) {
        VerifyEmailType type = VerifyEmailType.values()[WebUtils.getInt(
                request, "type", null)];
        verifyService.setEmailType(request.getParameter("email"), type);
    }
    
    @RequestMapping(value = "/search")
    public String searchUsers(HttpServletRequest request, Map<String, Object> map) {
        String searchKey = WebUtils.getNullIfEmpty(request, "searchKey");
        List<User> users = userService.search(searchKey);
        map.put("users", users);
        return "admin/user";
    }

}
