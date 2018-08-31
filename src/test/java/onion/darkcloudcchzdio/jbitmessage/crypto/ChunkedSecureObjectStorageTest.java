package onion.darkcloudcchzdio.jbitmessage.crypto;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ChunkedSecureObjectStorageTest {

    // TODO: get(name, version, hidden = false)
    // TODO: getAll(mask:String, ...)
    // TODO: getAll(mask:RexExp, ...)
    @Test public void testGet(){
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        assertNull(storage.get("test"));
        assertNull(storage.get("test", 100));
        assertNull(storage.get("test", -100));
    }

    @Test public void testPutGet(){
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        String name = "test";
        Object objectV1 = new Object() {public final int v = 1;};
        storage.put(name, objectV1);
        assertEquals(objectV1, storage.get(name));
        assertEquals(objectV1, storage.get(name, 0));
        assertEquals(objectV1, storage.get(name, 1));
        Object objectV2 = new Object() {public final int v = 2;};
        storage.put(name, objectV2);
        assertEquals(objectV1, storage.get(name, 0));
        assertEquals(objectV1, storage.get(name, -2));
        assertEquals(objectV2, storage.get(name));
        assertEquals(objectV2, storage.get(name, 1));
        assertEquals(objectV2, storage.get(name, Integer.MAX_VALUE));
        assertEquals(objectV2, storage.get(name, -1));
    }

    @Test public void testRemove() {
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        String name = "test";
        Object objectV1 = new Object();
        Object objectV2 = new Object();
        storage.put(name, objectV1);
        storage.put(name, objectV2);
        assertTrue(storage.remove(name));
        assertEquals(objectV1, storage.get(name, 0));
        assertNotEquals(objectV2, storage.get(name, 2));
        assertFalse(storage.remove(null));
    }

    @Test public void testGetAll() {
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        String name = "test";
        Object objectV1 = new Object();
        Object objectV2 = new Object();
        storage.put(name, objectV1);
        storage.put(name, objectV2);
        Map<String, Map<Integer, Object>> expected = Collections.unmodifiableMap(new HashMap<String, Map<Integer, Object>>() {{
            put(name, Collections.unmodifiableMap(new HashMap<Integer, Object>(){{
                put(0, objectV1);
                put(1, objectV2);
            }}));
        }});
        assertEquals(expected, storage.getAll("*"));
    }

    @Test public void testGetAllWithoutHidden() {
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        String name = "test";
        Object objectV1 = new Object();
        Object objectV2 = new Object();
        Object objectV3 = new Object();
        storage.put(name, objectV1);
        storage.put(name, objectV2);
        storage.put(name, objectV3);
        storage.remove(name, 1);
        Map<String, Map<Integer, Object>> expected = Collections.unmodifiableMap(new HashMap<String, Map<Integer, Object>>() {{
            put(name, Collections.unmodifiableMap(new HashMap<Integer, Object>(){{
                put(0, objectV1);
                put(2, objectV3);
            }}));
        }});
        assertEquals(expected, storage.getAll("*"));
    }

    @Test public void testGetAllWithHidden() {
        ChunkedSecureObjectStorage storage = new ChunkedSecureObjectStorage();
        String name = "test";
        Object objectV1 = new Object();
        Object objectV2 = new Object();
        Object objectV3 = new Object();
        storage.put(name, objectV1);
        storage.put(name, objectV2);
        storage.put(name, objectV3);
        storage.remove(name, 1);
        Map<String, Map<Integer, Object>> expected = Collections.unmodifiableMap(new HashMap<String, Map<Integer, Object>>() {{
            put(name, Collections.unmodifiableMap(new HashMap<Integer, Object>(){{
                put(0, objectV1);
                put(1, objectV2);
                put(2, objectV3);
            }}));
        }});
        assertEquals(expected, storage.getAll("*", true));
    }
}
