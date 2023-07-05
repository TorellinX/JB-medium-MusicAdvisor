package advisor.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class FeaturedHandler extends RequestHandler {

  FeaturedHandler(Connection connection, String path) {
    super(connection, path);
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
}
