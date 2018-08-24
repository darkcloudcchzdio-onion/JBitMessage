package onion.darkcloudcchzdio.jbitmessage.pipes;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

public class MainApp {
    private static Logger log = Logger.getLogger(MainApp.class.getName());

    public static void main(String[] args) {
        System.out.println("see logs");
        LogUtils.readConfiguration("main-logging.properties");
        log.info("MainApp started");
        MessageHandler messageHandler = new MessageHandler() {
            @Override
            public void onMessage(Message message) {
                String destination = message.getDestination();
                log.info("Received message: " + message);
                ObjectOutputStream out = ProcessUtils.getOutput(destination);
                if (out != null) {
                    try {
                        log.info("Sending message to destination: " + destination);
                        out.writeObject(message);
                        out.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        };
        ProcessUtils.runClassInProcess(AppA.class, messageHandler);
        ProcessUtils.runClassInProcess(AppB.class, messageHandler);
        ProcessUtils.runClassInProcess(AppC.class, messageHandler);
        log.info("main method finished");
    }

}
