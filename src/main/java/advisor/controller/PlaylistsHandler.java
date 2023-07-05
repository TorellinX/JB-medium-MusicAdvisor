package advisor.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistsHandler extends RequestHandler {

  Map<String, String> categories = new HashMap<>();
  private final String categoryName;

  PlaylistsHandler(Connection connection, String path, String categoryName) {
    super(connection, path);
    this.categoryName = categoryName;
  }

  @Override
  List<String> handle() throws IllegalArgumentException {
    JsonObject response = connection.getGetResponse(path + getPathAddition());
    return extractFromJson(response);
  }

  @Override
  List<String> extractFromJson(JsonObject response) {
    List<String> items = new ArrayList<>();
    JsonArray responseItems = response.getAsJsonObject("playlists")
        .getAsJsonArray("items");
    for (JsonElement item : responseItems) {
      StringBuilder builder = new StringBuilder();
      JsonObject itemObj = item.getAsJsonObject();
      String playlistName = itemObj.get("name").getAsString();
      builder.append(playlistName).append("\n");

      String playlistUrl = itemObj.getAsJsonObject("external_urls")
          .get("spotify")
          .getAsString();
      builder.append(playlistUrl).append("\n");
      items.add(builder.toString());
    }
    return items;
  }

  private void retrieveCategories() {
    JsonObject response = connection.getGetResponse(path);
    JsonArray items = response.getAsJsonObject("categories")
        .getAsJsonArray("items");
    for (JsonElement item : items) {
      JsonObject itemObj = item.getAsJsonObject();
      String categoryName = itemObj.get("name").getAsString();
      String categoryId = itemObj.get("id").getAsString();
      categories.put(categoryName, categoryId);
    }
  }

  private String getPathAddition() {
    retrieveCategories();
    String categoryId = categories.get(categoryName);
    if (categoryId == null) {
      throw new IllegalArgumentException("Unknown category name.");
    }
    return "/" + categoryId + "/playlists";
  }

}
