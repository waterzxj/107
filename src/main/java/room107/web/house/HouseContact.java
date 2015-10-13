package room107.web.house;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public class HouseContact {
    
    @NonNull
    public final Integer authStatus;
    
    public String telephone;
    
    public String qq;
    
    public String wechat;

}
