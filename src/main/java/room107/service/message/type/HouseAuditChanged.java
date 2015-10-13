package room107.service.message.type;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import room107.service.message.Message;

/**
 * @author WangXiao
 */
@RequiredArgsConstructor
@Data
public class HouseAuditChanged implements Message {

    @NonNull
    private long houseId;
    
    @NonNull
    private String username;

    /**
     * New status.
     */
    @NonNull
    private int auditStatus;

}
