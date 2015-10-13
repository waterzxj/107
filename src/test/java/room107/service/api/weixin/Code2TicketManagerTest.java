package room107.service.api.weixin;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import room107.tool.AutowiredTest;

public class Code2TicketManagerTest extends AutowiredTest {

    @Autowired
    private Code2TicketManager manager;

    @Test
    public void testGetTicket() {
        manager.setCacheSize(3);
        for (int i = 0; i < 3; i++) {
            print(i);
            print(i);
            print(i);
        }
        print(3);
        print(2);
        print(1);
        print(0);
    }

    private void print(int i) {
        long t = System.currentTimeMillis();
        String ticket = manager.getTicketBlocked(i);
        System.out.println(i + ": " + (System.currentTimeMillis() - t) + ": "
                + ticket);
    }

}
