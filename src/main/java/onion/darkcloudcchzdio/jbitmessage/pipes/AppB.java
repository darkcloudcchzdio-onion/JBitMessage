package onion.darkcloudcchzdio.jbitmessage.pipes;

import java.io.ObjectOutputStream;
import java.util.logging.Logger;

public class AppB {
    static Logger log = Logger.getLogger(AppB.class.getName());

    public static void main(String[] args) throws Exception {
        LogUtils.readConfiguration("app-b-logging.properties");
        log.info("AppB started");
        ObjectOutputStream out = new ObjectOutputStream(System.out);
        MessageHandler messageHandler = new MessageHandler() {
            @Override
            public void onMessage(Message message) {
                log.info("Received message: " + message);
            }
        };
        ProcessUtils.runMessageHandler(messageHandler);
        try {
            for (int i = 0; i < 10; i++) {
                Message message = new Message();
                message.setSource(AppB.class.getName());
                String destination = Math.random() < 0.5 ? AppA.class.getName() : AppC.class.getName();
                message.setDestination(destination);
                message.setContent("Message B #" + i);
                log.info("Sending message:" + message);
                out.writeObject(message);
                Thread.sleep((long) (400 + Math.random() * 400));
            }
            out.writeObject(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("AppB finished");
    }
}
