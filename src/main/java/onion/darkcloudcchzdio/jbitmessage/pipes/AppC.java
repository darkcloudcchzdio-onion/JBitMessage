package onion.darkcloudcchzdio.jbitmessage.pipes;

import java.io.ObjectOutputStream;
import java.util.logging.Logger;

public class AppC {
    static Logger log = Logger.getLogger(AppC.class.getName());

    public static void main(String[] args) throws Exception {
        LogUtils.readConfiguration("app-c-logging.properties");
        log.info("AppC started");
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
                message.setSource(AppC.class.getName());
                String destination = Math.random() < 0.5 ? AppA.class.getName() : AppB.class.getName();
                message.setDestination(destination);
                message.setContent("Message C #" + i);
                log.info("Sending message:" + message);
                out.writeObject(message);
                Thread.sleep((long) (600 + Math.random() * 600));
            }
            out.writeObject(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("AppC finished");
    }
}
