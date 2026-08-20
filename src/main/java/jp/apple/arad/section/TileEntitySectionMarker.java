package jp.apple.arad.section;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.*;

import jp.apple.arad.controller.AutoDriveController;
import jp.apple.arad.controller.AutoDriveManager;

public class TileEntitySectionMarker extends TileEntity {

    private static final double BLOCK_TRIGGER_RADIUS = 3.0;

    private final List<SectionSlot> slots = new ArrayList<SectionSlot>();

    private final Map<Long, Boolean> blockCapturedByFormation = new HashMap<Long, Boolean>();
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
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote)
            return;
        if (slots.isEmpty())
            return;
        if (requireRedstone && !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
            return;

        double blockCx = xCoord + 0.5;
        double blockCz = zCoord + 0.5;
        double triggerR2 = BLOCK_TRIGGER_RADIUS * BLOCK_TRIGGER_RADIUS;

        for (AutoDriveController ctrl : AutoDriveManager.INSTANCE.getAllControllers()) {
            long fid = ctrl.getFormationId();
            double tx = ctrl.getLeadX(worldObj);
            double tz = ctrl.getLeadZ(worldObj);
            if (tx == Double.MIN_VALUE)
                continue;

            double dx = tx - blockCx;
            double dz = tz - blockCz;
            boolean nowOnBlock = (dx * dx + dz * dz) <= triggerR2;

            Boolean lastOnBlock = blockCapturedByFormation.get(fid);
            if (lastOnBlock == null)
                lastOnBlock = false;

            if (nowOnBlock && !lastOnBlock) {
                ctrl.onSectionMarkerPassed(new ArrayList<SectionSlot>(slots));
            }

            blockCapturedByFormation.put(fid, nowOnBlock);
        }

        // 廃止済みコントローラのエントリを削除
        Iterator<Map.Entry<Long, Boolean>> it = blockCapturedByFormation.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Boolean> e = it.next();
            if (AutoDriveManager.INSTANCE.getController(e.getKey()) == null) {
                it.remove();
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (SectionSlot s : slots)
            list.appendTag(s.toNBT());
        nbt.setTag("slots", list);
        nbt.setBoolean("RequireRedstone", requireRedstone);
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
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }
}