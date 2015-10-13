package room107.service.job;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;

import room107.tool.AutowiredTest;

public class WeiXinResubscribeJobTest extends AutowiredTest {

    @Autowired
    @Qualifier(value = "weiXinResubscribeJob")
    private WeiXinResubscribeJob weiXinResubscribeJob;

    @Test
    @Rollback(value = false)
    public void testRun() {
        weiXinResubscribeJob.run();
    }

}
