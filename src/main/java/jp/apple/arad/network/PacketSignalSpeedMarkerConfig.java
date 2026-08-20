package jp.apple.arad.network;

import io.netty.buffer.ByteBuf;
import jp.apple.arad.section.SectionSlot;
import jp.apple.arad.signalspeed.TileEntitySignalSpeedMarker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

@SuppressWarnings("unused")
public final class PacketSignalSpeedMarkerConfig implements IMessage {

    private int x, y, z;
    private int[] speedMap;

    public PacketSignalSpeedMarkerConfig() {
    }

    public PacketSignalSpeedMarkerConfig(int x, int y, int z, int[] speedMap) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.speedMap = speedMap;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(SectionSlot.MAP_SIZE);
        for (int i = 0; i < SectionSlot.MAP_SIZE; i++) {
            buf.writeInt(speedMap != null && i < speedMap.length ? speedMap[i] : SectionSlot.SPEED_FREE);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        int size = buf.readByte() & 0xFF;
        speedMap = new int[SectionSlot.MAP_SIZE];
        for (int i = 0; i < SectionSlot.MAP_SIZE; i++) {
            speedMap[i] = (i < size) ? buf.readInt() : SectionSlot.SPEED_FREE;
        }
    }

    public static final class Handler
            implements IMessageHandler<PacketSignalSpeedMarkerConfig, IMessage> {
        @Override
        public IMessage onMessage(PacketSignalSpeedMarkerConfig msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            WorldServer world = (net.minecraft.world.WorldServer) player.worldObj;

            TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
            if (!(te instanceof TileEntitySignalSpeedMarker))
                return null;
            ((TileEntitySignalSpeedMarker) te).setSpeedMap(msg.speedMap);

            return null;
        }
    }
}