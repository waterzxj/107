package room107.service.house.search.line;

import static org.junit.Assert.*;

import org.junit.Test;

public class LineQueryTest {

    @Test
    public void testNormalize() {
        assertNull(LineQuery.normalize(null));
        assertNull(LineQuery.normalize(""));
        assertNull(LineQuery.normalize("test"));
        assertNull(LineQuery.normalize("地铁"));
        assertNull(LineQuery.normalize("地铁1线"));
        assertNull(LineQuery.normalize("asd路"));
        assertNull(LineQuery.normalize("三路"));
        assertNull(LineQuery.normalize("asd号线"));
        assertNull(LineQuery.normalize("地铁5号"));
        assertNull(LineQuery.normalize("asd线"));
        assertNull(LineQuery.normalize("地铁一百号线"));
        assertNull(LineQuery.normalize("地铁5a号线"));
        assertNull(LineQuery.normalize("地铁十3号线"));
        assertNull(LineQuery.normalize("地铁十十一3号线"));
        assertEquals("355路", LineQuery.normalize("355路"));
        assertEquals("355路", LineQuery.normalize("355"));
        assertEquals("地铁10号线", LineQuery.normalize("10号线"));
        assertEquals("地铁10号线", LineQuery.normalize("地铁10号线"));
        assertEquals("地铁10号线", LineQuery.normalize("十号线"));
        assertEquals("地铁1号线", LineQuery.normalize("一号线"));
        assertEquals("地铁13号线", LineQuery.normalize("十三号线"));
        assertEquals("地铁13号线", LineQuery.normalize("地铁十三号线"));
        assertEquals("昌平线", LineQuery.normalize("昌平线"));
    }

}
