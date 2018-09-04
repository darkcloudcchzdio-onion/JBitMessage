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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkedSecureObjectStorage implements IObjectStorage<Object> {

    public ChunkedSecureObjectStorage(EncryptionProvider encryptor, ByteArrayOutputStream output) {
        this.encryptor = encryptor;
        this.output = output;
    }

    private final Map<String, Map<Integer, ChunkData>> nameToActiveObjects = new HashMap<>();
    private final Map<String, Set<Integer>> nameToRemovedObjects = new HashMap<>();
    private final ByteArrayOutputStream output;
    private final EncryptionProvider encryptor;
    private int previousChunkPosition = 0;

    public Object get(String name) throws IOException, ClassNotFoundException {
        return get(name, -1);
    }

    public Object get(String name, int version) throws IOException, ClassNotFoundException {
        if (!nameToActiveObjects.containsKey(name)) return null;
        version = normalizeVersion(name, version);
        if (nameToRemovedObjects.containsKey(name) && nameToRemovedObjects.get(name).contains(version)) return null;
        Map<Integer, ChunkData> versionToChunks = nameToActiveObjects.get(name);
        ChunkData chunk = versionToChunks.get(version);
        byte[] bytes = get(chunk);
        return encryptor.deserialize(bytes);
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
                    if (v == version) {
                        if (!result.containsKey(name)) result.put(name, new HashMap<>());
                        ChunkData chunk = versionToChunks.get(v);
                        byte[] bytes = get(chunk);
                        Object object = encryptor.deserialize(bytes);
                        result.get(name).put(v, object);
                        break;
                    }
                    if (version < 0) {
                        if (!result.containsKey(name)) result.put(name, new HashMap<>());
                        ChunkData chunk = versionToChunks.get(v);
                        byte[] bytes = get(chunk);
                        Object object = encryptor.deserialize(bytes);
                        result.get(name).put(v, object);
                    }
                }
            }
        }
        return result;
    }

    public void put(String name, Object object) throws IOException {
        if (!nameToActiveObjects.containsKey(name)) nameToActiveObjects.put(name, new HashMap<>());
        byte[] bytes = encryptor.serialize(object);
        output.write(bytes);
        Map<Integer, ChunkData> versionToChunks = nameToActiveObjects.get(name);
        versionToChunks.put(versionToChunks.size(), new ChunkData(previousChunkPosition, bytes.length));
        previousChunkPosition += bytes.length;
    }

    public boolean remove(String name) {
        return remove(name, -1);
    }

    public boolean remove(String name, int version) {
        if (!nameToActiveObjects.containsKey(name)) return false;
        version = normalizeVersion(name, version);
        if (!nameToRemovedObjects.containsKey(name)) nameToRemovedObjects.put(name, new HashSet<>());
        Set<Integer> versions = nameToRemovedObjects.get(name);
        return versions.add(version);
    }

    private int normalizeVersion(String name, int version) {
        Map<Integer, ?> versionToBytes = nameToActiveObjects.get(name);
        int size = versionToBytes.size();
        if (version >= size) version = size - 1;
        else if (version < 0) {
            if (version > -size) version %= size;
            version = size + version;
        }
        return version;
    }

    private byte[] get(ChunkData data) throws IOException {
        byte[] bytes = new byte[data.length];
        try (ByteArrayInputStream in = new ByteArrayInputStream((output).toByteArray())) {
            in.skip(data.position);
            in.read(bytes, 0, data.length);
            return bytes;
        }
    }
}

class ChunkData {
    ChunkData(int position, int length) {
        this.position = position;
        this.length = length;
    }

    int position;
    int length;
}