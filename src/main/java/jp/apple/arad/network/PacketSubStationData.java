package jp.apple.arad.network;

import io.netty.buffer.ByteBuf;
import jp.apple.arad.data.SubStationSnapshot;
import jp.apple.arad.substation.SubStationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class PacketSubStationData implements IMessage {

    private List<SubStationSnapshot> subStations;

    public PacketSubStationData() {
    }

    public PacketSubStationData(List<SubStationSnapshot> subStations) {
        this.subStations = subStations;
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
        buf.writeShort(subStations.size());
        for (SubStationSnapshot s : subStations) {
            writeStr(buf, s.id);
            writeStr(buf, s.parentStationId);
            buf.writeFloat(s.x);
            buf.writeFloat(s.z);
            buf.writeInt(s.dim);
            writeStr(buf, s.mode);
            buf.writeBoolean(s.turnback);
            buf.writeInt(s.blockX);
            buf.writeInt(s.blockY);
            buf.writeInt(s.blockZ);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readShort();
        subStations = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            subStations.add(new SubStationSnapshot(
                    readStr(buf), readStr(buf),
                    buf.readFloat(), buf.readFloat(),
                    buf.readInt(), readStr(buf), buf.readBoolean(),
                    buf.readInt(), buf.readInt(), buf.readInt()));
        }
    }

    public static final class Handler implements IMessageHandler<PacketSubStationData, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketSubStationData msg, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() ->
                    SubStationRegistry.INSTANCE.loadFromSnapshots(msg.subStations));
            return null;
        }
    }
}