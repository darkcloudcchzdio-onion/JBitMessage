package onion.darkcloudcchzdio.jbitmessage.pipes;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class LogUtils {
    public static void readConfiguration(String filename) {
        try {
            InputStream in = LogUtils.class.getClassLoader().getResourceAsStream(filename);
            LogManager.getLogManager().readConfiguration(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
