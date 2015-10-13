package room107.service.api.weixin.response;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import room107.util.JsonUtils;

/**
 * @author WangXiao
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class AbstractJsonResponse {

    @NonNull
    private String msgtype;

    @NonNull
    private String touser;

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

}
