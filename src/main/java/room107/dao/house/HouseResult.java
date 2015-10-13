package room107.dao.house;

import java.util.Collection;
import java.util.Collections;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.Validate;

import room107.datamodel.House;
import room107.datamodel.RentType;
import room107.datamodel.User;
import room107.util.StringUtils;
import room107.web.house.Item;
import room107.web.house.Item.Channel;
import room107.web.house.Itemable;

/**
 * @author WangXiao
 */
public class HouseResult implements Comparable<HouseResult>, Itemable {

    @Getter
    protected House house;
    @Setter
    @Getter
    protected User user;
    
    @Setter
    @Getter
    protected long distance = Integer.MAX_VALUE;

    public HouseResult(House house) {
        Validate.notNull(house);
        this.house = house;
    }

    public RentType getRentType() {
        return RentType.BY_HOUSE;
    }

    public double getScore() {
        /*
         * TODO rank, distance, POIs
         */
        long hourDelta = (System.currentTimeMillis() - house.getModifiedTime()
                .getTime()) / 3600000 + 1;
        return getRank() + 1.0 / hourDelta;
    }

    public long getId() {
        return house.getId();
    }

    public String getName() {
        return house.getName();
    }

    public Integer getPrice() {
        return house.getPrice();
    }

    public float getRank() {
        return house.getRank();
    }

    public String getUserName() {
        return house.getUsername();
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp = getId();
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HouseResult other = (HouseResult) obj;
        return getId() == other.getId();
    }
    
    public boolean isTextOnly() {
        return StringUtils.isEmpty(house.getImageId());
    }

    @Override
    public final int compareTo(HouseResult o) {
        if (isTextOnly() != o.isTextOnly()) {
            return isTextOnly() ? 1 : -1;
        }
        if (distance < o.distance) return -1;
        else if (distance > o.distance) return 1;
        double d = getScore() - o.getScore();
        return d == 0 ? 0 : (d > 0 ? -1 : 1);
    }

    @Override
    public String toString() {
        return StringUtils.toString(getRentType(), getId(), house.getCity(),
                house.getPosition(), getName(), getPrice(), getScore());
    }

    @Override
    public Item toItem() {
        Item result = new Item(Channel.DEFAULT.ordinal());
        result.setId(getId());
        result.setType(getRentType().ordinal());
        result.setPrice(getPrice());
        result.setDistrict(house.getCity());
        result.setPosition(house.getPosition());
        result.setName(getName());
        result.setDescription(house.getDescription());
        result.setX(house.getLocationX());
        result.setY(house.getLocationY());
        result.setFaviconUrl(user.getFaviconUrl());
        // cover
        result.setCover(house.getImageId());
        result.setTimestamp(house.getModifiedTime().getTime());
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Item> toItems(Collection<HouseResult> houseResults) {
        if (CollectionUtils.isEmpty(houseResults)) {
            return Collections.emptyList();
        }
        return CollectionUtils.collect(houseResults, new Transformer() {
            @Override
            public Object transform(Object input) {
                HouseResult r = (HouseResult) input;
                Item item = r.toItem();
                if (StringUtils.isEmpty(item.getCover())) {
                    item.setCover(Item.DEFAULT_COVER);
                }
                return item;
            }
        });
    }

}
