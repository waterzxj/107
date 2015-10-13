package room107.service.message;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author WangXiao
 */
public abstract class MessageHandler {

    @Autowired
    protected MessageService messageService;

    /**
     * Default retry interval: 30 min.
     */
    private static final int DEFAULT_RETRY_INTERVAL = 30 * 60 * 1000;

    /**
     * Default retry max count: 3.
     */
    private static final int DEFAULT_RETRY_MAX_COUNT = 3;

    /**
     * Register itself by {@link #getSubscription()}.
     */
    @PostConstruct
    public void register() {
        Class<? extends Message> message = getSubscription();
        Validate.notNull(message, "subscribe nothing: handler=" + this);
        messageService.register(message, this);
    }

    /**
     * @return subscribed types of messages.
     */
    abstract protected Class<? extends Message> getSubscription();

    /**
     * @param message
     *            assert non-null
     * @throws Exception
     *             may be retried when exception
     */
    abstract protected void handle(Message message) throws Exception;

    /**
     * @return retry interval if handle failed, in millisecond
     */
    protected int getRetryInterval() {
        return DEFAULT_RETRY_INTERVAL;
    }

    /**
     * @return maximum retry count if handle failed
     */
    protected int getRetryMaxCount() {
        return DEFAULT_RETRY_MAX_COUNT;
    }

}
