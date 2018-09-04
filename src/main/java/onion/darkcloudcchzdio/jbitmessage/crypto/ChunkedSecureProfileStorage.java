package onion.darkcloudcchzdio.jbitmessage.crypto;

import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

public class ChunkedSecureProfileStorage extends ChunkedSecureObjectContainerStorage {
    public ChunkedSecureProfileStorage(String userKey) {
        try {
            signingKey = new SecretKeySpec(userKey.getBytes("UTF-8"),"HmacSHA1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private SecretKeySpec signingKey;
}
