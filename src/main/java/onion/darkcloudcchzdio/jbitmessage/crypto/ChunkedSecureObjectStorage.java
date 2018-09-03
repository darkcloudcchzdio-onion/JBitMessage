package onion.darkcloudcchzdio.jbitmessage.crypto;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class ChunkedSecureObjectStorage {

    public ChunkedSecureObjectStorage(InputStream in, OutputStream out) {
        input = in;
        output = out;
    }

    private final HashMap<String, HashMap<Integer, byte[]>> nameToActiveObjects = new HashMap<>();
    private final HashMap<String, HashSet<Integer>> nameToRemovedObjects = new HashMap<>();
    private InputStream input;
    private OutputStream output;

    public Object get(String name) throws IOException, ClassNotFoundException {
        return get(name, -1);
    }

    public Object get(String name, int version) throws IOException, ClassNotFoundException {
        if (!nameToActiveObjects.containsKey(name)) return null;
        version = normalizeVersion(name, version);
        if (nameToRemovedObjects.containsKey(name) && nameToRemovedObjects.get(name).contains(version)) return null;
        return getInternal(name, version);
    }

    private Object getInternal(String name, int version) throws IOException, ClassNotFoundException {
        HashMap<Integer, byte[]> versionToBytes = nameToActiveObjects.get(name);
        byte[] bytes = versionToBytes.get(version);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            ObjectInput in = new ObjectInputStream(bis);
            Object result = in.readObject();
            in.close();
            return result;
        }
    }

    public HashMap<String, HashMap<Integer, Object>> getAll(String searchPattern) throws IOException, ClassNotFoundException {
        return getAll(searchPattern, -1);
    }

    public HashMap<String, HashMap<Integer, Object>> getAll(String searchPattern, boolean hidden) throws IOException, ClassNotFoundException {
        return getAll(searchPattern, -1, hidden);
    }

    public HashMap<String, HashMap<Integer, Object>> getAll(String searchPattern, int version) throws IOException, ClassNotFoundException {
        return getAll(searchPattern, version, false);
    }

    public HashMap<String, HashMap<Integer, Object>> getAll(String searchPattern, int version, boolean hidden) throws IOException, ClassNotFoundException {
        boolean all = searchPattern == "*";
        HashMap<String, HashMap<Integer, Object>> result = new HashMap<>();
        for (String name : nameToActiveObjects.keySet()) {
            if (all || name.matches(searchPattern)) {
                HashMap<Integer, byte[]> versionToBytes = nameToActiveObjects.get(name);
                for (int v : versionToBytes.keySet()) {
                    if (!hidden && nameToRemovedObjects.containsKey(name) && nameToRemovedObjects.get(name).contains(v)) continue;
                    if (v == version) {
                        if (!result.containsKey(name)) result.put(name, new HashMap<>());
                        Object object = getInternal(name, v);
                        result.get(name).put(v, object);
                        break;
                    }
                    if (version < 0) {
                        if (!result.containsKey(name)) result.put(name, new HashMap<>());
                        Object object = getInternal(name, v);
                        result.get(name).put(v, object);
                    }
                }
            }
        }
        return result;
    }

    public void put(String name, Object object) throws IOException {
        if (!nameToActiveObjects.containsKey(name)) nameToActiveObjects.put(name, new HashMap<>());
        HashMap<Integer, byte[]> versionToObject = nameToActiveObjects.get(name);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            byte[] bytes = bos.toByteArray();
            versionToObject.put(versionToObject.size(), bytes);
        }
    }

    public boolean remove(String name) {
        return remove(name, -1);
    }

    public boolean remove(String name, int version) {
        if (!nameToActiveObjects.containsKey(name)) return false;
        version = normalizeVersion(name, version);
        if (!nameToRemovedObjects.containsKey(name)) nameToRemovedObjects.put(name, new HashSet<>());
        HashSet<Integer> versions = nameToRemovedObjects.get(name);
        return versions.add(version);
    }

    private int normalizeVersion(String name, int version) {
        HashMap<Integer, byte[]> versionToBytes = nameToActiveObjects.get(name);
        int size = versionToBytes.size();
        if (version >= size) version = size - 1;
        else if (version < 0) {
            if (version > -size) version %= size;
            version = size + version;
        }
        return version;
    }
}
