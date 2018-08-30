package onion.darkcloudcchzdio.jbitmessage.crypto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ChunkedSecureObjectStorage {

    final HashMap<String, HashMap<Integer, Object>> nameToActiveObjects = new HashMap<>();
    final HashMap<String, HashSet<Integer>> nameToRemovedObjects = new HashMap<>();

    public Object read(String name) {
        return read(name, -1);
    }

    public Object read(String name, int version) {
        if (!nameToActiveObjects.containsKey(name)) return null;
        HashMap<Integer, Object> versionToObject = nameToActiveObjects.get(name);
        if (version < 0 || version >= versionToObject.size()) version = versionToObject.size() - 1;
        if (nameToRemovedObjects.containsKey(name) && nameToRemovedObjects.get(name).contains(version)) return null;
        return versionToObject.get(version);
    }

    public ArrayList<Object> readAll(String mask) {
        return readAll(mask, -1);
    }

    public ArrayList<Object> readAll(String mask, int version) {
        return readAll(mask, -1, false);
    }

    public ArrayList<Object> readAll(String mask, int version, Boolean hidden) {
        return null;
    }

    public void write(String name, Object object) {
        if (!nameToActiveObjects.containsKey(name)) nameToActiveObjects.put(name, new HashMap<>());
        HashMap<Integer, Object> versionToObject = nameToActiveObjects.get(name);
        versionToObject.put(versionToObject.size(), object);
    }

    public boolean delete(String name) {
        return delete(name, -1);
    }

    public boolean delete(String name, int version) {
        if (!nameToActiveObjects.containsKey(name)) return false;
        HashMap<Integer, Object> versionToObject = nameToActiveObjects.get(name);
        if (version < 0 || version >= versionToObject.size()) version = versionToObject.size() - 1;
        if (!nameToRemovedObjects.containsKey(name)) nameToRemovedObjects.put(name, new HashSet<>());
        HashSet<Integer> versions = nameToRemovedObjects.get(name);
        return versions.add(version);
    }
}
