package advisor;

import advisor.controller.Controller;

public class Main {

  /**
   * Args:
   * <ul>
   *   <li>"-access"  the Spotify access server point. The default value is https://accounts.spotify.com.</li>
   *   <li>"-resource" the API server path. The default value is https://api.spotify.com.</li>
   *   <li>"-page" a number of entries that should be shown on a page. The default value is 5.</li>
   * </ul>
   *
   * @param args
   */
  public static void main(String[] args) {
    new Controller().start(args);
  }
}