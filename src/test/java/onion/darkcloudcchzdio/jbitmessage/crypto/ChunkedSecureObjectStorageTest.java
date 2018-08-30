package onion.darkcloudcchzdio.jbitmessage.crypto;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChunkedSecureObjectStorageTest {

    @Test public void testRead(){
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        assertNull(storage.read("test"));
        assertNull(storage.read("test", 100));
        assertNull(storage.read("test", -100));
    }

    @Test public void testWriteRead(){
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        String name = "test";
        Object objectV1 = new Object();
        storage.write(name, objectV1);
        assertEquals(objectV1, storage.read(name));
        assertEquals(objectV1, storage.read(name, 1));
        Object objectV2 = new Object();
        storage.write(name, objectV2);
        assertEquals(objectV2, storage.read(name));
        assertEquals(objectV2, storage.read(name, 1));
        assertEquals(objectV1, storage.read(name, 0));
    }

    @Test public void testRemove() {
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        String name = "test";
        Object objectV1 = new Object();
        Object objectV2 = new Object();
        storage.write(name, objectV1);
        storage.write(name, objectV2);
        assertTrue(storage.delete(name));
        assertEquals(objectV1, storage.read(name, 0));
        assertNotEquals(objectV2, storage.read(name, 2));
        assertFalse(storage.delete(null));
    }

    @Test public void testReadByMask() {
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        String name = "test";
        Object objectV1 = new Object();
        Object objectV2 = new Object();
        storage.write(name, objectV1);
        storage.write(name, objectV2);
        storage.delete(name);
        assertNull(storage.readAll("*", -1, false));
    }
}
