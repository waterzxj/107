package room107.web.admin;

import java.util.Date;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.dao.IDoubanPosterDao;
import room107.datamodel.DoubanPoster;
import room107.datamodel.web.JsonResponse;
import room107.service.admin.IAdminService;
import room107.util.JsonUtils;
import room107.util.StringUtils;

/**
 * @author WangXiao
 */
@CommonsLog
@Controller
public class DoubanController {

    public static final String POSTER_PATH = "/douban-poster";

    @Autowired
    private IAdminService adminService;

    @Autowired
    private IDoubanPosterDao doubanPosterDao;

    @RequestMapping(value = POSTER_PATH)
    @ResponseBody
    public String getPosters() {
        return JsonUtils.toJson(adminService.getDoubanPosters());
    }

    @RequestMapping(value = "/admin/upper")
    public String index(Map<String, Object> map) {
        map.put("posters", adminService.getDoubanPosters());
        return "admin/upper";
    }

    @Transactional
    @RequestMapping(value = "/admin/upper/submit")
    @ResponseBody
    public String submit(@RequestParam long id, @RequestParam String name,
            @RequestParam String url1, @RequestParam String url2,
            @RequestParam String cookie) {
        try {
            Object o = JsonUtils.fromJson(cookie, Map.class);
            cookie = JsonUtils.toJson(o);
        } catch (Exception e) {
            return JsonResponse.error("Invalid cookie format");
        }

        DoubanPoster poster = doubanPosterDao.get(DoubanPoster.class, id);
        poster.setName(name);
        poster.setUrl1(url1);
        poster.setUrl2(url2);
        poster.setCookie(cookie);
        poster.setModifiedTime(new Date());
        doubanPosterDao.update(poster);
        log.info("Update douban poster: id=" + id + ", name=" + name
                + ", url1=" + url1 + ", url2=" + url2 + ", cookie=" + cookie);
        return JsonResponse.OK;
    }

    @Transactional
    @RequestMapping(value = "/admin/upper/delete")
    @ResponseBody
    public String delete(@RequestParam long id) {
        doubanPosterDao.delete(DoubanPoster.class, id);
        log.info("Delete douban poster: id=" + id);
        return JsonResponse.OK;
    }

}
