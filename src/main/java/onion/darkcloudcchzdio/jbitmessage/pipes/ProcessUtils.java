package onion.darkcloudcchzdio.jbitmessage.pipes;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ProcessUtils {
    private static Logger log = Logger.getLogger(MainApp.class.getName());
    private static Map<String, ObjectOutputStream> outputRegistry = new HashMap<>();
    private static String JAVA_HOME = System.getProperty("java.home");
    private static String CLASS_PATH = System.getProperty("java.class.path");

    public static void runClassInProcess(Class<?> clazz, MessageHandler messageHandler) {
        try {
            String className = clazz.getName();
            Process process = new ProcessBuilder(JAVA_HOME + "/bin/java", "-cp", CLASS_PATH, className).redirectError(ProcessBuilder.Redirect.INHERIT).start();
            log.info("Process for " + className + " started");
            OutputStream out = process.getOutputStream();
            outputRegistry.put(className, new ObjectOutputStream(out));
            InputStream in = process.getInputStream();
            InputStreamHandler inputStreamHandler = new InputStreamHandler(in, messageHandler);
            Thread thread = new Thread(inputStreamHandler);
            thread.start();
            log.info("InputStreamHandler for " + className + " started");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectOutputStream getOutput(String destination) {
        return outputRegistry.get(destination);
    }

    public static void runMessageHandler(MessageHandler messageHandler) {
        try {
            InputStreamHandler inputStreamHandler = new InputStreamHandler(System.in, messageHandler);
            Thread thread = new Thread(inputStreamHandler);
            thread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
