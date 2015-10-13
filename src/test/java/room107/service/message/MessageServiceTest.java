package room107.service.message;

import org.junit.Test;

/**
 * @author WangXiao
 */
public class MessageServiceTest {

    @Test
    public void testSend() throws InterruptedException {
        MessageService service = new MessageService();
        /*
         * handler
         */
        Handler1 handler1 = new Handler1();
        handler1.messageService = service;
        handler1.register();
        Handler2 handler2 = new Handler2();
        handler2.messageService = service;
        handler2.register();
        /*
         * send
         */
        service.send(new Message1());
        service.send(new Message2());
        Thread.sleep(1000);
    }

    static class Message1 implements Message {

    }

    static class Message2 implements Message {

    }

    static class Handler1 extends MessageHandler {

        @Override
        protected void handle(Message message) {
            throw new RuntimeException("1");
        }

        @Override
        protected int getRetryInterval() {
            return 0;
        }

        @Override
        protected int getRetryMaxCount() {
            return 0;
        }

        @Override
        protected Class<? extends Message> getSubscription() {
            return Message1.class;
        }

    }

    static class Handler2 extends MessageHandler {

        @Override
        protected void handle(Message message) {
            throw new RuntimeException("2");
        }

        @Override
        protected int getRetryInterval() {
            return 100;
        }

        @Override
        protected int getRetryMaxCount() {
            return 3;
        }

        @Override
        protected Class<? extends Message> getSubscription() {
            return Message2.class;
        }

    }

}
