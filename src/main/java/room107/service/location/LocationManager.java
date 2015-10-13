package room107.service.location;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import room107.dao.house.IHouseDao;
import room107.datamodel.House;
import room107.datamodel.Location;

/**
 * @author WangXiao
 */
@CommonsLog
@Component
public class LocationManager {

    @Autowired
    private LocationUpdater locationUpdater;

    private final int locateIntervalMin = 30;

    @PostConstruct
    public void init() {
        new Timer(getClass().getSimpleName()).schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    locationUpdater.updateLocation();
                } catch (Exception e) {
                    log.error("Locate failed", e);
                }
            }
        }, 0, locateIntervalMin * 60000);
    }

    /**
     * To be wrapped by AOP.
     * 
     * @author WangXiao
     */
    @Component
    public static class LocationUpdater {

        @Autowired
        private IHouseDao houseDao;

        @Qualifier(value = "cachedLocator")
        @Autowired
        private ILocator locator;

        public void updateLocation() {
            log.debug("Start locate");
            for (House house : houseDao.getUnlocatedHouses()) {
                String position = StringUtils.trimToNull(house.getPosition());
                try {
                    Validate.notNull(position, "null position: houseId="
                            + house.getId());
                    position = house.getCity() + "区" + position;
                    /*
                     * locate
                     */
                    Location location = locator.getLocation(position);
                    Validate.notNull(location, "null location");
                    /*
                     * save location
                     */
                    house.setLocationX(location.getX());
                    house.setLocationY(location.getY());
                    log.info("Locate house: id=" + house.getId()
                            + ", position=" + position + ", location="
                            + location);
                    houseDao.saveHouse(house);
                } catch (Exception e) {
                    log.error("Locate failed: id=" + house.getId()
                            + ", position=" + position + ", error=" + e);
                }
            }
        }
    }

}
