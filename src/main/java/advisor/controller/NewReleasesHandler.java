package advisor.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class NewReleasesHandler extends RequestHandler {

  NewReleasesHandler(Connection connection, String path) {
    super(connection, path);
  }

  @Override
  List<String> extractFromJson(JsonObject response) {
    List<String> items = new ArrayList<>();
    JsonArray responseItems = response.getAsJsonObject("albums")
        .getAsJsonArray("items");

    for (JsonElement item : responseItems) {
      StringBuilder builder = new StringBuilder();
      JsonObject itemObj = item.getAsJsonObject();
      String albumName = itemObj.get("name").getAsString();
      builder.append(albumName).append("\n");

      builder.append("[");
      String delimiter = ", ";
      for (JsonElement artist : itemObj.getAsJsonArray("artists")) {
        String artistName = artist.getAsJsonObject().get("name").getAsString();
        builder.append(artistName).append(delimiter).append("\n");
      }
      builder.delete(builder.length() - delimiter.length() - 1, builder.length());
      builder.append("]\n");

      String albumUrl = itemObj.getAsJsonObject("external_urls")
          .get("spotify")
          .getAsString();
      builder.append(albumUrl).append("\n");
      items.add(builder.toString());
    }
    return items;
  }

}
