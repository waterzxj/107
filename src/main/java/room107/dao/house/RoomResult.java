package room107.dao.house;

import lombok.NonNull;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

import room107.Constants;
import room107.datamodel.House;
import room107.datamodel.RentType;
import room107.datamodel.Room;
import room107.datamodel.RoomType;
import room107.util.StringUtils;
import room107.web.house.Item;

/**
 * @author WangXiao
 */
public class RoomResult extends HouseResult {

    @NonNull
    protected Room room;

    public RoomResult(House house, Room room) {
        super(house);
        Validate.notNull(room);
        this.room = room;
    }

    @Override
    public long getId() {
        return room.getId();
    }

    @Override
    public String getName() {
        return RoomType.get(room.getType());
    }

    @Override
    public Integer getPrice() {
        return room.getPrice();
    }

    @Override
    public Item toItem() {
        Item result = super.toItem();
        // cover
        String cover = null;
        String[] ids = StringUtils.split(room.getImageIds(),
                Constants.MULTI_VALUE_SEPARATOR);
        if (!ArrayUtils.isEmpty(ids)) {
            cover = ids[0];
        }
        result.setCover(cover);
        if (result.getTimestamp() < room.getModifiedTime().getTime()) {
            result.setTimestamp(room.getModifiedTime().getTime());
        }
        return result;
    }

    @Override
    public RentType getRentType() {
        return RentType.BY_ROOM;
    }

    public float getRank() {
        return room.getRank();
    }

}
