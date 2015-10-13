package room107.service.house.search;

/**
 * @author WangXiao
 */
public class HouseScorer {

    /**
     * @param scorePriority
     *            begin from 0, lower means higher score
     */
    public double score(int scorePriority, double distance) {
        return 1E5 / (scorePriority + 1) / (distance + 0.1);
    }
}
