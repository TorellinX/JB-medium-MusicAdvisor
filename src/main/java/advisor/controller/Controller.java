package advisor.controller;

import advisor.model.Items;
import advisor.model.User;
import advisor.view.View;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Commands:
 * <ul>
 *   <li>"auth" — an authorization link;</li>
 *   <li>"featured" — a list of Spotify-featured playlists with their links fetched from API;</li>
 *   <li>"new" — a list of new albums with artists and links on Spotify;</li>
 *   <li>"categories" — a list of all available categories on Spotify (just their names);</li>
 *   <li>"playlists C_NAME", where C_NAME is the name of category. The list contains playlists of this category and their links on Spotify;</li>
 *   <li>"exit" shuts down the application.</li>
 * </ul>
 */
public class Controller {

  private boolean running = true;
  private View view;
  private final String REDIRECT_URI = "http://localhost:8080";
  private String accessServerUri = "https://accounts.spotify.com";
  private String resourceServerUri = "https://api.spotify.com";
  public int pageLimit = 5;
  Connection connection;
  User user;
  Map<String, String> resources = Map.of(
      "featured", "/v1/browse/featured-playlists",
      "new", "/v1/browse/new-releases",
      "categories", "/v1/browse/categories");
  Items items = Items.getInstance();
  private static final String START_MSG = """
       Commands:
       - auth
       - new
       - featured
       - categories
       - playlists <category>
       - exit
      """;


  public void start(String[] args) {
    readArgs(args);
    view = new View();
    user = new User();
    connection = new Connection(user, accessServerUri, resourceServerUri, REDIRECT_URI);
    view.printMessage(START_MSG);
    while (running) {
      handleUserRequest();
    }
  }

  private void readArgs(String[] args) {
    // TODO: args via JCommander
    String accessToken = null;
    String resourceToken = null;
    int limitToken = 0;
    for (int i = 0; i < args.length - 1; i++) {
      if (Objects.equals("-access", args[i])) {
        accessToken = args[++i];
      }
      if (Objects.equals("-resource", args[i])) {
        resourceToken = args[++i];
      }
      if (Objects.equals("-page", args[i])) {
        try {
          limitToken = Integer.parseInt(args[++i]);
        } catch (NumberFormatException e) {
          view.printErrorMessage("Argument -page is not a number");
        }
      }
    }
    if (accessToken != null && !accessToken.isBlank()) {
      accessServerUri = accessToken;
    }
    if (resourceToken != null && !resourceToken.isBlank()) {
      resourceServerUri = resourceToken;
    }
    if (limitToken != 0) {
      pageLimit = limitToken;
    }
  }

  private void handleUserRequest() {
    String input = InputManager.getInput();
    if (input == null || input.isBlank()) {
      return;
    }
    String[] tokens = input.split("\s+", 2);
    String command = tokens[0].toLowerCase();

    switch (command) {
      case "auth" -> authUser();
      case "new", "categories", "featured" -> handleCommand(tokens);
      case "playlists" -> {
        if ((tokens.length > 1)) {
          handleCommand(tokens);
        } else {
          view.printErrorMessage("Please specify playlists category!");
        }
      }
      case "prev" -> showPreviousPage();
      case "next" -> showNextPage();
      case "exit" -> exit();
      default -> view.printErrorMessage("Error! Illegal command: " + input);
    }
  }

  private void authUser() {
    if (user.hasAuthorizationCode() && user.hasAccessToken()) {
      view.printMessage("User is already authorized");
      return;
    }

    view.printMessage("use this link to request the access code:");
    view.printMessage(connection.getAuthorizationLink());

    connection.getAuthCode();

    // OPEN BROWSER
//    try {
//      if (Desktop.isDesktopSupported()) {
//        Desktop.getDesktop().browse(URI.create(authorizationLink));
//      }
//    } catch (IOException e) {}

    view.printMessage("waiting for code...");

    while (!user.hasAuthorizationCode()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    view.printMessage("code received");
    view.printMessage("Making http request for access_token...");
    connection.sendAccessTokenRequest(user.getAuthorizationCode());

    if (user.hasAccessToken()) {
      view.printMessage("Success!");
    }

  }

  private void handleCommand(String[] tokens) {
    if (!user.hasAuthorizationCode()) {
      view.printMessage("Please, provide access for application.");
      return;
    }
    String command = tokens[0];

    List<String> responseItems = switch (command) {
      case "new" -> new NewReleasesHandler(connection, resources.get("new")).handle();
      case "featured" -> new FeaturedHandler(connection, resources.get("featured")).handle();
      case "categories" -> new CategoriesHandler(connection, resources.get("categories")).handle();
      case "playlists" ->
          new PlaylistsHandler(connection, resources.get("categories"), tokens[1]).handle();
      default -> throw new IllegalArgumentException("Illegal command: " + command);
    };
    items.setItems(responseItems, pageLimit);
    int page = 1;
    view.printList(items.getItems(pageLimit, page), page, items.getLastPageIndex());
  }

  private void showPreviousPage() {
    int previousPage = items.getCurrentPage() - 1;
    if (previousPage < 1) {
      view.printMessage("No more pages.");
      return;
    }
    view.printList(items.getItems(pageLimit, previousPage), previousPage, items.getLastPageIndex());
  }

  private void showNextPage() {
    int nextPage = items.getCurrentPage() + 1;
    if (nextPage > items.getLastPageIndex()) {
      view.printMessage("No more pages.");
      return;
    }
    view.printList(items.getItems(pageLimit, nextPage), nextPage, items.getLastPageIndex());
  }

  private void exit() {
    running = false;
  }


}
