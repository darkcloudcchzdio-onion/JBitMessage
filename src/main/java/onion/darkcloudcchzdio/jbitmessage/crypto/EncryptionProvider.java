package onion.darkcloudcchzdio.jbitmessage.crypto;

import java.io.*;

public class EncryptionProvider {
    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            ObjectInput in = new ObjectInputStream(bis);
            Object result = in.readObject();
            in.close();
            return result;
        }
    }

    public byte[] serialize(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        }
    }
}
