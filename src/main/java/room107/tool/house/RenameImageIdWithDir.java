package room107.tool.house;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
 * @deprecated after using qiniu
 */
public class RenameImageIdWithDir extends AutowiredTest {

    @Autowired
    private IHouseDao houseDao;

    @Test
    @Rollback(value = false)
    public void main() throws IOException {
        System.out.println("house");
        /*
         * house
         */
        List<House> houses = houseDao.getAll(House.class);
        for (House house : houses) {
            if (house.getImageId() != null) {
                List<Room> rooms = houseDao.getRooms(house.getId());
                for (Room room : rooms) {
                    if (room.getImageIds() != null
                            && room.getImageIds().contains(house.getImageId())) {
                        try {
                            String newId = rename(house.getImageId(),
                                    room.getCreatedTime());
                            house.setImageId(newId);
                            houseDao.updateHouse(house, false);
                            // System.out.println("house rename: "
                            // + house.getImageId() + "->" + newId);
                        } catch (Exception e) {
                            System.err.println("house rename failed: imageId="
                                    + house.getImageId() + ", roomId="
                                    + room.getId() + e);
                            // e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
        /*
         * room
         */
        System.out.println("room");
        List<Room> rooms = houseDao.getAll(Room.class);
        for (Room room : rooms) {
            try {
                List<String> newIds = new ArrayList<String>();
                String oldIds = room.getImageIds();
                if (StringUtils.isNotEmpty(oldIds)) {
                    for (String id : StringUtils.split(oldIds,
                            Constants.MULTI_VALUE_SEPARATOR)) {
                        String newId = rename(id, room.getCreatedTime());
                        newIds.add(newId);
                    }
                    String newId = StringUtils.join(newIds,
                            Constants.MULTI_VALUE_SEPARATOR);
                    room.setImageIds(newId);
                    houseDao.updateRoom(room);
                    // System.out.println("room rename: " + oldIds + "->" +
                    // newId);
                }
            } catch (Exception e) {
                System.err.println("room rename failed: roomId=" + room.getId()
                        + e);
                // e.printStackTrace();
            }
        }
    }

    private String rename(String imageId, Date createdTime) throws IOException {
        if (imageId.contains("/")) {
            return imageId;
        }
        String newId = new SimpleDateFormat("yyyyMMdd").format(createdTime)
                + '/' + imageId;
        /*
         * move
         */
        File from = new File(Constants.PATH_HOUSE_PHOTO_DIR, imageId);
        File to = new File(Constants.PATH_HOUSE_PHOTO_DIR, newId);
        FileUtils.copyFile(from, to);
        return newId;
    }

}
