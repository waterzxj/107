package room107.service.message.type;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import room107.datamodel.House;
import room107.service.message.Message;
import room107.util.StringUtils;

/**
 * When house position changed (created or updated).
 * 
 * @author WangXiao
 */
@RequiredArgsConstructor
@Data
public class HousePositionChanged implements Message {

    @NonNull
    private House house;

    @Override
    public String toString() {
        return StringUtils
                .toString(house.getId(), house.getCity(), house.getPosition(),
                        house.getLocationX(), house.getLocationY());
    }

}
