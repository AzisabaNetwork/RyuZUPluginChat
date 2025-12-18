package net.azisaba.ryuzupluginchat.util;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CodecException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unchecked")
public class ViaUtil {
    public static boolean isRGBSupportedVersion(@NotNull Player player) {
        return ((ViaAPI<Player>) Via.getAPI()).getPlayerProtocolVersion(player).newerThanOrEqualTo(ProtocolVersion.v1_16);
    }

    public static void sendJsonMessage(@NotNull Player player, @NotNull String json) {
        ByteBuf buf = Unpooled.buffer();
        PacketUtil.writeVarInt(buf, 0x0F);
        PacketUtil.writeString(buf, json, 262144);
        buf.writeByte(0);
        Objects.requireNonNull(Via.getAPI().getConnection(player.getUniqueId())).transformClientbound(buf, CodecException::new);
        ((ViaAPI<Player>) Via.getAPI()).sendRawPacket(player, buf);
    }
}
