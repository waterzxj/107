package room107.service.message.handler;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import room107.datamodel.House;
import room107.datamodel.Location;
import room107.service.house.IHouseService;
import room107.service.location.ILocator;
import room107.service.message.Message;
import room107.service.message.MessageHandler;
import room107.service.message.type.HouseLocationChanged;
import room107.service.message.type.HousePositionChanged;

/**
 * Locate house.
 * 
 * @author WangXiao
 */
@Component
public class HousePositionChangedHandler extends MessageHandler {

    @Qualifier(value = "cachedLocator")
    @Autowired
    private ILocator locator;

    @Autowired
    private IHouseService houseService;

    @Override
    protected Class<? extends Message> getSubscription() {
        return HousePositionChanged.class;
    }

    @Override
    public void handle(Message message) throws Exception {
        HousePositionChanged housePositionChanged = (HousePositionChanged) message;
        Thread.sleep(1000);
        /*
         * locate
         */
        House house = housePositionChanged.getHouse();
        Location location = locator.getLocation(house.getCity()
                + house.getPosition());
        Validate.notNull(location);
        /*
         * house
         */
        Validate.notNull(house);
        if (house.getLocationX() != null
                && house.getLocationY() != null
                && NumberUtils.compare(house.getLocationX(), location.getX()) == 0
                && NumberUtils.compare(house.getLocationY(), location.getY()) == 0) {
            // not changed
            return;
        }
        house.setLocationX(location.getX());
        house.setLocationY(location.getY());
        houseService.updateHouse(house);
        /*
         * update surroundings
         */
        messageService.send(new HouseLocationChanged(house.getId(), location));
    }

    @Override
    protected int getRetryMaxCount() {
        return 48;
    }

}
