package misc;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * 
 * @author WangXiao
 * 
 */
public class Log4jTest extends TestCase {

    @Test
    public void test() throws Exception {
        Log log = LogFactory.getLog(Log4jTest.class);
        log.info("Log4jTest");
    }

}
