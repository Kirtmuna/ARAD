package jp.apple.arad.network;

import io.netty.buffer.ByteBuf;
import jp.apple.arad.section.SectionSlot;
import jp.apple.arad.section.TileEntitySectionMarker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class PacketSectionMarkerConfig implements IMessage {

    private int x, y, z;
    private List<SectionSlot> slots;
    private boolean requireRedstone;

    public PacketSectionMarkerConfig() {
    }

    public PacketSectionMarkerConfig(int x, int y, int z, List<SectionSlot> slots, boolean requireRedstone) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.slots = new ArrayList<>(slots);
        this.requireRedstone = requireRedstone;
    }

    private static void writeStr(ByteBuf buf, String s) {
        byte[] b = (s == null ? "" : s).getBytes(StandardCharsets.UTF_8);
        int len = Math.min(b.length, 255);
        buf.writeByte(len);
        buf.writeBytes(b, 0, len);
    }

    private static String readStr(ByteBuf buf) {
        int len = buf.readByte() & 0xFF;
        byte[] b = new byte[len];
        buf.readBytes(b);
        return new String(b, StandardCharsets.UTF_8);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(requireRedstone);
        buf.writeShort(slots.size());
        for (SectionSlot s : slots) {
            writeStr(buf, s.name);
            buf.writeInt(s.sigX);
            buf.writeInt(s.sigY);
            buf.writeInt(s.sigZ);
            buf.writeInt(s.passX);
            buf.writeInt(s.passZ);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        requireRedstone = buf.readBoolean();
        int count = buf.readShort() & 0xFFFF;
        slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String name = readStr(buf);
            int sX = buf.readInt();
            int sY = buf.readInt();
            int sZ = buf.readInt();
            int passX = buf.readInt();
            int passZ = buf.readInt();
            slots.add(new SectionSlot(name, sX, sY, sZ, passX, passZ));
        }
    }

    public static final class Handler implements IMessageHandler<PacketSectionMarkerConfig, IMessage> {
        @Override
        public IMessage onMessage(PacketSectionMarkerConfig msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            WorldServer world = (net.minecraft.world.WorldServer) player.worldObj;

            TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
            if (!(te instanceof TileEntitySectionMarker))
                return null;
            TileEntitySectionMarker marker = (TileEntitySectionMarker) te;
            marker.setRequireRedstone(msg.requireRedstone);
            marker.setSlots(msg.slots);

            return null;
        }
    }
}