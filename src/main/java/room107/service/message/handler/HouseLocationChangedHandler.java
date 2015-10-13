package room107.service.message.handler;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import room107.datamodel.House;
import room107.datamodel.Poi;
import room107.datamodel.PoiType;
import room107.service.api.weixin.WeiXinService;
import room107.service.house.IHouseService;
import room107.service.house.index.IHouseIndexer;
import room107.service.house.search.position.IPositionSearcher;
import room107.service.location.ILocator;
import room107.service.message.Message;
import room107.service.message.MessageHandler;
import room107.service.message.type.HouseLocationChanged;
import room107.util.HouseUtils;

/**
 * Update house surroundings.
 * 
 * @author WangXiao
 */
@CommonsLog
@Component
public class HouseLocationChangedHandler extends MessageHandler {

    @Qualifier(value = "cachedLocator")
    @Autowired
    private ILocator locator;

    @Autowired
    private IHouseService houseService;

    @Autowired
    private IHouseIndexer houseIndexer;

    @Autowired
    private WeiXinService weiXinService;

    @Override
    protected Class<? extends Message> getSubscription() {
        return HouseLocationChanged.class;
    }

    @Override
    protected void handle(Message message) throws Exception {
        HouseLocationChanged houseLocationChanged = (HouseLocationChanged) message;
        /*
         * index line
         */
        List<Poi> pois = new ArrayList<Poi>(128);
        pois.addAll(index(houseLocationChanged, PoiType.SUBWAY,
                IPositionSearcher.SEARCH_SURROUNDING_SUBWAY_RADIUS));
        pois.addAll(index(houseLocationChanged, PoiType.BUS,
                IPositionSearcher.SEARCH_SURROUNDING_BUS_RADIUS));
        /*
         * notify subscriber
         */
        long houseId = houseLocationChanged.getHouseId();
        House house = houseService.getHouse(houseId);
        if (HouseUtils.isValidToPush(house)) {
            weiXinService.notifySubscribers(houseId, pois);
        }
    }

    private List<Poi> index(HouseLocationChanged houseLocationChanged,
            PoiType type, int radius) throws Exception {
        List<Poi> pois = locator.getNearbyPois(
                houseLocationChanged.getLocation(), radius, type);
        if (log.isDebugEnabled()) {
            log.debug("Index: type=" + type + ", POIs=" + pois);
        }
        houseIndexer.indexSurroundingPois(houseLocationChanged.getHouseId(),
                type, pois);
        return pois;
    }

}
