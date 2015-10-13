package room107.service.api.weixin.response;

import org.junit.Test;

public class TextJsonResponseTest {

    @Test
    public void testGetText() {
        System.out.println(new TextJsonResponse("to", "content"));
    }

}
