package room107.tool.totorol;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import room107.service.totoro.ITotoroService;
import room107.tool.AutowiredTest;

/**
 * @author WangXiao
 */
public class ImportBrokerId extends AutowiredTest {

    @Autowired
    private ITotoroService totoroService;

    @Test
    @Rollback(value = false)
    public void main() throws IOException {
        File file = new File("f");
        LineIterator iterator = FileUtils.lineIterator(file);
        while (iterator.hasNext()) {
            String line = (String) iterator.next();
            line = StringUtils.trimToNull(line);
            if (line == null) {
                continue;
            }
            System.out.println(line);
            totoroService.reportBrokerId("107room", "l27.0.0.1", line);
        }
    }

}
