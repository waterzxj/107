package room107.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import room107.service.user.IVerifyService.ConfirmData;

;

/**
 * @author WangXiao
 */
public class VerifyServiceImplTest {

    @Test
    public void testVerifyEmail() {
        ConfirmData data = new ConfirmData("xxx", "xx@xx.com");
        System.out.println(data);
        System.out.println(data.encrypt());
        System.out.println(data.getConfirmUrl());
        // parse
        ConfirmData data2 = ConfirmData.decrypt(data.encrypt());
        System.out.println(data2);
        System.out.println(data2.encrypt());
        assertEquals(data, data2);
    }

}
