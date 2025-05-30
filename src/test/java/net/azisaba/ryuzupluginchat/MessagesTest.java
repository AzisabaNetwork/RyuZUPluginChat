package net.azisaba.ryuzupluginchat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.azisaba.ryuzupluginchat.localization.Messages;
import org.intellij.lang.annotations.Subst;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MessagesTest {
  private static final Set<Locale> LOCALES_TO_TEST = new HashSet<>(Collections.singleton(Locale.JAPANESE));

  @Test
  public void test() throws IOException {
    Messages.load();

    // check for missing translations

    Set<String> errors = new HashSet<>();
    for (Locale locale : LOCALES_TO_TEST) {
      if (Messages.getInstance(locale) == Messages.getInstance(Locale.ENGLISH)) {
        errors.add("Missing translation file for " + locale.toLanguageTag());
      }
    }

    try (InputStream in = Messages.class.getResourceAsStream("/messages_en.json")) {
      if (in == null) {
        throw new RuntimeException("messages_en.json is missing");
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
        JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
          @Subst("key") String key = entry.getKey();
          for (Locale locale : LOCALES_TO_TEST) {
            if (Messages.getInstance(locale).get(key).equals(key)) {
              errors.add("Missing translation for " + key + " in " + locale);
            }
          }
        }
      }
    }
    if (!errors.isEmpty()) {
      for (String error : errors) {
        System.err.println(error);
      }
      throw new RuntimeException(errors.size() + " error(s) found");
    }
  }
}
