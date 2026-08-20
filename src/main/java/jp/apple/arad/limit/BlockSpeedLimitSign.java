package jp.apple.arad.limit;

import jp.apple.arad.AradCore;
import jp.apple.arad.handler.AradGuiHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSpeedLimitSign extends BlockContainer {

    public BlockSpeedLimitSign() {
        super(Material.iron);
        setBlockName("aradu_speed_limit_sign");
        setHardness(2.0f);
        setResistance(12.0f);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(AradCore.INSTANCE, AradGuiHandler.GUI_SPEED_LIMIT, world, x, y, z);
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySpeedLimitSign();
    }
}
