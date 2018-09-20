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

import com.google.common.primitives.Bytes;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkedSecureObjectStorage implements IObjectStorage<Object> {

    public ChunkedSecureObjectStorage(EncryptionProvider encryptionProvider, ByteArrayOutputStream output) {
        this.encryptionProvider = encryptionProvider;
        this.output = output;
    }

    private final EncryptionProvider encryptionProvider;
    private final ByteArrayOutputStream output;
    private final Map<String, Map<Integer, ChunkData>> nameToActiveObjects = new HashMap<>();
    private final Map<String, Set<Integer>> nameToRemovedObjects = new HashMap<>();
    private int previousChunkPosition = 0;

    void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int chunkLength = buffer.getInt(0);
        byte[] bytes = new byte[chunkLength];
        buffer.get(bytes);
        Chunk chunk = new Chunk(bytes);
        try {
            String key = (String) encryptionProvider.deserialize(chunk.name);
            put(key, bytes);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Object get(String key) throws IOException, ClassNotFoundException {
        return get(key, -1);
    }

    public Object get(String key, int version) throws IOException, ClassNotFoundException {
        if (!nameToActiveObjects.containsKey(key)) return null;
        version = normalizeVersion(key, version);
        if (nameToRemovedObjects.containsKey(key) && nameToRemovedObjects.get(key).contains(version)) return null;
        Map<Integer, ChunkData> versionToChunks = nameToActiveObjects.get(key);
        ChunkData chunkData = versionToChunks.get(version);
        return get(chunkData);
    }

    private Object get(ChunkData data) throws IOException, ClassNotFoundException {
        byte[] chunk = read(data);
        byte[] bytes = new Chunk(chunk).data;
        return encryptionProvider.deserialize(bytes);
    }

    public Map<String, Map<Integer, Object>> getAll(String searchPattern) throws IOException, ClassNotFoundException {
        return getAll(searchPattern, -1);
    }

    public Map<String, Map<Integer, Object>> getAll(String searchPattern, boolean hidden) throws IOException, ClassNotFoundException {
        return getAll(searchPattern, -1, hidden);
    }

    public Map<String, Map<Integer, Object>> getAll(String searchPattern, int version) throws IOException, ClassNotFoundException {
        return getAll(searchPattern, version, false);
    }

    public Map<String, Map<Integer, Object>> getAll(String searchPattern, int version, boolean hidden) throws IOException, ClassNotFoundException {
        boolean all = searchPattern == "*";
        Map<String, Map<Integer, Object>> result = new HashMap<>();
        for (String name : nameToActiveObjects.keySet()) {
            if (all || name.matches(searchPattern)) {
                Map<Integer, ChunkData> versionToChunks = nameToActiveObjects.get(name);
                for (int v : versionToChunks.keySet()) {
                    if (!hidden && nameToRemovedObjects.containsKey(name) && nameToRemovedObjects.get(name).contains(v)) continue;
                    if (version < 0) {
                        if (!result.containsKey(name)) result.put(name, new HashMap<>());
                        ChunkData chunkData = versionToChunks.get(v);
                        Object object = get(chunkData);
                        result.get(name).put(v, object);
                    } else if (v == version) {
                        if (!result.containsKey(name)) result.put(name, new HashMap<>());
                        ChunkData chunkData = versionToChunks.get(v);
                        Object object = get(chunkData);
                        result.get(name).put(v, object);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void put(String key, Object value) throws IOException {
        byte[] name = encryptionProvider.serialize(key);
        byte[] nameLength = ByteBuffer.allocate(4).putInt(name.length).array();
        byte[] data = encryptionProvider.serialize(value);
        byte[] chunkLength = ByteBuffer.allocate(4).putInt(4 + 4 + name.length + data.length).array();
        byte[] bytes = Bytes.concat(chunkLength, nameLength, name, data);
        put(key, bytes);
        write(bytes);
    }

    private void put(String key, byte[] bytes) {
        if (!nameToActiveObjects.containsKey(key)) nameToActiveObjects.put(key, new HashMap<>());
        Map<Integer, ChunkData> versionToChunks = nameToActiveObjects.get(key);
        versionToChunks.put(versionToChunks.size(), new ChunkData(previousChunkPosition, bytes.length));
        previousChunkPosition += bytes.length;
    }

    public boolean remove(String key) {
        return remove(key, -1);
    }

    public boolean remove(String key, int version) {
        if (!nameToActiveObjects.containsKey(key)) return false;
        version = normalizeVersion(key, version);
        if (!nameToRemovedObjects.containsKey(key)) nameToRemovedObjects.put(key, new HashSet<>());
        Set<Integer> versions = nameToRemovedObjects.get(key);
        return versions.add(version);
    }

    private int normalizeVersion(String key, int version) {
        Map<Integer, ?> versionToBytes = nameToActiveObjects.get(key);
        int size = versionToBytes.size();
        if (version >= size) version = size - 1;
        else if (version < 0) {
            if (version > -size) version %= size;
            version = size + version;
        }
        return version;
    }

    protected void write(byte[] bytes) throws IOException {
        output.write(bytes);
    }

    protected byte[] read(ChunkData data) throws IOException {
        byte[] bytes = new byte[data.length];
        try (ByteArrayInputStream in = new ByteArrayInputStream((output).toByteArray())) {
            in.skip(data.offset);
            in.read(bytes, 0, data.length);
            return bytes;
        }
    }
}

class ChunkData {
    ChunkData(int position, int length) {
        this.offset = position;
        this.length = length;
    }

    int offset;
    int length;
}

class Chunk {

    Chunk(byte[] chunk) {
        ByteBuffer buffer = ByteBuffer.wrap(chunk);
        chunkLength = buffer.getInt();
        nameLength = buffer.getInt();
        name = new byte[nameLength];
        buffer.get(name);
        data = new byte[chunk.length - (4 + 4 + nameLength)];
        buffer.get(data);
    }

    final int nameLength;
    final byte[] name;
    final int chunkLength;
    final byte[] data;
}