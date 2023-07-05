package advisor.controller;

import advisor.model.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Connection {

  private HttpServer server;
  private HttpClient client;
  User user;
  private final String accessServerUri;
  private final String resourceServerUri;
  String redirect_uri;
  private final String CLIENT_ID = "0dab320708b74a8bac373951ff57a761";
  private final String CLIENT_SECRET = "0f8594f9d77644999474363f181106ee";
  private static String code = null;

  Connection(User user, String accessServerUri, String resourceServerUri, String redirect_uri) {
    this.user = user;
    this.accessServerUri = accessServerUri;
    this.resourceServerUri = resourceServerUri;
    this.redirect_uri = redirect_uri;
    client = HttpClient.newBuilder().build();
  }

  String getAuthorizationLink() {
    return accessServerUri + "/authorize?" +
        "client_id=" + CLIENT_ID +
        "&redirect_uri=" + redirect_uri +
        "&response_type=code";
  }


  void getAuthCode() {
    try {
      server = HttpServer.create();
      server.bind(new InetSocketAddress(8080), 0);
    } catch (IOException e) {
      System.out.println("We cannot start the server.");
    }

    server.createContext("/", new HttpHandler() {
          public void handle(HttpExchange exchange) throws IOException {
            String codeNotFound = "Authorization code not found. Try again.";
            String codeFound = "Got the code. Return back to your program.";

            String query;
            int timeout = 10;
            while (timeout > 0) {
              query = exchange.getRequestURI().getQuery();
              timeout--;

              String responseBody;
              if (query == null || !query.contains("code=")) {
                responseBody = codeNotFound;
              } else {
                responseBody = codeFound;
                // TODO: Pattern & Matcher
                Map<String, String> params = queryToMap(query);
                code = params.get("code");
              }

              exchange.sendResponseHeaders(200, responseBody.length());
              exchange.getResponseBody().write(responseBody.getBytes());
              exchange.getResponseBody().close();

              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                throw new RuntimeException();
              }

              if (code != null) {
                server.stop(1);
                user.setAuthorizationCode(code);
                break;
              }
            }

          }
        }
    );

    server.start();
  }


  void sendAccessTokenRequest(String authCode) {
    URI serverUri = URI.create(accessServerUri + "/api/token");
    String requestBody = "grant_type=authorization_code" +
        "&code=" + authCode +
        "&redirect_uri=" + redirect_uri;
    HttpRequest request = HttpRequest.newBuilder()
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()))
        .uri(serverUri)
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();

    HttpResponse<String> response;

    // TODO: try again if bad connection
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        return;
      }
      JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
      String accessToken = responseJson.get("access_token").getAsString();
      if (accessToken == null || accessToken.isBlank()) {
        System.out.println("Empty or null access token");
        return;
      }
      user.setAccessToken(accessToken);
    } catch (IOException |
             InterruptedException e) {
      System.out.println("We cannot access the site. Please, try later.");
    }
  }

  private Map<String, String> queryToMap(String query) {
    if (query == null) {
      return null;
    }
    Map<String, String> params = new HashMap<>();
    for (String param : query.split("&")) {
      String[] entry = param.split("=");
      if (entry.length > 1) {
        params.put(entry[0], entry[1]);
      }
    }
    return params;
  }

  JsonObject getGetResponse(String resourcePath) {
    HttpRequest request = buildGetRequest(resourcePath);
    HttpResponse<String> response;
    JsonObject responseJson = null;
    // TODO: try again if bad connection
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
      responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
      if (response.statusCode() != 200 || responseJson.getAsJsonObject("error") != null) {
        System.out.println(responseJson.getAsJsonObject("error").get("message"));
        return null;
      }
    } catch (IOException | InterruptedException e) {
      System.out.println("We cannot access the site. Please, try later.");
    }
    return responseJson;
  }

  private HttpRequest buildGetRequest(String resourcePath) {
    return HttpRequest.newBuilder()
        .header("Authorization", "Bearer " + user.getAccessToken())
        .uri(URI.create(resourceServerUri + resourcePath))
        .GET()
        .build();
  }


}