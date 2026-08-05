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
                te.getMode().name(),
                te.isTurnback(),
                te.getPos().getX(),
                te.getPos().getY(),
                te.getPos().getZ(),
                te.isDoorLeft(),
                te.isDoorRight());
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

    public List<SubStationSnapshot> findAllByParent(String parentStationId) {
        List<SubStationSnapshot> result = new ArrayList<>();
        if (parentStationId == null)
            return result;
        for (SubStationSnapshot s : cache.values()) {
            if (parentStationId.equals(s.parentStationId))
                result.add(s);
        }
        return result;
    }
}