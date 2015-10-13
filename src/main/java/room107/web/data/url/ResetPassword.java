package room107.web.data.url;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author WangXiao
 */
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class ResetPassword {

    @NonNull
    private String username;
    
    @NonNull
    private long timestamp;

}
