package room107.web.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.service.user.IContactService;
import room107.util.EmailUtils;
import room107.util.EmailUtils.EmailAccount;
import room107.util.MsgUtils;
import room107.util.UserBehaviorLog;
import room107.util.WebUtils;

/**
 * @author WangXiao
 */
@Controller
public class AdminContactController {

    @Autowired
    private IContactService contactService;

    @RequestMapping("/admin/contact")
    public String contact(HttpServletRequest request, Map<String, Object> map) {
        map.put("contacts", contactService.getAll());
        return "admin/contact";
    }

    @RequestMapping("/admin/sms")
    public String sms(HttpServletRequest request, Map<String, Object> map) {
        return "admin/sms";
    }

    @RequestMapping("/admin/sms/submit")
    @ResponseBody
    public String smsSubmit(HttpServletRequest request, Map<String, Object> map) {
        String telephone = WebUtils.getNullIfEmpty(request, "tel");
        String message = WebUtils.getNullIfEmpty(request, "message");
        if (telephone == null || message == null)
            return "电话和短信不允许为空";
        String result = MsgUtils.sendSms(message, telephone);
        UserBehaviorLog.AUTH.info("send a message, telephone = " + telephone
                + ", message = " + message + ", result = " + result);
        if (result.contains("OK"))
            return "发送成功";
        return "发送失败";
    }

    @RequestMapping("/admin/email")
    @ResponseBody
    public String smsEmail(HttpServletRequest request) {
        String email = WebUtils.getNullIfEmpty(request, "email");
        String title = WebUtils.getNullIfEmpty(request, "title");
        String content = WebUtils.getNullIfEmpty(request, "message");
        if (email == null || content == null || title == null) {
            return "email和内容不能为空";
        } else {
            EmailUtils.sendMail(email, title, content, HtmlEmail.class,
                    EmailAccount.SUPERVISION);
            return "发送成功";
        }
    }
}
