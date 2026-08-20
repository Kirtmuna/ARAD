package jp.apple.arad.substation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TileEntitySubStation extends TileEntity {

    private String subStationId = UUID.randomUUID().toString();
    private String parentStationId = "";
    private SubStationMode mode = SubStationMode.STOP_POSITION_CORRECTION;
    private boolean turnback = false;
    private boolean doorLeft = true;
    private boolean doorRight = true;
    private boolean registered = false;

    public boolean isTurnback() {
        return turnback;
    }

    public void setTurnback(boolean turnback) {
        this.turnback = turnback;
        markDirty();
        registered = false;
    }

    public boolean isDoorLeft() {
        return doorLeft;
    }

    public void setDoorLeft(boolean v) {
        this.doorLeft = v;
        markDirty();
        registered = false;
    }

    public boolean isDoorRight() {
        return doorRight;
    }

    public void setDoorRight(boolean v) {
        this.doorRight = v;
        markDirty();
        registered = false;
    }

    public byte getDoorData() {
        if (doorLeft && doorRight)
            return 3;
        if (doorRight)
            return 1;
        if (doorLeft)
            return 2;
        return 0;
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
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote)
            return;
        if (!registered) {
            SubStationRegistry.INSTANCE.register(this);
            registered = true;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (worldObj != null && !worldObj.isRemote)
            SubStationRegistry.INSTANCE.unregister(subStationId);
    }

    @Override
    public void onChunkUnload() {
        if (worldObj != null && !worldObj.isRemote)
            SubStationRegistry.INSTANCE.unregister(subStationId);
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

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("SubStationId", subStationId);
        nbt.setString("ParentStationId", parentStationId);
        nbt.setString("Mode", mode.name());
        nbt.setBoolean("Turnback", turnback);
        nbt.setBoolean("DoorLeft", doorLeft);
        nbt.setBoolean("DoorRight", doorRight);
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
        if (nbt.hasKey("DoorLeft"))
            doorLeft = nbt.getBoolean("DoorLeft");
        if (nbt.hasKey("DoorRight"))
            doorRight = nbt.getBoolean("DoorRight");
        registered = false;
    }
}