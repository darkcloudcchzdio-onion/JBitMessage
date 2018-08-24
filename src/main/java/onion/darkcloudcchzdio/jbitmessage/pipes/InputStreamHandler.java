package onion.darkcloudcchzdio.jbitmessage.pipes;

import java.io.EOFException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

public class InputStreamHandler implements Runnable {

    private static Logger log = Logger.getLogger(InputStreamHandler.class.getName());
    private InputStream inputStream;
    private MessageHandler messageHandler;

    public InputStreamHandler(InputStream inputStream, MessageHandler messageHandler) {
        this.inputStream = inputStream;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        try {
            log.info("Opening ObjectInputStream");
            ObjectInputStream in = new ObjectInputStream(inputStream);
            for (; ; ) {
                Object object = in.readObject();
                if (!(object instanceof Message)) {
                    log.info("Received non message object: " + object + ". Handler stopped.");
                    break;
                } else {
                    Message message = (Message) object;
                    messageHandler.onMessage(message);
                }
            }
        } catch (EOFException e) {
            log.info("EOF is reached. Handler stopped.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
