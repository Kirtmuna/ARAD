package jp.apple.arad.substation;

import jp.apple.arad.data.SubStationSnapshot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SubStationCacheStore extends WorldSavedData {

    public static final String DATA_NAME = "arad_substation_cache";

    private final Map<String, SubStationSnapshot> snapshotMap = new LinkedHashMap<>();

    public SubStationCacheStore(String name) {
        super(name);
    }

    public static SubStationCacheStore get(World world) {
        SubStationCacheStore store = (SubStationCacheStore) world.loadData(SubStationCacheStore.class, DATA_NAME);
        if (store == null) {
            store = new SubStationCacheStore(DATA_NAME);
            world.setData(DATA_NAME, store);
        }
        return store;
    }

    public void upsert(SubStationSnapshot snapshot) {
        if (snapshot == null || snapshot.id == null || snapshot.id.isEmpty())
            return;
        snapshotMap.put(snapshot.id, snapshot);
        markDirty();
    }

    public void remove(String id) {
        if (id == null || id.isEmpty())
            return;
        if (snapshotMap.remove(id) != null)
            markDirty();
    }

    public List<SubStationSnapshot> getAllSnapshots() {
        return new ArrayList<>(snapshotMap.values());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        snapshotMap.clear();
        NBTTagList list = nbt.getTagList("subStations", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            SubStationSnapshot s = new SubStationSnapshot(
                    tag.getString("id"),
                    tag.getString("parentStationId"),
                    tag.getFloat("x"),
                    tag.getFloat("z"),
                    tag.getInteger("dim"),
                    tag.hasKey("mode") ? tag.getString("mode") : SubStationMode.STOP_POSITION_CORRECTION.name(),
                    tag.hasKey("turnback") && tag.getBoolean("turnback"));
            if (s.id != null && !s.id.isEmpty())
                snapshotMap.put(s.id, s);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (SubStationSnapshot s : snapshotMap.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("id", s.id == null ? "" : s.id);
            tag.setString("parentStationId", s.parentStationId == null ? "" : s.parentStationId);
            tag.setFloat("x", s.x);
            tag.setFloat("z", s.z);
            tag.setInteger("dim", s.dim);
            tag.setString("mode", s.mode == null ? "" : s.mode);
            tag.setBoolean("turnback", s.turnback);
            list.appendTag(tag);
        }
        nbt.setTag("subStations", list);
        return nbt;
    }
}