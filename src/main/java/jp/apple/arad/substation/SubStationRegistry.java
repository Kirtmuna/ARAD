package jp.apple.arad.substation;

import jp.apple.arad.data.SubStationSnapshot;

import java.util.*;

public final class SubStationRegistry {

    public static final SubStationRegistry INSTANCE = new SubStationRegistry();
    private final Map<String, TileEntitySubStation> loadedMap = new LinkedHashMap<>();
    private final Map<String, SubStationSnapshot> cache = new LinkedHashMap<>();

    private SubStationRegistry() {
    }

    public void register(TileEntitySubStation te) {
        if (te == null || te.getWorld() == null)
            return;
        loadedMap.put(te.getSubStationId(), te);
        SubStationSnapshot snap = new SubStationSnapshot(
                te.getSubStationId(),
                te.getParentStationId(),
                (float) (te.getPos().getX() + 0.5),
                (float) (te.getPos().getZ() + 0.5),
                te.getWorld().provider.getDimension(),
                te.getMode().name());
        cache.put(snap.id, snap);
        if (!te.getWorld().isRemote) {
            SubStationCacheStore.get(te.getWorld()).upsert(snap);
        }
    }

    public void unregister(String id) {
        loadedMap.remove(id);
    }

    public void loadFromWorld(net.minecraft.world.World world) {
        cache.clear();
        if (world == null || world.isRemote)
            return;
        for (SubStationSnapshot s : SubStationCacheStore.get(world).getAllSnapshots())
            cache.put(s.id, s);
    }

    public void loadFromSnapshots(List<SubStationSnapshot> list) {
        cache.clear();
        for (SubStationSnapshot s : list)
            cache.put(s.id, s);
    }

    public void removeFromCache(net.minecraft.world.World world, String id) {
        loadedMap.remove(id);
        cache.remove(id);
        if (world != null && !world.isRemote)
            SubStationCacheStore.get(world).remove(id);
    }

    public List<SubStationSnapshot> toSnapshots() {
        return new ArrayList<>(cache.values());
    }

    public TileEntitySubStation get(String id) {
        return loadedMap.get(id);
    }

    public SubStationSnapshot findByParent(String parentStationId, SubStationMode mode) {
        if (parentStationId == null)
            return null;
        for (SubStationSnapshot s : cache.values()) {
            if (parentStationId.equals(s.parentStationId) && mode.name().equals(s.mode))
                return s;
        }
        return null;
    }
}