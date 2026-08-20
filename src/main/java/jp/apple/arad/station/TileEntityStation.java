package jp.apple.arad.station;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.Arrays;
import java.util.UUID;

import jp.apple.arad.item.ItemArtpeTrain;

public class TileEntityStation extends TileEntity implements IInventory {

    private String stationId = UUID.randomUUID().toString();
    private String stationName = "新しい駅";
    private boolean registered = false;

    private static final int FORMATION_SLOT_COUNT = 3;
    private final ItemStack[] formationItems = new ItemStack[FORMATION_SLOT_COUNT];
    private int nextFormationSlot = 0;

    private boolean doorLeft = true;
    private boolean doorRight = true;
    private boolean spawnReversed = false;
    private boolean turnback = false;
    private int dwellTimeTicks = 400;

    public TileEntityStation() {
        Arrays.fill(formationItems, null);
    }

    public String getStationId() {
        return stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String name) {
        this.stationName = (name == null || name.isEmpty()) ? "駅" : name;
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

    public boolean isSpawnReversed() {
        return spawnReversed;
    }

    public void setSpawnReversed(boolean v) {
        this.spawnReversed = v;
        markDirty();
        registered = false;
    }

    public boolean isTurnback() {
        return turnback;
    }

    public void setTurnback(boolean v) {
        this.turnback = v;
        markDirty();
        registered = false;
    }

    public int getDwellTimeTicks() {
        return dwellTimeTicks;
    }

    public void setDwellTimeTicks(int ticks) {
        this.dwellTimeTicks = Math.max(20, ticks);
        markDirty();
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

    /** 次の有効な編成アイテムを返す（ラウンドロビン） */
    public ItemStack getFormationItem() {
        for (int i = 0; i < FORMATION_SLOT_COUNT; i++) {
            int idx = (nextFormationSlot + i) % FORMATION_SLOT_COUNT;
            if (formationItems[idx] != null) {
                nextFormationSlot = (idx + 1) % FORMATION_SLOT_COUNT;
                return formationItems[idx];
            }
        }
        return null;
    }

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote)
            return;
        if (!registered) {
            StationRegistry.INSTANCE.register(this);
            registered = true;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (worldObj != null && !worldObj.isRemote)
            StationRegistry.INSTANCE.unregister(stationId);
    }

    @Override
    public void onChunkUnload() {
        if (worldObj != null && !worldObj.isRemote)
            StationRegistry.INSTANCE.unregister(stationId);
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
        nbt.setString("StationId", stationId);
        nbt.setString("StationName", stationName);
        nbt.setBoolean("DoorLeft", doorLeft);
        nbt.setBoolean("DoorRight", doorRight);
        nbt.setBoolean("SpawnReversed", spawnReversed);
        nbt.setBoolean("Turnback", turnback);
        nbt.setInteger("DwellTicks", dwellTimeTicks);
        nbt.setInteger("NextFormationSlot", nextFormationSlot);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < FORMATION_SLOT_COUNT; i++) {
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setInteger("Slot", i);
            if (formationItems[i] != null)
                formationItems[i].writeToNBT(itemTag);
            itemList.appendTag(itemTag);
        }
        nbt.setTag("FormationItems", itemList);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("StationId"))
            stationId = nbt.getString("StationId");
        if (nbt.hasKey("StationName"))
            stationName = nbt.getString("StationName");
        if (nbt.hasKey("DoorLeft"))
            doorLeft = nbt.getBoolean("DoorLeft");
        if (nbt.hasKey("DoorRight"))
            doorRight = nbt.getBoolean("DoorRight");
        if (nbt.hasKey("SpawnReversed"))
            spawnReversed = nbt.getBoolean("SpawnReversed");
        if (nbt.hasKey("Turnback"))
            turnback = nbt.getBoolean("Turnback");
        if (nbt.hasKey("DwellTicks"))
            dwellTimeTicks = Math.max(20, nbt.getInteger("DwellTicks"));
        if (nbt.hasKey("NextFormationSlot"))
            nextFormationSlot = nbt.getInteger("NextFormationSlot") % FORMATION_SLOT_COUNT;

        Arrays.fill(formationItems, null);
        if (nbt.hasKey("FormationItems")) {
            NBTTagList itemList = nbt.getTagList("FormationItems", 10);
            for (int i = 0; i < itemList.tagCount(); i++) {
                NBTTagCompound itemTag = itemList.getCompoundTagAt(i);
                int slot = itemTag.getInteger("Slot");
                if (slot >= 0 && slot < FORMATION_SLOT_COUNT)
                    formationItems[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        } else if (nbt.hasKey("FormationItem")) {
            formationItems[0] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("FormationItem"));
        }
        registered = false;
    }

    // IInventory 実装
    @Override
    public int getSizeInventory() {
        return FORMATION_SLOT_COUNT;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return (index >= 0 && index < FORMATION_SLOT_COUNT) ? formationItems[index] : null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index < 0 || index >= FORMATION_SLOT_COUNT || formationItems[index] == null)
            return null;
        ItemStack result = formationItems[index].splitStack(count);
        if (formationItems[index].stackSize == 0)
            formationItems[index] = null;
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (index < 0 || index >= FORMATION_SLOT_COUNT)
            return null;
        ItemStack old = formationItems[index];
        formationItems[index] = null;
        markDirty();
        return old;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= FORMATION_SLOT_COUNT)
            return;
        formationItems[index] = stack;
        if (stack != null && stack.stackSize > 1)
            stack.stackSize = 1;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return stationName;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        // ARAD内包のItemArtpeTrainクラスかどうかを判定
        return stack != null && stack.getItem() instanceof ItemArtpeTrain;
    }
}
