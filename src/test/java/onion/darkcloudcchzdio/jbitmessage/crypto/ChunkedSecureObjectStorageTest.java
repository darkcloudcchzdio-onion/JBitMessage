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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ChunkedSecureObjectStorageTest {

    @Parameterized.Parameters
    public static EncryptionProvider[] data() {
        SecretKey secretKey = null;
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128); // 192 and 256 bits may not be available
            secretKey = generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new EncryptionProvider[] {new EncryptionProvider(), new AESEncryptionProvider(secretKey)};
    }

    public ChunkedSecureObjectStorageTest(EncryptionProvider encryptionProvider) {
        this.encryptionProvider = encryptionProvider;
    }

    @Before
    public void before() {
        this.storage = new ChunkedSecureObjectStorage(encryptionProvider, new ByteArrayOutputStream(1024));
    }

    private EncryptionProvider encryptionProvider;
    private ChunkedSecureObjectStorage storage;

    @Test public void testGet() throws IOException, ClassNotFoundException {
        assertNull(storage.get("test"));
        assertNull(storage.get("test", 100));
        assertNull(storage.get("test", -100));
    }

    @Test public void testPutGet() throws IOException, ClassNotFoundException {
        String name = "test";
        Object objectV1 = "test_v0";
        storage.put(name, objectV1);
        assertEquals(objectV1, storage.get(name));
        assertEquals(objectV1, storage.get(name, 0));
        assertEquals(objectV1, storage.get(name, 1));
        Object objectV2 = "test_v1";
        storage.put(name, objectV2);
        assertEquals(objectV1, storage.get(name, 0));
        assertEquals(objectV1, storage.get(name, -2));
        assertEquals(objectV2, storage.get(name));
        assertEquals(objectV2, storage.get(name, 1));
        assertEquals(objectV2, storage.get(name, Integer.MAX_VALUE));
        assertEquals(objectV2, storage.get(name, -1));
    }

    @Test public void testRemove() throws IOException, ClassNotFoundException {
        String name = "test";
        Object objectV1 = "test_v0";
        Object objectV2 = "test_v1";
        storage.put(name, objectV1);
        storage.put(name, objectV2);
        assertTrue(storage.remove(name));
        assertEquals(objectV1, storage.get(name, 0));
        assertNotEquals(objectV2, storage.get(name, 2));
        assertFalse(storage.remove(null));
    }

    @Test public void testGetAll() throws IOException, ClassNotFoundException {
        String name = "test";
        Object objectV1 = "test_v0";
        Object objectV2 = "test_v1";
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

    @Test public void testGetAllWithoutHidden() throws IOException, ClassNotFoundException {
        String name = "test";
        Object objectV1 = "test_v0";
        Object objectV2 = "test_v1";
        Object objectV3 = "test_v2";
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

    @Test public void testGetAllWithHidden() throws IOException, ClassNotFoundException {
        String name = "test";
        Object objectV1 = "test_v0";
        Object objectV2 = "test_v1";
        Object objectV3 = "test_v2";
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

    @Test public void testGetAllAtVersion() throws IOException, ClassNotFoundException {
        String category1 = "test1";
        Object objectC1V0 = "test1_v0";
        Object objectC1V1 = "test1_v1";
        storage.put(category1, objectC1V0);
        storage.put(category1, objectC1V1);
        String category2 = "test2";
        Object objectC2V0 = "test2_v0";
        Object objectC2V1 = "test2_v1";
        storage.put(category2, objectC2V0);
        storage.put(category2, objectC2V1);
        String category3 = "test3";
        Object objectC3V0 = "test3_v0";
        Object objectC3V1 = "test3_v1";
        Object objectC3V2 = "test3_v2";
        storage.put(category3, objectC3V0);
        storage.put(category3, objectC3V1);
        storage.put(category3, objectC3V2);
        Map<String, Map<Integer, Object>> expected = Collections.unmodifiableMap(new HashMap<String, Map<Integer, Object>>() {{
            put(category1, Collections.unmodifiableMap(new HashMap<Integer, Object>(){{
                put(1, objectC1V1);
            }}));
            put(category2, Collections.unmodifiableMap(new HashMap<Integer, Object>(){{
                put(1, objectC2V1);
            }}));
            put(category3, Collections.unmodifiableMap(new HashMap<Integer, Object>(){{
                put(1, objectC3V1);
            }}));
        }});
        assertEquals(expected, storage.getAll("*", 1));
        expected = Collections.unmodifiableMap(new HashMap<String, Map<Integer, Object>>() {{
            put(category3, Collections.unmodifiableMap(new HashMap<Integer, Object>(){{
                put(2, objectC3V2);
            }}));
        }});
        assertEquals(expected, storage.getAll("*", 2));
    }

    @Test public void testGetAllSearchPattern() throws IOException, ClassNotFoundException {
        String category1 = "test1";
        Object objectC1V0 = "test1_v0";
        storage.put(category1, objectC1V0);
        String category2 = "category2";
        Object objectC2V0 = "test2_v0";
        storage.put(category2, objectC2V0);
        String category3 = "test3";
        Object objectC3V0 = "test3_v0";
        Object objectC3V1 = "test3_v1";
        storage.put(category3, objectC3V0);
        storage.put(category3, objectC3V1);
        Map<String, Map<Integer, Object>> expected = Collections.unmodifiableMap(new HashMap<String, Map<Integer, Object>>() {{
            put(category1, Collections.unmodifiableMap(new HashMap<Integer, Object>(){{
                put(0, objectC1V0);
            }}));
            put(category3, Collections.unmodifiableMap(new HashMap<Integer, Object>(){{
                put(0, objectC3V0);
                put(1, objectC3V1);
            }}));
        }});
        assertEquals(expected, storage.getAll("^test.*"));
    }
}