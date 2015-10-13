package room107.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author WangXiao
 */
@RequestMapping("/doc")
@Controller
public class DocumentController {

    @RequestMapping("/{docName}")
    public String forHouse(HttpServletRequest request,
            @PathVariable String docName) {
        return "doc/" + docName;
    }

}
