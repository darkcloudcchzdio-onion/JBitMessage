/*******************************************************************************
               __ _____ _ _   _____
            __|  | __  |_| |_|     |___ ___ ___ ___ ___ ___
           |  |  | __ -| |  _| | | | -_|_ -|_ -| .'| . | -_|
           |_____|_____|_|_| |_|_|_|___|___|___|__,|_  |___|
                                                   |___|
 Security oriented Java implementation of @Bitmessage CLIENT and SERVER
 (https://github.com/darkcloudcchzdio-onion/JBitMessage)

 Developed by darkcloudcchzdio.onion Dev Team and other Contributors
 Sponsored by darkcloudcchzdio.onion and other Donators

 Contacts:
 * email: dev+jbitmessage@darkcloudcchzdio.onion
 * url: https://darkcloudcchzdio.onion/project/jbitmessage
 Please note, you need Tor Browser or any Tor related software for access
 to Tor Hidden Services (aka .onion) sites.
 More information about Tor and related - https://www.torproject.org/

 License terms:
 * for open source purpose - GPL3 (LICENSE.GPL3)
 * for education purpose - CC BY-NC-ND 4.0i (LICENSE.CC)
 * for commercial or any other purpose - EULA (LICENSE.EULA)

 *******************************************************************************/
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
