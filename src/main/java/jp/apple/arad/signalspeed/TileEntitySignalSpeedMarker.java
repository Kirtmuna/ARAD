package jp.apple.arad.signalspeed;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jp.apple.arad.controller.AutoDriveController;
import jp.apple.arad.controller.AutoDriveManager;
import jp.apple.arad.section.SectionSlot;

public class TileEntitySignalSpeedMarker extends TileEntity {

    private static final double TRIGGER_RADIUS = 3.0;
    private final Map<Long, Boolean> triggerState = new HashMap<Long, Boolean>();
    private int[] speedMap = SectionSlot.defaultSpeedMap();

    private static int[] build(int[] src) {
        int[] map = SectionSlot.defaultSpeedMap();
        if (src == null)
            return map;
        for (int i = 0; i < SectionSlot.MAP_SIZE && i < src.length; i++)
            map[i] = (src[i] < 0) ? SectionSlot.SPEED_FREE : Math.min(src[i], 9999);
        return map;
    }

    public int[] getSpeedMap() {
        return speedMap.clone();
    }

    public void setSpeedMap(int[] src) {
        this.speedMap = build(src);
        triggerState.clear();
        markDirty();
        if (worldObj != null && !worldObj.isRemote)
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote)
            return;

        double bx = xCoord + 0.5;
        double bz = zCoord + 0.5;
        double r2 = TRIGGER_RADIUS * TRIGGER_RADIUS;

        for (AutoDriveController ctrl : AutoDriveManager.INSTANCE.getAllControllers()) {
            long fid = ctrl.getFormationId();
            double tx = ctrl.getLeadX(worldObj);
            double tz = ctrl.getLeadZ(worldObj);
            if (tx == Double.MIN_VALUE)
                continue;

            double dx = tx - bx, dz = tz - bz;
            boolean nowOn = (dx * dx + dz * dz) <= r2;
            Boolean lastOn = triggerState.containsKey(fid) ? triggerState.get(fid) : false;

            if (nowOn && !lastOn) {
                ctrl.onSignalSpeedMapReceived(speedMap.clone());
            }
            triggerState.put(fid, nowOn);
        }

        Iterator<Map.Entry<Long, Boolean>> it = triggerState.entrySet().iterator();
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
        nbt.setIntArray("speedMap", speedMap);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("speedMap"))
            speedMap = build(nbt.getIntArray("speedMap"));
        triggerState.clear();
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound nbt = pkt.func_148857_g();
        if (nbt.hasKey("speedMap"))
            speedMap = build(nbt.getIntArray("speedMap"));
        triggerState.clear();
    }
}