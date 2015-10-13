package room107.service.message.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.datamodel.RentStatus;
import room107.datamodel.Room;
import room107.datamodel.RoomType;
import room107.service.house.IHouseService;
import room107.service.message.Message;
import room107.service.message.MessageHandler;
import room107.service.message.type.RoomStatusChanged;

/**
 * Keep house status consistent with room's.
 * 
 * @author WangXiao
 */
@Component
public class RoomStatusSynchronizer extends MessageHandler {

    @Autowired
    private IHouseService houseService;

    @Override
    protected void handle(Message message) {
        RoomStatusChanged m = (RoomStatusChanged) message;
        List<Room> rooms = houseService.getRooms(m.getHouseId());
        RentStatus houseStatus = RentStatus.CLOSED;
        for (Room room : rooms) {
            if (RoomType.isBedroom(room.getType())) {
                int status = room.getStatus();
                if (room.getId() == m.getRoomId())
                    status = m.getNewStatus();
                if (status == RentStatus.OPEN.ordinal()) {
                    houseStatus = RentStatus.OPEN;
                    break;
                }
            }
        }
        houseService.updateHouseStatus(m.getHouseId(), houseStatus.ordinal());
    }

    @Override
    protected int getRetryMaxCount() {
        return 0; // no retry
    }

    @Override
    protected Class<? extends Message> getSubscription() {
        return RoomStatusChanged.class;
    }

}
