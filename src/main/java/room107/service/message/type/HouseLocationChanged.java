package room107.service.message.type;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import room107.datamodel.Location;
import room107.service.message.Message;

/**
 * @author WangXiao
 */
@RequiredArgsConstructor
@Data
public class HouseLocationChanged implements Message {

    @NonNull
    private long houseId;

    @NonNull
    private Location location;

}
