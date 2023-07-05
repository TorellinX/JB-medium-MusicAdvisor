package advisor.model;

public class User {

  private String authorizationCode;
  private String accessToken;


  public boolean hasAuthorizationCode() {
    return authorizationCode != null && !authorizationCode.isBlank();
  }

  public String getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAuthorizationCode(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public boolean hasAccessToken() {
    return accessToken != null && !accessToken.isBlank();
  }
}
