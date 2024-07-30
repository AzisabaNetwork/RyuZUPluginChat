package net.azisaba.ryuzupluginchat.util;

import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Reqwest {
    public static @NotNull String get(@NotNull String url, @NotNull Map<String, String> headers) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            headers.forEach(connection::setRequestProperty);
            connection.connect();
            try {
                return new String(ByteStreams.toByteArray(connection.getInputStream()), StandardCharsets.UTF_8);
            } catch (Exception e) {
                return new String(ByteStreams.toByteArray(connection.getErrorStream()), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull String post(@NotNull String data, @NotNull String url, @NotNull Map<String, String> headers) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            headers.forEach(connection::setRequestProperty);
            connection.setDoOutput(true);
            connection.connect();
            connection.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
            try {
                return new String(ByteStreams.toByteArray(connection.getInputStream()), StandardCharsets.UTF_8);
            } catch (Exception e) {
                return new String(ByteStreams.toByteArray(connection.getErrorStream()), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
