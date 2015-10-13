/**
 * 
 */
package room107.service.job;

import org.springframework.stereotype.Component;

import lombok.extern.apachecommons.CommonsLog;

/**
 * @author yanghao
 */
@CommonsLog
@Component
public class FullGcJob extends SimpleJob {

    @Override
    public void run() {
        log.info("call system gc to release memory.");
        System.gc();
    }

}
