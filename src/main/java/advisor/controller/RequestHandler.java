package advisor.controller;

import com.google.gson.JsonObject;
import java.util.List;

public abstract class RequestHandler {

  final Connection connection;
  final String path;

  RequestHandler(Connection connection, String path) {
    this.connection = connection;
    this.path = path;
  }

  List<String> handle() {
    JsonObject response = connection.getGetResponse(path);
    return extractFromJson(response);
  }

  abstract List<String> extractFromJson(JsonObject response);
}
