package jp.apple.arad.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.apple.arad.handler.AradKeyHandler;
import jp.apple.arad.handler.ClientAradEventHandler;
import net.minecraftforge.common.MinecraftForge;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        AradKeyHandler.register();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new ClientAradEventHandler());
        MinecraftForge.EVENT_BUS.register(new AradKeyHandler());
    }
}
