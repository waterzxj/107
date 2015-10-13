package room107.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author WangXiao
 */
public class XmlUtils {

    public static Element getRootElement(HttpServletRequest request)
            throws Exception {
        if (request == null) {
            return null;
        }
        return getRootElement(request.getInputStream());
    }

    public static Element getRootElement(String s) throws Exception {
        if (s == null) {
            return null;
        }
        return getRootElement(new ByteArrayInputStream(s.getBytes()));
    }

    private static Element getRootElement(InputStream in) throws Exception {
        SAXReader reader = new SAXReader();
        try {
            return reader.read(in).getRootElement();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
