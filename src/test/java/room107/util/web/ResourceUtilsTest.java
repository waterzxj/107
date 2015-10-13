package room107.util.web;

import org.junit.Test;

/**
 * @author WangXiao
 */
public class ResourceUtilsTest {

    @Test
    public void testGetStaticPath() {
        System.out.println(ResourceUtils
                .getStaticPath("static/image/mobile/wx-logo.png"));
    }

}
