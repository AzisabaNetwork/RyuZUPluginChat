package net.azisaba.ryuzupluginchat.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.azisaba.ryuzupluginchat.config.RPCConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OpenAIUtils {
    public static @NotNull Map<String, String> getOpenAIRequestHeaders(@NotNull RPCConfig config) {
        Map<String, String> map = new HashMap<>();
        map.put("Content-Type", "application/json");
        map.put("Authorization", "Bearer " + config.getTranslatorOpenAIApiKey());
        if (config.getTranslatorOpenAIOrganization() != null) {
            map.put("OpenAI-Organization", config.getTranslatorOpenAIOrganization());
        }
        return map;
    }

    public static @NotNull String ask(@NotNull RPCConfig config, @NotNull String language, @Nullable String user, @NotNull String message) {
        JsonObject obj = getRequestBody(config.getTranslatorPrompt().replace("%language%", language), user, message);
        JsonObject responseObject = new Gson().fromJson(Reqwest.post(obj.toString(), "https://api.openai.com/v1/chat/completions", getOpenAIRequestHeaders(config)), JsonObject.class);
        return responseObject.getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
    }

    private static @NotNull JsonObject getRequestBody(@NotNull String prompt, @Nullable String user, @NotNull String message) {
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", prompt);
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        JsonArray messages = new JsonArray();
        messages.add(systemMessage);
        messages.add(userMessage);
        JsonObject obj = new JsonObject();
        obj.addProperty("model", "gpt-4o");
        obj.addProperty("user", user);
        obj.addProperty("max_tokens", 250);
        obj.add("messages", messages);
        return obj;
    }
}
