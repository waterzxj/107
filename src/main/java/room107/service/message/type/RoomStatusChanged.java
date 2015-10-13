package room107.service.message.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import room107.service.message.Message;

/**
 * When room status changed.
 * 
 * @author WangXiao
 */
@Data
@AllArgsConstructor
public class RoomStatusChanged implements Message {

    private long houseId, roomId;

    private int newStatus;

}