package jp.apple.arad.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import jp.apple.arad.cache.CachedRail;
import jp.apple.arad.cache.RailCacheManager;
import jp.apple.arad.controller.AutoDriveManager;
import jp.apple.arad.data.RouteSnapshot;
import jp.apple.arad.data.ServerData;
import jp.apple.arad.data.StationSnapshot;
import jp.apple.arad.data.SubStationSnapshot;
import jp.apple.arad.network.PacketRailData;
import jp.apple.arad.network.PacketStationRouteData;
import jp.apple.arad.network.PacketSubStationData;
import jp.apple.arad.route.RouteManager;
import jp.apple.arad.station.BlockStation;
import jp.apple.arad.station.StationRegistry;
import jp.apple.arad.station.TileEntityStation;
import jp.apple.arad.substation.BlockSubStation;
import jp.apple.arad.substation.SubStationRegistry;
import jp.apple.arad.substation.TileEntitySubStation;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.List;

public final class CommonAradEventHandler {

    private static void refreshRailChunk(WorldServer ws, int dim, int cx, int cz) {
        Chunk chunk = ws.getChunkFromChunkCoords(cx, cz);
        String key = RailCacheManager.makeChunkKey(dim, cx, cz);
        List<CachedRail> segs = ServerData.INSTANCE.extractFromChunkPublic(chunk);
        AradPacketHandler.CHANNEL.sendToAll(new PacketRailData(key, segs));
    }

    private static boolean isRailRelatedBlock(Block block) {
        if (block instanceof jp.ngt.rtm.rail.BlockLargeRailBase)
            return true;
        // 1.7.10 does not have getRegistryName() reliably, check unlocalized name
        if (block == null)
            return false;
        String name = block.getUnlocalizedName();
        return name != null && (name.contains("rail") || name.contains("marker"));
    }

    private static boolean isPrimaryServerWorld(World world) {
        return world != null
                && !world.isRemote
                && world.provider != null
                && world.provider.dimensionId == 0;
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        World world = event.world;
        if (world.isRemote)
            return;
        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            Chunk chunk = event.getChunk();
            ServerData.INSTANCE.onChunkLoad(chunk, ws);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (!isPrimaryServerWorld(event.world))
            return;
        ServerData.INSTANCE.onServerTick(event.world);
        AutoDriveManager.INSTANCE.tick(event.world);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.world;
        if (!isPrimaryServerWorld(world))
            return;
        StationRegistry.INSTANCE.loadFromWorld(world);
        AutoDriveManager.INSTANCE.restoreFromRouteManager(world);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!isPrimaryServerWorld(event.world))
            return;
        StationRegistry.INSTANCE.clearLoaded();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP))
            return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        ServerData.INSTANCE.onPlayerLogin(player, player.worldObj);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        World world = event.world;
        if (world.isRemote || !(world instanceof WorldServer))
            return;
        Block placed = event.placedBlock;
        if (!isRailRelatedBlock(placed))
            return;

        WorldServer ws = (WorldServer) world;
        int dim = world.provider.dimensionId;
        int cx = event.x >> 4;
        int cz = event.z >> 4;
        refreshRailChunk(ws, dim, cx, cz);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        World world = event.world;
        if (world.isRemote || !(world instanceof WorldServer))
            return;

        Block broken = event.block;
        boolean isRailBlock = isRailRelatedBlock(broken);
        boolean isStationBlock = broken instanceof BlockStation;
        boolean isSubStationBlock = broken instanceof BlockSubStation;
        if (!isRailBlock && !isStationBlock && !isSubStationBlock)
            return;

        WorldServer ws = (WorldServer) world;

        if (isStationBlock) {
            TileEntity te = world.getTileEntity(event.x, event.y, event.z);
            if (te instanceof TileEntityStation) {
                StationRegistry.INSTANCE.removeFromCache(world, ((TileEntityStation) te).getStationId());
                StationRegistry.INSTANCE.removeFromCacheByPos(
                        world,
                        world.provider.dimensionId,
                        event.x,
                        event.z);
            }
            List<StationSnapshot> stations = StationRegistry.INSTANCE.toSnapshots();
            List<RouteSnapshot> routes = RouteManager.get(world).toSnapshots();
            AradPacketHandler.CHANNEL.sendToAll(new PacketStationRouteData(stations, routes));
        }
        if (isSubStationBlock) {
            TileEntity te = world.getTileEntity(event.x, event.y, event.z);
            if (te instanceof TileEntitySubStation) {
                SubStationRegistry.INSTANCE.removeFromCache(
                        world, ((TileEntitySubStation) te).getSubStationId());
            }
            List<SubStationSnapshot> subs = SubStationRegistry.INSTANCE.toSnapshots();
            AradPacketHandler.CHANNEL.sendToAll(new PacketSubStationData(subs));
        }

        if (!isRailBlock)
            return;

        int dim = world.provider.dimensionId;
        int cx = event.x >> 4;
        int cz = event.z >> 4;
        refreshRailChunk(ws, dim, cx, cz);
    }
}
