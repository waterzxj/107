package room107.util;

import org.junit.Test;

public class UserUtilsTest {

    @Test
    public void testEncryptPassword() {
        System.out.println(UserUtils.encryptPassword(""));
        System.out.println(UserUtils.encryptPassword("  \n"));
        System.out.println(UserUtils.encryptPassword("asd asdasd2"));
        System.out.println(UserUtils.encryptPassword("asd asdasd2"));
        System.out.println(UserUtils.encryptPassword("123"));
    }

}
