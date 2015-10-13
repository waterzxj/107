package room107.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.datamodel.AuditStatus;
import room107.datamodel.House;
import room107.datamodel.HouseSource;
import room107.datamodel.RentStatus;
import room107.datamodel.RentType;
import room107.datamodel.RoomType;
import room107.service.stat.HouseStatistics;

/**
 * @author WangXiao
 */
@CommonsLog
@Component
public class HouseUtils {

    @Getter
    private static HouseStatistics stat;

    @Autowired
    public void setStat(HouseStatistics stat) {
        HouseUtils.stat = stat;
    }

    private static final String[] CN_DIGITS = { "一", "二", "三", "四", "五", "六",
            "七", "八", "九" };

    /**
     * @return X室X厅X厨X卫
     */
    public static String formatStruct(House house) {
        if (house == null) {
            return null;
        }
        return formatStruct(house.getRoomNumber(), house.getHallNumber(),
                house.getKitchenNumber(), house.getToiletNumber());
    }

    /**
     * @return X室X厅X厨X卫
     */
    public static String formatStruct(Integer roomNum, Integer hallNum,
            Integer kitchenNum, Integer toiletNum) {
        StringBuilder builder = new StringBuilder();
        if (roomNum != null) {
            builder.append(formatDigit(roomNum)).append("室");
        }
        if (hallNum != null) {
            builder.append(formatDigit(hallNum)).append("厅");
        }
        if (kitchenNum != null) {
            builder.append(formatDigit(kitchenNum)).append("厨");
        }
        if (toiletNum != null) {
            builder.append(formatDigit(toiletNum)).append("卫");
        }
        return builder.toString();
    }
    
    public static boolean isValidNumber(Integer num) {
        return (num != null && num > 0);
    }

    /**
     * @return description of the house position, currently only for Beijing
     */
    public static String formatPosition(House house) {
        if (house == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (house.getProvince() != null) {
            builder.append(house.getProvince()).append("市");
        }
        if (house.getCity() != null) {
            builder.append(house.getCity()).append("区");
        }
        if (house.getPosition() != null) {
            builder.append(house.getPosition());
        }
        return builder.toString();
    }

    /**
     * @return facility names
     */
    public static String[] formatFacility(House house) {
        if (house == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String[] s = StringUtils.split(house.getFacilities(), '|');
        return s == null ? ArrayUtils.EMPTY_STRING_ARRAY : s;
    }

    public static String getRoomType(int type) {
        try {
            return RoomType.values()[type].toString();
        } catch (Exception e) {
            log.warn("Unknown room type: type");
            return "";
        }
    }

    /**
     * @return Chinese digit
     */
    private static String formatDigit(int i) {
        if (i > 0 && i < 10) {
            return CN_DIGITS[i - 1];
        }
        return Integer.toString(i);
    }

    /**
     * Format mobile only.
     * 
     * @return 123-4567-8901
     */
    public static String formatTelephone(String telephone) {
        telephone = StringUtils.trimToNull(telephone);
        if (telephone == null) {
            return null;
        }
        // remove (+)86
        if (telephone.startsWith("+")) {
            telephone = telephone.substring(1);
        }
        if (telephone.startsWith("86") && telephone.length() > 11) {
            telephone = telephone.substring(2);
        }
        telephone = StringUtils.remove(telephone, ' ');
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < telephone.length(); i++) {
            char c = telephone.charAt(i);
            if (c == ' ')
                continue;
            if (i == 3 || (i - 3) % 4 == 0) {
                builder.append('-');
            }
            builder.append(c);
        }
        return builder.toString();
    }
    
    public static String formatQQ(String qq) {
        return qq;
    }
    
    public static String formatWechat(String wechat) {
        return wechat;
    }

    public static boolean isValidToPush(House house) {
        return house.getStatus() == RentStatus.OPEN.ordinal()
                && house.getAuditStatus() == AuditStatus.ACCEPTED.ordinal()
                && StringUtils.isNotEmpty(house.getImageId());
        //only push houses that contain images.
    }
    
    public static String formatSource(int source) {
        return HouseSource.values()[source].toString();
    }
    
    public static String formatRentType(int rentType) {
        return RentType.values()[rentType].toString();
    }
    
    public static List<String> formatImages(String imgs) {
        List<String> imgList = new ArrayList<String>();
        if (imgs == null) return imgList;
        for (String s : imgs.split("\\|")) {
            if (!StringUtils.isEmpty(s)) {
                imgList.add(s);
            }
        }
        return imgList;
    }
    
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");
    
    public static String formatDate(Date date) {
        if (date == null) date = new Date();
        return FORMAT.format(date);
    }
    
    public static Date parseDate(String content) {
        if (content == null) return null;
        try {
            return FORMAT.parse(content);
        } catch (ParseException e) {
        }
        return null;
    }
    
    
}
