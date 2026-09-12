package jp.apple.arad.station;

import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class StationChunkValidationHandler {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        StationRegistry.INSTANCE.validateChunk(event.getWorld(), event.getChunk());
    }
}