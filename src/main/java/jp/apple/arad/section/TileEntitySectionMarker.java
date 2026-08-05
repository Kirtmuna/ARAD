package jp.apple.arad.section;

import jp.apple.arad.controller.AutoDriveController;
import jp.apple.arad.controller.AutoDriveManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import java.util.*;

public class TileEntitySectionMarker extends TileEntity implements ITickable {

    private static final double BLOCK_TRIGGER_RADIUS = 3.0;

    private final List<SectionSlot> slots = new ArrayList<>();

    private final Map<Long, Boolean> blockCapturedByFormation = new HashMap<>();
    private boolean requireRedstone = false;

    public List<SectionSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    public void setSlots(List<SectionSlot> newSlots) {
        slots.clear();
        for (SectionSlot s : newSlots)
            slots.add(s.copy());
        blockCapturedByFormation.clear();
        markDirty();
        if (world != null && !world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    public void addSlot(SectionSlot slot) {
        slots.add(slot.copy());
        blockCapturedByFormation.clear();
        markDirty();
    }

    public void removeSlot(int index) {
        if (index >= 0 && index < slots.size()) {
            slots.remove(index);
            blockCapturedByFormation.clear();
            markDirty();
        }
    }

    public boolean isRequireRedstone() {
        return requireRedstone;
    }

    public void setRequireRedstone(boolean requireRedstone) {
        this.requireRedstone = requireRedstone;
        markDirty();
        if (world != null && !world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @Override
    public void update() {
        World world = getWorld();
        if (world == null || world.isRemote)
            return;
        if (slots.isEmpty())
            return;
        if (requireRedstone && !world.isBlockPowered(pos))
            return;

        double blockCx = pos.getX() + 0.5;
        double blockCz = pos.getZ() + 0.5;
        double triggerR2 = BLOCK_TRIGGER_RADIUS * BLOCK_TRIGGER_RADIUS;

        for (AutoDriveController ctrl : AutoDriveManager.INSTANCE.getAllControllers()) {
            long fid = ctrl.getFormationId();
            double tx = ctrl.getLeadX(world);
            double tz = ctrl.getLeadZ(world);
            if (tx == Double.MIN_VALUE)
                continue;

            double dx = tx - blockCx;
            double dz = tz - blockCz;
            boolean nowOnBlock = (dx * dx + dz * dz) <= triggerR2;

            Boolean lastOnBlock = blockCapturedByFormation.get(fid);
            if (lastOnBlock == null)
                lastOnBlock = false;

            if (nowOnBlock && !lastOnBlock) {

                ctrl.onSectionMarkerPassed(new ArrayList<>(slots));
            }

            blockCapturedByFormation.put(fid, nowOnBlock);
        }

        blockCapturedByFormation.entrySet().removeIf(e -> AutoDriveManager.INSTANCE.getController(e.getKey()) == null);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (SectionSlot s : slots)
            list.appendTag(s.toNBT());
        nbt.setTag("slots", list);
        nbt.setBoolean("RequireRedstone", requireRedstone);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        slots.clear();
        blockCapturedByFormation.clear();
        if (nbt.hasKey("slots")) {
            NBTTagList list = nbt.getTagList("slots", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                slots.add(SectionSlot.fromNBT(list.getCompoundTagAt(i)));
            }
        }
        if (nbt.hasKey("RequireRedstone"))
            requireRedstone = nbt.getBoolean("RequireRedstone");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}