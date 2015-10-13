package room107.service.job;

/**
 * @author WangXiao
 */
public abstract class SimpleJob {

    public String getName() {
        return getClass().getSimpleName();
    }

    public abstract void run();

}
