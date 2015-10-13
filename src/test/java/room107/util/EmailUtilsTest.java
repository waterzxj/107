package room107.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author WangXiao
 */
public class EmailUtilsTest {

    @Test
    public void testIsEduEmail() {
        assertTrue(EmailUtils.isEduEmail("asdas@asda.edu"));
        assertTrue(EmailUtils.isEduEmail("asd.edu@asd.edu.cn"));
        assertTrue(EmailUtils.isEduEmail("x@x.ac.uk"));
        assertTrue(EmailUtils.isEduEmail("x@x.x.x.x.edu.au"));
        assertTrue(EmailUtils.isEduEmail("x@xxxxx.edu.xx.edu.sg"));
        assertFalse(EmailUtils.isEduEmail("x-edu.zxc@edu.xx.cn"));
        assertFalse(EmailUtils.isEduEmail("xx.edu.cn@edu.cn.ssd"));
        assertFalse(EmailUtils.isEduEmail("ac.edu@asd.ac"));
        assertFalse(EmailUtils.isEduEmail("edu.cn@edu.163.com"));
        assertFalse(EmailUtils.isEduEmail("ead.jp@xx.edu.jp.cn"));
    }

}
