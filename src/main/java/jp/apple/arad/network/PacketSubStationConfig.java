package jp.apple.arad.network;

import io.netty.buffer.ByteBuf;
import jp.apple.arad.data.SubStationSnapshot;
import jp.apple.arad.handler.AradPacketHandler;
import jp.apple.arad.substation.SubStationMode;
import jp.apple.arad.substation.SubStationRegistry;
import jp.apple.arad.substation.TileEntitySubStation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class PacketSubStationConfig implements IMessage {

    private int x, y, z;
    private String parentStationId;
    private String mode;

    public PacketSubStationConfig() {
    }

    public PacketSubStationConfig(BlockPos pos, String parentStationId, SubStationMode mode) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.parentStationId = parentStationId == null ? "" : parentStationId;
        this.mode = mode.name();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        byte[] pb = parentStationId.getBytes(StandardCharsets.UTF_8);
        buf.writeByte(Math.min(pb.length, 255));
        buf.writeBytes(pb, 0, Math.min(pb.length, 255));
        byte[] mb = mode.getBytes(StandardCharsets.UTF_8);
        buf.writeByte(Math.min(mb.length, 255));
        buf.writeBytes(mb, 0, Math.min(mb.length, 255));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        int pLen = buf.readByte() & 0xFF;
        byte[] pb = new byte[pLen];
        buf.readBytes(pb);
        parentStationId = new String(pb, StandardCharsets.UTF_8);
        int mLen = buf.readByte() & 0xFF;
        byte[] mb = new byte[mLen];
        buf.readBytes(mb);
        mode = new String(mb, StandardCharsets.UTF_8);
    }

    public static final class Handler implements IMessageHandler<PacketSubStationConfig, IMessage> {
        @Override
        public IMessage onMessage(PacketSubStationConfig msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            WorldServer world = player.getServerWorld();

            world.addScheduledTask(() -> {
                TileEntity te = world.getTileEntity(new BlockPos(msg.x, msg.y, msg.z));
                if (!(te instanceof TileEntitySubStation))
                    return;

                TileEntitySubStation sub = (TileEntitySubStation) te;
                sub.setParentStationId(msg.parentStationId);
                try {
                    sub.setMode(SubStationMode.valueOf(msg.mode));
                } catch (IllegalArgumentException ignored) {
                }
                SubStationRegistry.INSTANCE.register(sub);

                List<SubStationSnapshot> subs = SubStationRegistry.INSTANCE.toSnapshots();
                AradPacketHandler.CHANNEL.sendToAll(new PacketSubStationData(subs));
            });
            return null;
        }
    }
}