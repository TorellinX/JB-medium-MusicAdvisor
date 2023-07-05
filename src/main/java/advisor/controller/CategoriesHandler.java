package advisor.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesHandler extends RequestHandler {

  Map<String, String> categories = new HashMap<>();

  CategoriesHandler(Connection connection, String path) {
    super(connection, path);
  }

  @Override
  List<String> extractFromJson(JsonObject response) {
    List<String> items = new ArrayList<>();
    JsonArray responseItems = response.getAsJsonObject("categories")
        .getAsJsonArray("items");

    for (JsonElement item : responseItems) {
      StringBuilder builder = new StringBuilder();
      JsonObject itemObj = item.getAsJsonObject();
      String categoryName = itemObj.get("name").getAsString();
      String categoryId = itemObj.get("id").getAsString();
      categories.put(categoryName, categoryId);
      builder.append(categoryName);
      items.add(builder.toString());
    }
    return items;
  }
}
