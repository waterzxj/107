package room107.service.message.handler;

import org.springframework.stereotype.Component;

import room107.datamodel.House;
import room107.service.message.Message;
import room107.service.message.MessageHandler;
import room107.service.message.type.NewHouse;
import room107.util.EmailUtils;

/**
 * Handle new house.
 * 
 * @author WangXiao
 */
@Component
public class NewHouseHandler extends MessageHandler {

    @Override
    protected Class<? extends Message> getSubscription() {
        return NewHouse.class;
    }

    @Override
    protected void handle(Message message) throws Exception {
        NewHouse newHouse = (NewHouse) message;
        House house = newHouse.getHouse();
        if (!house.getDescription().equals("test")) {
            EmailUtils.sendAdminMail("New house", "houseId=" + house.getId()
                    + ", username=" + house.getUsername());
        }
    }

}
