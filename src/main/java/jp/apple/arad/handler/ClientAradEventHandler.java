package jp.apple.arad.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.apple.arad.data.MapData;
import net.minecraftforge.event.world.WorldEvent;

@SideOnly(Side.CLIENT)
public final class ClientAradEventHandler {
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.world.isRemote)
            return;
        MapData.INSTANCE.onWorldJoin();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!event.world.isRemote)
            return;
        MapData.INSTANCE.onWorldLeave();
    }
}