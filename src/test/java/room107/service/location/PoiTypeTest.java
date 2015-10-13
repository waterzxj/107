package room107.service.location;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import room107.datamodel.PoiType;

public class PoiTypeTest {

    @Test
    public void testMatch() {
        /*
         * subway
         */
        assertFalse(PoiType.SUBWAY.matches(null));
        assertFalse(PoiType.SUBWAY.matches(""));
        assertTrue(PoiType.SUBWAY.matches("地铁1号线"));
        assertTrue(PoiType.SUBWAY.matches("地铁10号线"));
        assertTrue(PoiType.SUBWAY.matches("地铁4号线大兴线"));
        assertTrue(PoiType.SUBWAY.matches("八通线"));
        assertTrue(PoiType.SUBWAY.matches("昌平线"));
        assertTrue(PoiType.SUBWAY.matches("房山线"));
        assertTrue(PoiType.SUBWAY.matches("亦庄线"));
        assertTrue(PoiType.SUBWAY.matches("机场线"));
        assertFalse(PoiType.SUBWAY.matches("地铁XXXXXXX线"));
        assertFalse(PoiType.SUBWAY.matches("地1号线"));
        assertFalse(PoiType.SUBWAY.matches("1号线"));
        assertFalse(PoiType.SUBWAY.matches("355路"));
        /*
         * bus
         */
        assertFalse(PoiType.BUS.matches(null));
        assertFalse(PoiType.BUS.matches(""));
        assertFalse(PoiType.BUS.matches("地铁1号线"));
        assertFalse(PoiType.BUS.matches("八通线"));
        assertTrue(PoiType.BUS.matches("1路"));
        assertTrue(PoiType.BUS.matches("11路"));
        assertTrue(PoiType.BUS.matches("111路"));
        assertTrue(PoiType.BUS.matches("207路夜班车"));
        assertTrue(PoiType.BUS.matches("867路旅游专线"));
        assertTrue(PoiType.BUS.matches("915路快"));
        assertTrue(PoiType.BUS.matches("915区"));
        assertTrue(PoiType.BUS.matches("300快外"));
        assertTrue(PoiType.BUS.matches("300快内"));
    }

}
