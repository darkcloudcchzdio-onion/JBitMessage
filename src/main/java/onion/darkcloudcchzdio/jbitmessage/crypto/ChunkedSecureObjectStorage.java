package onion.darkcloudcchzdio.jbitmessage.crypto;

import java.util.HashMap;
import java.util.HashSet;

public class ChunkedSecureObjectStorage {

    final HashMap<String, HashMap<Integer, Object>> nameToActiveObjects = new HashMap<>();
    final HashMap<String, HashSet<Integer>> nameToRemovedObjects = new HashMap<>();

    public Object get(String name) {
        return get(name, -1);
    }

    public Object get(String name, int version) {
        if (!nameToActiveObjects.containsKey(name)) return null;
        version = normalizeVersion(name, version);
        if (nameToRemovedObjects.containsKey(name) && nameToRemovedObjects.get(name).contains(version)) return null;
        HashMap<Integer, Object> versionToObject = nameToActiveObjects.get(name);
        return versionToObject.get(version);
    }

    public HashMap<String, HashMap<Integer, Object>> getAll(String searchPattern) {
        return getAll(searchPattern, -1);
    }

    public HashMap<String, HashMap<Integer, Object>> getAll(String searchPattern, boolean hidden) {
        return getAll(searchPattern, -1, hidden);
    }

    public HashMap<String, HashMap<Integer, Object>> getAll(String searchPattern, int version) {
        return getAll(searchPattern, version, false);
    }

    public HashMap<String, HashMap<Integer, Object>> getAll(String searchPattern, int version, boolean hidden) {
        HashMap<String, HashMap<Integer, Object>> result = new HashMap<>();
        for (String name : nameToActiveObjects.keySet()) {
            if (searchPattern == "*" || name.matches(searchPattern)) {
                HashMap<Integer, Object> versionToObject = nameToActiveObjects.get(name);
                for (int v : versionToObject.keySet()) {
                    if (!hidden && nameToRemovedObjects.containsKey(name) && nameToRemovedObjects.get(name).contains(v)) continue;
                    if (v == version) {
                        if (!result.containsKey(name)) result.put(name, new HashMap<>());
                        result.get(name).put(v, versionToObject.get(v));
                        break;
                    }
                    if (version < 0) {
                        if (!result.containsKey(name)) result.put(name, new HashMap<>());
                        result.get(name).put(v, versionToObject.get(v));
                    }
                }
            }
        }
        return result;
    }

    public void put(String name, Object object) {
        if (!nameToActiveObjects.containsKey(name)) nameToActiveObjects.put(name, new HashMap<>());
        HashMap<Integer, Object> versionToObject = nameToActiveObjects.get(name);
        versionToObject.put(versionToObject.size(), object);
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

    int normalizeVersion(String name, int version) {
        HashMap<Integer, Object> versionToObject = nameToActiveObjects.get(name);
        int size = versionToObject.size();
        if (version >= size) version = size - 1;
        else if (version < 0) {
            if (version > -size) version %= size;
            version = size + version;
        }
        return version;
    }
}
