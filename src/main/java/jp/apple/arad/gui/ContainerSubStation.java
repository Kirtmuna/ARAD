package jp.apple.arad.gui;

import jp.apple.arad.substation.TileEntitySubStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class ContainerSubStation extends Container {

    public final TileEntitySubStation subStation;

    public ContainerSubStation(TileEntitySubStation subStation) {
        this.subStation = subStation;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return null;
    }
}