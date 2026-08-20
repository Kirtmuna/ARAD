package jp.apple.arad.section;

import jp.apple.arad.AradCore;
import jp.apple.arad.handler.AradGuiHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSectionMarker extends BlockContainer {

    public BlockSectionMarker() {
        super(Material.iron);
        setBlockName("aradu_section_marker");
        setHardness(2.0f);
        setResistance(12.0f);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(AradCore.INSTANCE, AradGuiHandler.GUI_SECTION_MARKER, world, x, y, z);
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySectionMarker();
    }
}