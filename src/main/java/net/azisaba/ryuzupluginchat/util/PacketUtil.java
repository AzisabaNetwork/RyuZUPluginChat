package net.azisaba.ryuzupluginchat.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class PacketUtil {
    public static void writeVarInt(@NotNull ByteBuf buf, int i) {
        while ((i & 0xFFFFFF80) != 0) {
            buf.writeByte(i & 0x7F | 0x80);
            i >>>= 7;
        }
        buf.writeByte(i);
    }

    public static void writeString(@NotNull ByteBuf buf, @NotNull String s, int max) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > max) {
            throw new EncoderException("String too big (was " + bytes.length + " bytes encoded, max " + max + ")");
        }
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }
}
