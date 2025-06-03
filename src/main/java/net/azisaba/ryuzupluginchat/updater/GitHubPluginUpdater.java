package net.azisaba.ryuzupluginchat.updater;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.semver4j.Semver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.ryuzupluginchat.RyuZUPluginChat;
import org.bukkit.plugin.AuthorNagException;

@Getter
@RequiredArgsConstructor
public class GitHubPluginUpdater {

  private final RyuZUPluginChat plugin;

  private final String updateCheckURL =
      "https://api.github.com/repos/AzisabaNetwork/RyuZUPluginChat/releases/latest";
  private final String currentVersion;

  private UpdateStatus status = UpdateStatus.UNKNOWN;
  private String latestFileDownloadUrl;
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * 更新があるかどうかの確認をする
   *
   * @return 成功した場合true, 失敗した場合false
   */
  public boolean checkUpdate() {
    if (status == UpdateStatus.ALREADY_UPDATED) {
      return true;
    }
    status = UpdateStatus.FAILED;

    URL url;
    try {
      url = new URL(updateCheckURL);
    } catch (MalformedURLException e) {
      throw new AuthorNagException("Update check url is invalid.");
    }

    Map<String, Object> data;
    try {
      String response = readUrlAsString(url);
      data = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});
    } catch (IOException e) {
      plugin.getSLF4JLogger().error("Failed to read data from update check url content", e);
      return false;
    }

    String branch = getAsString(data, "target_commitish");
    String nextTag = getAsString(data, "tag_name");

    if (branch == null || nextTag == null) {
      return false;
    }

    if (!branch.equals("main") && !branch.equals("master")) {
      status = UpdateStatus.LATEST;
      return true;
    }

    // draft / pre-release の場合は更新しない
    if (!isValidRelease(data)) {
      status = UpdateStatus.LATEST;
      return true;
    }

    Semver currentVersion;
    Semver nextVersion;

    if (getCurrentVersion().toLowerCase().startsWith("v")) {
      currentVersion = new Semver(getCurrentVersion().substring(1));
    } else {
      currentVersion = new Semver(getCurrentVersion());
    }

    if (nextTag.toLowerCase().startsWith("v")) {
      nextVersion = new Semver(nextTag.substring(1));
    } else {
      nextVersion = new Semver(nextTag);
    }

    // Majorバージョンが変わっている場合は更新しない
    if (!Objects.equals(currentVersion.getMajor(), nextVersion.getMajor())) {
      status = UpdateStatus.MAJOR_VERSION_CHANGED;
      return true;
    }

    if (!nextVersion.isGreaterThan(currentVersion)) {
      status = UpdateStatus.LATEST;
      return true;
    }

    List<Map<String, Object>> assets = digAsMapList(data, "assets");
    if (assets == null || assets.isEmpty()) {
      // リリースではないGitHubのreleaseを書き込んだ可能性があるので一応最新としておく
      status = UpdateStatus.LATEST;
      return true;
    }

    for (Map<String, Object> map : assets) {
      String fileName = getAsString(map, "name");
      if (fileName == null || !fileName.equals("RyuZUPluginChat.jar")) {
        continue;
      }
      latestFileDownloadUrl = getAsString(map, "browser_download_url");
      break;
    }

    if (latestFileDownloadUrl != null) {
      status = UpdateStatus.OUTDATED;
    } else {
      status = UpdateStatus.FAILED;
    }
    return true;
  }

  public boolean executeDownloadLatestJar(File file) {
    if (status != UpdateStatus.OUTDATED || latestFileDownloadUrl == null) {
      throw new IllegalStateException(
          "Latest update not found or checkUpdate() hasn't called yet. Or failed to get the latest version download URL.");
    }

    URL url;
    try {
      url = new URL(latestFileDownloadUrl);
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("Download URL is invalid.");
    }

    try (InputStream in = url.openStream();
        ReadableByteChannel rbc = Channels.newChannel(in);
        FileOutputStream fos = new FileOutputStream(file)) {
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

      status = UpdateStatus.ALREADY_UPDATED;
      return true;
    } catch (IOException e) {
      plugin.getSLF4JLogger().error("Failed to download latest jar file", e);
      return false;
    }
  }

  private boolean isValidRelease(Map<String, Object> map) {
    boolean preRelease = map.containsKey("prerelease") && (boolean) map.get("prerelease");
    boolean draft = map.containsKey("draft") && (boolean) map.get("draft");

    return !preRelease && !draft;
  }

  private String readUrlAsString(URL url) throws IOException {
    try (InputStream in = url.openStream()) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> dig(Map<String, Object> map, String key) {
    Object obj = map.getOrDefault(key, null);
    if (obj instanceof Map) {
      return (Map<String, Object>) obj;
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> digAsMapList(Map<String, Object> map, String key) {
    Object obj = map.getOrDefault(key, null);
    if (obj instanceof List) {
      return (List<Map<String, Object>>) obj;
    }

    return null;
  }

  private String getAsString(Map<String, Object> map, String key) {
    Object obj = map.get(key);
    if (obj instanceof String) {
      return (String) obj;
    }
    return null;
  }
}
