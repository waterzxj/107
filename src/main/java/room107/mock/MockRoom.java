package room107.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

import room107.datamodel.Room;
import room107.datamodel.RoomType;
import room107.util.HouseUtils;

/**
 * @author WangXiao
 */
public class MockRoom extends Room {

    private static final long serialVersionUID = -8113602105159678698L;

    public MockRoom() {
        this(0, 1);
    }

    public MockRoom(long id) {
        this(0, RandomUtils.nextInt(RoomType.values().length - 1) + 1);
    }

    public MockRoom(long id, float rank) {
        this(0, RandomUtils.nextInt(RoomType.values().length - 1) + 1, id);
    }

    public MockRoom(long id, int type, float rank) {
        setId((long) RandomUtils.nextInt(30));
        setRank(rank);
        setStatus(RandomUtils.nextInt(2));
        setType(type);
        setName(HouseUtils.getRoomType(type) + id);
        setPrice(1000 + 100 * RandomUtils.nextInt(10));
        setArea(32);
        setOrientation("北");
        List<String> imageIds = new ArrayList<String>();
        for (int j = 0; j < 5; j++) {
            imageIds.add(j + ".jpg");
        }
        Collections.shuffle(imageIds);
        setImageIds(StringUtils.join(imageIds, '|'));
    }

}
