package room107.service.api.weixin.admin;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import room107.dao.IAdminDao;
import room107.dao.IContactDao;
import room107.dao.IUserDao;
import room107.dao.IWxUserDao;
import room107.dao.house.IExternalHouseDao;
import room107.dao.house.IHouseDao;
import room107.datamodel.Admin;
import room107.datamodel.Contact;
import room107.datamodel.House;
import room107.datamodel.HouseSource;
import room107.datamodel.RentStatus;
import room107.datamodel.RentType;
import room107.datamodel.Room;
import room107.datamodel.UserStatus;
import room107.service.api.weixin.Renderer;
import room107.service.api.weixin.request.TextRequest;
import room107.service.house.IHouseService;
import room107.util.StringUtils;

/**
 * @author WangXiao
 */
@Service
@CommonsLog
public class WeiXinAdminService {

    @Autowired
    private IAdminDao adminDao;

    @Setter
    @Autowired
    private IUserDao userDao;
    
    @Setter
    @Autowired
    private IWxUserDao wxUserDao;
    
    @Setter
    @Autowired
    private IHouseDao houseDao;
    
    @Autowired
    private IContactDao contactDao;
    
    @Autowired
    private IHouseService houseService;

    @Autowired
    @Setter
    private IExternalHouseDao externalHouseDao;

    private static final String ADMIN_PREFIX = "@";

    private static final String SUCCESS = "DONE";

    /**
     * 
     * @param request
     *            like '@command xxx'
     * @return non-null when handled
     */
    public String handle(TextRequest request) {
        if (!isAdminRequest(request)) {
            return null;
        }
        // set default '@s'
        if (ADMIN_PREFIX.equals(request.getContent())) {
            request.setContent(ADMIN_PREFIX + "s");
        }
        try {
            String content = request.getContent().substring(
                    ADMIN_PREFIX.length());
            String[] ss = StringUtils.split(content);
            Validate.isTrue(ss.length >= 1, "Invalid command length: "
                    + ss.length);
            return handle(ss[0],
                    (String[]) ArrayUtils.subarray(ss, 1, ss.length));
        } catch (Exception e) {
            log.error("Handle admin request failed: ", e);
            return null;
        }
    }

    public boolean isAdmin(String openId) {
        if (StringUtils.isEmpty(openId)) {
            return false;
        }
        Admin admin = adminDao.get(Admin.class, openId);
        return admin != null && admin.getStatus() == 0;
    }

    /**
     * @return true when the content is non-null and a command
     */
    private boolean isAdminRequest(TextRequest request) {
        if (request == null) {
            return false;
        }
        String content = request.getContent();
        boolean result = content == null ? false : content
                .startsWith(ADMIN_PREFIX);
        if (result) { // check authority
            if (isAdmin(request.getFrom())) {
                return true;
            } else {
                log.warn("Invalid admin request: " + request);
            }
        }
        return false;
    }

    private String handle(String command, String... values) {
        try {
            if (command.equals("s")) {
                return stat();
            }
            if (command.equals("contact")) {
                return contact(values);
            }
            if (command.equals("close")) {
                return close(values);
            }
        } catch (Exception e) {
            log.error("Handle command failed: command=" + command + ", values="
                    + ArrayUtils.toString(values), e);
        }
        return help();
    }

    String help() {
        String[][] samples = { { "帮助", "@h" }, { "查看统计", "@s" },
                { "查看留言", "@contact_{留言数目，不填默认为5}" },
                { "关闭房间", "@close_room_{房间ID}  |  @close_house_{房子ID}" } };
        StringBuilder builder = new StringBuilder();
        builder.append("'_'表示空格，'{xxx}'表示变量名").append(Renderer.NEXT_LINE);
        for (int i = 0; i < samples.length; i++) {
            builder.append(samples[i][0]).append("：").append(samples[i][1])
                    .append(Renderer.NEXT_LINE);
        }
        return builder.toString();
    }

    String stat() {
        StringBuilder builder = new StringBuilder();
        /*
         * total count
         */
        builder.append("【Total 用户统计】 ").append(Renderer.NEXT_LINE);
        int webUser = userDao.getAllCount(null);
        builder.append("网站总用户：").append(webUser).append(Renderer.NEXT_LINE);
        builder.append("网站注册用户：").append(userDao.getRegisterCount(null))
                .append(Renderer.NEXT_LINE);
        builder.append("网站匿名用户：").append(userDao.getAnonymousCount(null))
                .append(Renderer.NEXT_LINE);
        builder.append("网站认证用户：").append(userDao.getAuthenticationCount(null))
                .append(Renderer.NEXT_LINE);
        builder.append("微信用户：").append(wxUserDao.getWxCount(null))
                .append(Renderer.NEXT_LINE);
        int wxOnlyUser = wxUserDao.getWxOnlyCount(null);
        builder.append("总用户：").append(wxOnlyUser + webUser)
                .append(Renderer.NEXT_LINE);
        builder.append(Renderer.NEXT_LINE);
        builder.append("【ToTal  房源统计】").append(Renderer.NEXT_LINE);
        builder.append("总房源：").append(houseDao.getAllHouseCount(null))
                .append(Renderer.NEXT_LINE);
        builder.append("总房间(卧室)： ").append(houseDao.getAllBedRoomCount())
                .append(Renderer.NEXT_LINE);
        builder.append("当前有效房源： ")
                .append(houseDao.getEffectiveHouseCount(null))
                .append(Renderer.NEXT_LINE);
        builder.append(Renderer.NEXT_LINE);
        builder.append("【当日用户统计：】 ").append(Renderer.NEXT_LINE);
        Date date = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        webUser = userDao.getAllCount(date);
        builder.append("网站总用户： ").append(webUser).append(Renderer.NEXT_LINE);
        builder.append("网站注册用户：").append(userDao.getRegisterCount(date))
                .append(Renderer.NEXT_LINE);
        builder.append("网站匿名用户：").append(userDao.getAnonymousCount(date))
                .append(Renderer.NEXT_LINE);
        builder.append("网站认证用户: ").append(userDao.getAuthenticationCount(date))
                .append(Renderer.NEXT_LINE);
        builder.append("微信用户： ").append(wxUserDao.getWxCount(date))
                .append(Renderer.NEXT_LINE);
        wxOnlyUser = wxUserDao.getWxOnlyCount(date);
        builder.append("总用户： ").append(webUser + wxOnlyUser)
                .append(Renderer.NEXT_LINE);
        builder.append(Renderer.NEXT_LINE);
        builder.append("【当日房源统计：】").append(Renderer.NEXT_LINE);
        builder.append("有效房源: ").append(houseDao.getEffectiveHouseCount(date))
                .append(Renderer.NEXT_LINE);
        builder.append("自发房源： ")
                .append(houseDao.getHouseCountBySource(date,UserStatus.NORMAL))
                .append(Renderer.NEXT_LINE);
        builder.append("自发有效房源： ")
                .append(houseDao.getEffectiveHouseCountBySource(date,UserStatus.NORMAL))
                .append(Renderer.NEXT_LINE);
        builder.append("外部房源： ")
                .append(externalHouseDao.getExternalHouseCountBySource(date, null))
                .append(Renderer.NEXT_LINE);
        builder.append("外部录入房源： ")
                .append(externalHouseDao.getExternalEnteringHouseCountBySource(date, null))
                .append(Renderer.NEXT_LINE);
        builder.append("外部有效房源： ")
                .append(externalHouseDao.getExternalEffectiveHouseCountBySource(date, null))
                .append(Renderer.NEXT_LINE);
        builder.append(Renderer.NEXT_LINE);
        builder.append("【当日分渠道房源统计】").append(Renderer.NEXT_LINE);
        builder.append("58房源：")
                .append(externalHouseDao.getExternalHouseCountBySource(date,
                        HouseSource.LOCAL58)).append(Renderer.NEXT_LINE);
        builder.append("58录入房源: ")
                .append(externalHouseDao.getExternalEnteringHouseCountBySource(date,
                        HouseSource.LOCAL58)).append(Renderer.NEXT_LINE);
        builder.append("58有效房源: ")
                .append(externalHouseDao.getExternalEffectiveHouseCountBySource(date,
                        HouseSource.LOCAL58)).append(Renderer.NEXT_LINE);
        builder.append(Renderer.NEXT_LINE);
        builder.append("豆瓣房源： ")
                .append(externalHouseDao.getExternalHouseCountBySource(date,
                        HouseSource.DOUBAN)).append(Renderer.NEXT_LINE);
        builder.append("豆瓣录入房源： ")
                .append(externalHouseDao.getExternalEnteringHouseCountBySource(date,
                        HouseSource.DOUBAN)).append(Renderer.NEXT_LINE);
        builder.append("豆瓣有效房源： ")
                .append(externalHouseDao.getExternalEffectiveHouseCountBySource(date,
                        HouseSource.DOUBAN)).append(Renderer.NEXT_LINE);
        builder.append(Renderer.NEXT_LINE);
        builder.append("水木房源： ")
                .append(externalHouseDao.getExternalHouseCountBySource(date,
                        HouseSource.SHUIMU)).append(Renderer.NEXT_LINE);
        builder.append("水木录入房源： ")
                .append(externalHouseDao.getExternalEnteringHouseCountBySource(date,
                        HouseSource.SHUIMU)).append(Renderer.NEXT_LINE);
        builder.append("水木有效房源： ")
                .append(externalHouseDao.getExternalEffectiveHouseCountBySource(date,
                        HouseSource.SHUIMU)).append(Renderer.NEXT_LINE);
        return builder.toString();
    }

    String contact(String... values) {
        boolean isView = false;
        if (values.length == 0 || NumberUtils.isDigits(values[0])) {
            isView = true;
        }
        if (isView) {
            int maxCount = 5; // default
            try {
                maxCount = Integer.parseInt(values[0]);
            } catch (Exception e) {
            }
            StringBuilder builder = new StringBuilder();
            List<Contact> contacts = contactDao.getLatest(maxCount);
            for (Contact c : contacts) {
                builder.append(c.getId()).append(":")
                        .append(Renderer.NEXT_LINE);
                builder.append(c.getCreatedTime()).append(Renderer.NEXT_LINE);
                builder.append(
                        StringUtils.join(
                                new String[] { c.getUsername(),
                                        c.getContact1(), c.getContact2() }, " "))
                        .append(Renderer.NEXT_LINE);
                builder.append(c.getMessage()).append(Renderer.NEXT_LINE)
                        .append(Renderer.NEXT_LINE);
            }
            return builder.toString();
        } else {
            throw new IllegalArgumentException("Unknown contact operation: "
                    + values[0]);
        }
    }
    
    String close(String... values) {
        try {
            if (values.length == 2) {
                long id = Long.valueOf(values[1]);
                if ("house".equals(values[0])) {
                    House house = houseDao.getHouse(id);
                    if (house != null) {
                        if (house.getRentType() == RentType.BY_HOUSE.ordinal()) {
                            houseService.updateHouseStatus(id, RentStatus.CLOSED.ordinal());
                        } else {
                            houseService.closeAll(house.getUsername(), id);
                        }
                        return SUCCESS;
                    } else {
                        return "没有id为" + id + "的房子";
                    }
                } else if ("room".equals(values[0])) {
                    Room room = houseDao.getRoom(id);
                    if (room != null) {
                        House house = houseDao.getHouse(room.getHouseId());
                        if (house.getRentType() == RentType.BY_ROOM.ordinal()) {
                            houseService.updateRoomStatus(id, RentStatus.CLOSED.ordinal());
                            return SUCCESS;
                        } else {
                            return "整租房不允许关闭一间房";
                        }
                    } else {
                        return "没有id为" + id + "的房间";
                    }
                }
            }
        } catch(Exception e) {
            log.error(e, e);
        }
        return "参数错误，请输入@h查看帮助";
    }

    public static void main(String[] args) {
        @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "WeiXinAdminService.xml");
        WeiXinAdminService tool = (WeiXinAdminService) context
                .getBean("wexinAdminService");
        System.out.println(tool.stat());
    }

}
