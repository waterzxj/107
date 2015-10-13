package room107.service.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.stereotype.Service;

/**
 * Singleton.
 * 
 * @author WangXiao
 */
@Service
@CommonsLog
public class MessageService {

    private Map<Class<? extends Message>, Collection<MessageHandler>> handlerMap = new HashMap<Class<? extends Message>, Collection<MessageHandler>>();

    private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>(
            1024);

    private BlockingQueue<RetryMessage> retryQueue = new LinkedBlockingQueue<RetryMessage>(
            1024);

    public MessageService() {
        new MessageScheduler().start();
        new MessageRetryer().start();
    }

    /**
     * Make specified handler subscribe the given type of message.
     */
    public synchronized <T extends Message> void register(
            Class<T> messageClass, MessageHandler handler) {
        if (messageClass == null || handler == null) {
            return;
        }
        Collection<MessageHandler> collection = handlerMap.get(messageClass);
        if (collection == null) {
            handlerMap.put(messageClass,
                    collection = new ArrayList<MessageHandler>());
        }
        log.info("Register message handler: message="
                + messageClass.getSimpleName() + ", handler="
                + handler.getClass().getSimpleName());
        collection.add(handler);
    }

    /**
     * Thread-safe.
     */
    public void send(Message message) {
        if (message == null) {
            log.warn("Null message");
            return;
        }
        log.info("Send message: " + message);
        messageQueue.offer(message);
    }

    /**
     * Handle {@link MessageService#messageQueue}.
     * 
     * @author WangXiao
     */
    private class MessageScheduler extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    schedule(messageQueue.take());
                } catch (Exception e) {
                    log.error("Failed to take message from queue", e);
                }
            }
        }

        private void schedule(Message message) {
            if (message == null) {
                return;
            }
            Collection<MessageHandler> handlers = handlerMap.get(message
                    .getClass());
            if (log.isDebugEnabled()) {
                log.debug("Schedule: message="
                        + message.getClass().getSimpleName() + ", handlers: "
                        + handlers);
            }
            if (handlers == null || handlers.isEmpty()) {
                log.warn("No handler for message: " + message);
                return;
            }
            for (MessageHandler handler : handlers) {
                StopWatch watch = new StopWatch();
                watch.start();
                try {
                    log.info("Handle message: message=" + message
                            + ", handler=" + handler.getClass().getSimpleName());
                    handler.handle(message);
                    if (log.isDebugEnabled()) {
                        log.debug("Handle time: " + watch);
                    }
                } catch (Exception e) {
                    log.error("Handle message failed: " + message, e);
                    if (handler.getRetryMaxCount() > 0) {
                        RetryMessage retryMessage = new RetryMessage(message,
                                handler);
                        log.info("Retry later: " + retryMessage);
                        retryQueue.offer(retryMessage);
                    }
                }
            }
        }

    }

    /**
     * Retry between {@link Message} and {@link MessageHandler}.
     * 
     * @author WangXiao
     */
    @RequiredArgsConstructor
    private static class RetryMessage {

        /**
         * Original.
         */
        @NonNull
        private Message message;

        @NonNull
        private MessageHandler handler;

        private int retryCount;

        private long handleTime = System.currentTimeMillis();

        void retry() throws Exception {
            retryCount++;
            handleTime = System.currentTimeMillis();
            handler.handle(message);
        }

        /**
         * @return [0, {@link MessageHandler#getRetryMaxCount()}].
         */
        int getRetryQuota() {
            return handler.getRetryMaxCount() - retryCount;
        }

        /**
         * @return <=0 means able to schedule
         */
        int getRetryLeftTime() {
            return (int) (handleTime + handler.getRetryInterval() - System
                    .currentTimeMillis());
        }

        @Override
        public String toString() {
            return "RetryMessage [message="
                    + message.getClass().getSimpleName() + ", handler="
                    + handler.getClass().getSimpleName() + ", retryQuota="
                    + getRetryQuota() + ", retryLeftTime=" + getRetryLeftTime()
                    + "]";
        }

    }

    /**
     * Handle {@link MessageService#retryQueue}.
     * 
     * @author WangXiao
     */
    private class MessageRetryer extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    RetryMessage retryMessage = retryQueue.take();
                    if (retryMessage == null) {
                        continue;
                    }
                    if (retryMessage.getRetryQuota() <= 0) {
                        continue; // discard
                    }
                    if (retryMessage.getRetryLeftTime() <= 0) {
                        try {
                            log.info("Retry: " + retryMessage);
                            retryMessage.retry();
                            continue; // success
                        } catch (Exception e) {
                            log.error("Retry failed: " + retryMessage);
                        }
                    }
                    retryQueue.put(retryMessage);
                    Thread.sleep(retryMessage.handler.getRetryInterval());
                } catch (InterruptedException e) {
                    log.error("Retry interrupted", e);
                }
            }
        }

    }

}
