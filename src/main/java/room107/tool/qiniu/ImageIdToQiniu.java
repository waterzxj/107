package room107.tool.qiniu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import room107.Constants;
import room107.dao.house.IHouseDao;
import room107.datamodel.House;
import room107.datamodel.Room;
import room107.tool.AutowiredTest;

/**
 * @author WangXiao
 */
public class ImageIdToQiniu extends AutowiredTest {

    @Autowired
    private IHouseDao houseDao;

    @Test
    @Rollback(value = false)
    public void main() throws IOException {
        /*
         * house
         */
        List<House> houses = houseDao.getAll(House.class);
        for (House house : houses) {
            if (house.getImageId() != null) {
                String newId = rename(house.getImageId());
                if (!StringUtils.equals(newId, house.getImageId())) {
                    house.setImageId(newId);
                    houseDao.updateHouse(house, false);
                    System.out
                            .println("house rename to: " + house.getImageId());
                }
            }
        }
        /*
         * room
         */
        List<Room> rooms = houseDao.getAll(Room.class);
        for (Room room : rooms) {
            List<String> newIds = new ArrayList<String>();
            if (StringUtils.isNotEmpty(room.getImageIds())) {
                for (String id : StringUtils.split(room.getImageIds(),
                        Constants.MULTI_VALUE_SEPARATOR)) {
                    String newId = rename(id);
                    newIds.add(newId);
                }
                String newId = StringUtils.join(newIds,
                        Constants.MULTI_VALUE_SEPARATOR);
                room.setImageIds(newId);
                houseDao.updateRoom(room);
                System.out.println("room rename: " + newId);
            }
        }
    }

    private String rename(String imageId) throws IOException {
        if (imageId == null || imageId.contains("://")) {
            return imageId;
        }
        return "http://h107.qiniudn.com/" + imageId;
    }

}
