package client;

import pulpcore.scene.Scene2D;

public class PingScene extends Scene2D {

   private ConnectionHandler connection;

   private int time;

   private static final int PING_TIME = 60 * 1000;

   public PingScene(ConnectionHandler connection) {
      this.connection = connection;
      this.time = 0;
   }

   @Override
   public void update(int elapsedTime) {
      time += elapsedTime;

      if (time > PING_TIME) {
         connection.sendPingMessage();
         time = 0;
      }
   }
}
