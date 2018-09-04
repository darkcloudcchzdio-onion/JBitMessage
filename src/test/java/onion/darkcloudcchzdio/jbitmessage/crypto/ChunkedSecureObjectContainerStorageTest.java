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

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class ChunkedSecureObjectContainerStorageTest {

    @Test public void testGet() {
        ChunkedSecureObjectContainerStorage container = new ChunkedSecureObjectContainerStorage();
        assertNull(container.get(null));
        assertNull(container.get("test"));
    }

    @Test public void testPutGet() throws Exception {
        ChunkedSecureObjectContainerStorage container = new ChunkedSecureObjectContainerStorage();
        container.put("container", container);
        assertEquals(container, container.get("container"));
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage(new EncryptionProvider(), new ByteArrayOutputStream(0));
        container.put("test", storage);
        assertEquals(storage, container.get("test"));
    }

    @Test public void testRemove() throws Exception {
        ChunkedSecureObjectContainerStorage container = new ChunkedSecureObjectContainerStorage();
        container.put("container", container);
        assertTrue(container.remove("container"));
        assertFalse(container.remove("container"));
    }
}
