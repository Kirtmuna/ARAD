package jp.apple.arad.limit;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntitySpeedLimitSign extends TileEntity {

    private int speedLimitKmh = 120;
    private int startOffsetBlocks = 0;
    private boolean requireRedstone = false;

    private static int clampSpeed(int kmh) {
        return Math.max(1, Math.min(360, kmh));
    }

    private static int clampOffset(int blocks) {
        return Math.max(0, Math.min(5000, blocks));
    }

    public int getSpeedLimitKmh() {
        return speedLimitKmh;
    }

    public void setSpeedLimitKmh(int kmh) {
        this.speedLimitKmh = clampSpeed(kmh);
        markDirty();
    }

    public int getStartOffsetBlocks() {
        return startOffsetBlocks;
    }

    public void setStartOffsetBlocks(int blocks) {
        this.startOffsetBlocks = clampOffset(blocks);
        markDirty();
    }

    public void setConfig(int kmh, int offsetBlocks, boolean requireRedstone) {
        this.speedLimitKmh = clampSpeed(kmh);
        this.startOffsetBlocks = clampOffset(offsetBlocks);
        this.requireRedstone = requireRedstone;
        markDirty();
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
        nbt.setInteger("SpeedLimitKmh", speedLimitKmh);
        nbt.setInteger("StartOffsetBlocks", startOffsetBlocks);
        nbt.setBoolean("RequireRedstone", requireRedstone);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("SpeedLimitKmh"))
            speedLimitKmh = clampSpeed(nbt.getInteger("SpeedLimitKmh"));
        if (nbt.hasKey("StartOffsetBlocks"))
            startOffsetBlocks = clampOffset(nbt.getInteger("StartOffsetBlocks"));
        if (nbt.hasKey("RequireRedstone"))
            requireRedstone = nbt.getBoolean("RequireRedstone");
    }

    public boolean isRequireRedstone() {
        return requireRedstone;
    }

    public void setRequireRedstone(boolean requireRedstone) {
        this.requireRedstone = requireRedstone;
        markDirty();
    }
}
