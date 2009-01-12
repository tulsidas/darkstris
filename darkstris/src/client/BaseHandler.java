package client;

/**
 * Handler from which all Handler interfaces must be subinterface
 */
public interface BaseHandler {
   void log(String str);

   void incomingChat(String str);
}
