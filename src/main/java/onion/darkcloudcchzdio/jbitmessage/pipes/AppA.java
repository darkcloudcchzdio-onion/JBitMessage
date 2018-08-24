package onion.darkcloudcchzdio.jbitmessage.pipes;

import java.io.ObjectOutputStream;
import java.util.logging.Logger;

public class AppA {
    static Logger log = Logger.getLogger(AppA.class.getName());

    public static void main(String[] args) throws Exception {
        LogUtils.readConfiguration("app-a-logging.properties");
        log.info("AppA started");
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
                message.setSource(AppA.class.getName());
                String destination = Math.random() < 0.5 ? AppB.class.getName() : AppC.class.getName();
                message.setDestination(destination);
                message.setContent("Message A #" + i);
                log.info("Sending message:" + message);
                out.writeObject(message);
                Thread.sleep((long) (500 + Math.random() * 500));
            }
            out.writeObject(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("AppA finished");
    }
}
