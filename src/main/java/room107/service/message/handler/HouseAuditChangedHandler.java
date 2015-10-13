package room107.service.message.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.datamodel.AuditStatus;
import room107.datamodel.User;
import room107.datamodel.VerifyStatus;
import room107.service.api.weixin.WeiXinService;
import room107.service.message.Message;
import room107.service.message.MessageHandler;
import room107.service.message.type.HouseAuditChanged;
import room107.service.user.IUserService;

/**
 * Notify subscribers when audit passed.
 * 
 * @author WangXiao
 */
@Component
public class HouseAuditChangedHandler extends MessageHandler {

    @Autowired
    private WeiXinService weiXinService;
    
    @Autowired
    private IUserService userService;

    @Override
    protected Class<? extends Message> getSubscription() {
        return HouseAuditChanged.class;
    }

    @Override
    protected void handle(Message message) throws Exception {
        HouseAuditChanged houseAuditChanged = (HouseAuditChanged) message;
        if (houseAuditChanged.getAuditStatus() == AuditStatus.ACCEPTED
                .ordinal()) {
            User user = userService.getUser(houseAuditChanged.getUsername());
            if (user.getVerifyStatus() == VerifyStatus.UNVERIFIED.ordinal()) {
                user.setVerifyStatus(VerifyStatus.VERIFIED_HOUSE.ordinal());
                userService.updateUser(user);
            }   
            weiXinService.notifySubscribers(houseAuditChanged.getHouseId());
        }
    }

}
