package room107.util.web;

/**
 * @author WangXiao
 */
public class ResourceUtils {

    static final String STATIC_107 = "http://107room.com/";

    static final String STATIC_CDN = "http://s107.qiniudn.com/";

    public static String getStaticPath(String name) {
        return STATIC_CDN + name;
    }

}
