package room107.service.message;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.tool.AutowiredTest;

public class MessageHandlerTest extends AutowiredTest {

    @Autowired
    private MessageService messageService;

    @Test
    public void testSetMessageService() {
        System.err.println(messageService);
        messageService.send(new Message1());
    }

    public static class Message1 implements Message {
    }

    @Component
    public static class Handler1 extends MessageHandler {

        public Handler1() {
            super();
        }

        @Override
        protected Class<? extends Message> getSubscription() {
            return Message1.class;
        }

        @Override
        protected void handle(Message message) throws Exception {
            System.err.println("handler1");
        }

    }

}
