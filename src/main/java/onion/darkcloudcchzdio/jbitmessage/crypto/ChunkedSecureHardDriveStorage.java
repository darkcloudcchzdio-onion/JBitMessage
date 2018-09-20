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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class ChunkedSecureHardDriveStorage extends ChunkedSecureObjectStorage {

    public ChunkedSecureHardDriveStorage(File file, EncryptionProvider encryptor) {
        super(encryptor, null);
        this.file = file;
        deserialize(file);
    }

    private final File file;

    private void deserialize(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.getAbsoluteFile().toPath());
            deserialize(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void write(byte[] bytes) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
        }
    }

    @Override
    protected byte[] read(ChunkData data) throws IOException {
        byte[] bytes;
        try (FileInputStream in = new FileInputStream(file)) {
            in.skip(data.offset);
            bytes = new byte[data.length];
            in.read(bytes);
        }
        return bytes;
    }
}
