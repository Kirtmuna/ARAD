package jp.apple.arad.substation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import java.util.UUID;

public class TileEntitySubStation extends TileEntity implements ITickable {

    private String subStationId = UUID.randomUUID().toString();
    private String parentStationId = "";
    private SubStationMode mode = SubStationMode.STOP_POSITION_CORRECTION;
    private boolean turnback = false;
    private boolean registered = false;

    public boolean isTurnback() {
        return turnback;
    }

    public void setTurnback(boolean turnback) {
        this.turnback = turnback;
        markDirty();
        registered = false;
    }

    public String getSubStationId() {
        return subStationId;
    }

    public String getParentStationId() {
        return parentStationId;
    }

    public void setParentStationId(String parentStationId) {
        this.parentStationId = parentStationId == null ? "" : parentStationId;
        markDirty();
        registered = false;
    }

    public SubStationMode getMode() {
        return mode;
    }

    public void setMode(SubStationMode mode) {
        this.mode = mode == null ? SubStationMode.STOP_POSITION_CORRECTION : mode;
        markDirty();
        registered = false;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote)
            return;
        if (!registered) {
            SubStationRegistry.INSTANCE.register(this);
            registered = true;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (world != null && !world.isRemote)
            SubStationRegistry.INSTANCE.unregister(subStationId);
    }

    @Override
    public void onChunkUnload() {
        if (world != null && !world.isRemote)
            SubStationRegistry.INSTANCE.unregister(subStationId);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("SubStationId", subStationId);
        nbt.setString("ParentStationId", parentStationId);
        nbt.setString("Mode", mode.name());
        nbt.setBoolean("Turnback", turnback);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("SubStationId"))
            subStationId = nbt.getString("SubStationId");
        if (nbt.hasKey("ParentStationId"))
            parentStationId = nbt.getString("ParentStationId");
        if (nbt.hasKey("Mode")) {
            try {
                mode = SubStationMode.valueOf(nbt.getString("Mode"));
            } catch (IllegalArgumentException e) {
                mode = SubStationMode.STOP_POSITION_CORRECTION;
            }
        }
        if (nbt.hasKey("Turnback"))
            turnback = nbt.getBoolean("Turnback");
        registered = false;
    }
}