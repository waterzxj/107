package room107.web.house;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import room107.util.QiniuUploadUtils;
import room107.util.StringUtils;

/**
 * Abstract of a card to display as search result.
 * 
 * @author WangXiao
 */
@RequiredArgsConstructor
@Data
public class Item {
    
    public static final String DEFAULT_COVER = QiniuUploadUtils.getUrl(
            QiniuUploadUtils.HOUSE_BUCKET, "default_search_picture_20150522.jpg");

    public enum Channel {
        DEFAULT
    }

    /**
     * Currently only rent.
     */
    @NonNull
    private int channel;

    private long id;

    /**
     * Detailed type defined in {@link #channel}, e.g. rent type.
     */
    private int type;

    private Integer price;

    private String district, position;

    private String name, description;

    private String cover;
    
    private String faviconUrl;
    
    private Long timestamp;

    /**
     * Location.
     */
    private Double x, y;

    public void setDescription(String desc) {
        if (StringUtils.isNotEmpty(desc)) {
            description = StringUtils.escpaeHtml(StringUtils.abbreviate(desc,
                    107));
        }
    }

}
