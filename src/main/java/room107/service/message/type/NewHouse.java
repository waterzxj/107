package room107.service.message.type;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import room107.datamodel.House;
import room107.service.message.Message;

/**
 * @author WangXiao
 */
@Data
@RequiredArgsConstructor
public class NewHouse implements Message {

    @NonNull
    private House house;

}
