package jp.apple.arad;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import jp.apple.arad.handler.AradGuiHandler;
import jp.apple.arad.handler.AradPacketHandler;
import jp.apple.arad.item.ItemArtpeTrain;
import jp.apple.arad.limit.BlockSpeedLimitSign;
import jp.apple.arad.limit.TileEntitySpeedLimitSign;
import jp.apple.arad.proxy.CommonProxy;
import jp.apple.arad.section.BlockSectionMarker;
import jp.apple.arad.section.TileEntitySectionMarker;
import jp.apple.arad.signalspeed.BlockSignalSpeedMarker;
import jp.apple.arad.signalspeed.TileEntitySignalSpeedMarker;
import jp.apple.arad.station.BlockStation;
import jp.apple.arad.station.TileEntityStation;
import jp.apple.arad.substation.BlockSubStation;
import jp.apple.arad.substation.TileEntitySubStation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = AradCore.MOD_ID, name = AradCore.MOD_NAME, version = "1.0.0-Alpha1", dependencies = "required-after:rtm")
public class AradCore {

    public static final String MOD_ID = "arad";
    public static final String MOD_NAME = "ARAD";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    /** ARADu独自のCreativeTab */
    public static final CreativeTabs tabAradu = new CreativeTabs("aradu_tab") {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(blockStation);
        }
    };

    public static BlockStation blockStation;
    public static BlockSpeedLimitSign blockSpeedLimitSign;
    public static BlockSectionMarker blockSectionMarker;
    public static BlockSignalSpeedMarker blockSignalSpeedMarker;
    public static BlockSubStation blockSubStation;
    public static ItemArtpeTrain itemArtpeTrain;

    @Instance(MOD_ID)
    public static AradCore INSTANCE;

    @SidedProxy(clientSide = "jp.apple.arad.proxy.ClientProxy", serverSide = "jp.apple.arad.proxy.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("[Arad] preInit");

        blockStation = new BlockStation();
        blockStation.setCreativeTab(tabAradu);
        blockSpeedLimitSign = new BlockSpeedLimitSign();
        blockSpeedLimitSign.setCreativeTab(tabAradu);
        blockSectionMarker = new BlockSectionMarker();
        blockSectionMarker.setCreativeTab(tabAradu);
        blockSignalSpeedMarker = new BlockSignalSpeedMarker();
        blockSignalSpeedMarker.setCreativeTab(tabAradu);
        blockSubStation = new BlockSubStation();
        blockSubStation.setCreativeTab(tabAradu);
        itemArtpeTrain = new ItemArtpeTrain();
        itemArtpeTrain.setCreativeTab(tabAradu);

        // ブロック登録
        GameRegistry.registerBlock(blockStation, "station");
        GameRegistry.registerBlock(blockSpeedLimitSign, "speed_limit_sign");
        GameRegistry.registerBlock(blockSectionMarker, "section_marker");
        GameRegistry.registerBlock(blockSignalSpeedMarker, "signal_speed_marker");
        GameRegistry.registerBlock(blockSubStation, "substation");

        // アイテム登録
        GameRegistry.registerItem(itemArtpeTrain, "aradu_train");

        // TileEntity登録
        GameRegistry.registerTileEntity(TileEntityStation.class, "aradu_station");
        GameRegistry.registerTileEntity(TileEntitySpeedLimitSign.class, "aradu_speed_limit_sign");
        GameRegistry.registerTileEntity(TileEntitySectionMarker.class, "aradu_section_marker");
        GameRegistry.registerTileEntity(TileEntitySignalSpeedMarker.class, "aradu_signal_speed_marker");
        GameRegistry.registerTileEntity(TileEntitySubStation.class, "aradu_substation");

        AradPacketHandler.register();
        proxy.preInit(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new AradGuiHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("[Arad] init");
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
