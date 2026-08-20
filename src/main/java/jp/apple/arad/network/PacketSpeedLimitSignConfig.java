package jp.apple.arad.network;

import io.netty.buffer.ByteBuf;
import jp.apple.arad.limit.TileEntitySpeedLimitSign;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

@SuppressWarnings("unused")
public final class PacketSpeedLimitSignConfig implements IMessage {

    private int x;
    private int y;
    private int z;
    private int speedLimitKmh;
    private int startOffsetBlocks;
    private boolean requireRedstone;

    public PacketSpeedLimitSignConfig() {
    }

    public PacketSpeedLimitSignConfig(int x, int y, int z, int speedLimitKmh, int startOffsetBlocks,
            boolean requireRedstone) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.speedLimitKmh = speedLimitKmh;
        this.startOffsetBlocks = startOffsetBlocks;
        this.requireRedstone = requireRedstone;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(speedLimitKmh);
        buf.writeInt(startOffsetBlocks);
        buf.writeBoolean(requireRedstone);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        speedLimitKmh = buf.readInt();
        startOffsetBlocks = buf.readInt();
        requireRedstone = buf.readBoolean();
    }

    public static final class Handler implements IMessageHandler<PacketSpeedLimitSignConfig, IMessage> {
        @Override
        public IMessage onMessage(PacketSpeedLimitSignConfig msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            WorldServer world = (net.minecraft.world.WorldServer) player.worldObj;

            TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
            if (!(te instanceof TileEntitySpeedLimitSign))
                return null;

            TileEntitySpeedLimitSign sign = (TileEntitySpeedLimitSign) te;
            sign.setConfig(msg.speedLimitKmh, msg.startOffsetBlocks, msg.requireRedstone);

            if (world != null) {
                world.markBlockForUpdate(sign.xCoord, sign.yCoord, sign.zCoord);
            }

            return null;
        }
    }
}
