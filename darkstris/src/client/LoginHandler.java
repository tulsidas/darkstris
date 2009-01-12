package client;

public interface LoginHandler extends BaseHandler {

   void loggedIn();

   void loginFailed(String reason);
}
