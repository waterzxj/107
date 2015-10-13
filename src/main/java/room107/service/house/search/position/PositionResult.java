package room107.service.house.search.position;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import room107.datamodel.House;
import room107.datamodel.Room;

/**
 * @author WangXiao
 */
@Getter
@Setter
@ToString
public class PositionResult implements Comparable<PositionResult> {

    private House house;

    private Room room;

    private double distance;

    private double score;

    /**
     * @param room
     *            null when rent by house
     */
    public PositionResult(House house, Room room, double distance) {
        this.house = house;
        this.room = room;
        this.distance = distance;
    }

    @Override
    public int compareTo(PositionResult o) {
        return -Double.compare(score, ((PositionResult) o).score);
    }

    @Override
    public int hashCode() {
        return house.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return house.getId() == ((PositionResult) obj).house.getId();
    }

}
