package room107.service.api.weixin.admin;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import room107.tool.AutowiredTest;

public class WeiXinAdminServiceTest extends AutowiredTest {

    @Autowired
    private WeiXinAdminService service;

    @Test
    public void testHandle() {
        System.out.println(service.help());
        System.out.println(service.stat());
        System.out.println(service.contact());
        System.out.println(service.contact("10"));
    }

}
