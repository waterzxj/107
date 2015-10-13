package room107.web;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.datamodel.Contact;
import room107.datamodel.ContactType;
import room107.datamodel.LogType;
import room107.datamodel.User;
import room107.service.user.IContactService;
import room107.util.JsonUtils;
import room107.util.UserStatLog;
import room107.util.UserUtils;
import room107.util.WebUtils;

/**
 * @author WangXiao
 */
@CommonsLog
@Controller
public class ContactController {

    @Autowired
    private IContactService contactService;
    
    @RequestMapping("/contact")
    public String contact(HttpServletRequest request, Map<String, Object> map) {
        return "contact";
    }
    
    @RequestMapping({"/aboutus", "/about"})
    public String abouts(HttpServletRequest request, Map<String, Object> map) {
        return "aboutus";
    }

    @RequestMapping("/contact/{type}")
    public String contactWithType(HttpServletRequest request,
            @PathVariable String type, Map<String, Object> map) {
        int typeValue = ContactType.GENERAL.ordinal();
        try {
            typeValue = ContactType.valueOf(type.toUpperCase()).ordinal();
        } catch (Exception e) {
            log.warn("Invalid type: " + type);
        }
        map.put("type", typeValue);
        return "contact";
    }

    @RequestMapping("/contact/submit")
    @ResponseBody
    public String submitJson(HttpServletRequest request, HttpServletResponse response) {
        Contact contact = new Contact();
        contact.setMessage(request.getParameter("message"));
        contact.setContact1(request.getParameter("contact"));
        contact.setContact2(request.getParameter("contact2"));
        contact.setType(WebUtils.getInt(request, "type",
                ContactType.GENERAL.ordinal()));
        contact.setCreatedTime(new Date());
        // user name
        User user = UserUtils.getUser(request, response);
        if (user != null) {
            contact.setUsername(user.getUsername());
        }
        contactService.submit(contact);
        UserStatLog.log(request, response, LogType.feedback, JsonUtils.toJson(contact));
        return null;
    }

}
