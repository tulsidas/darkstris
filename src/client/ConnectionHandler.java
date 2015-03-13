package client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import common.Commands;
import common.FigureInfo;
import common.LobbyData;
import common.PlayerInfo;
import common.Commands.Command;

public class ConnectionHandler implements SimpleClientListener,
      ClientChannelListener {

   private long msgTimeStamp;

   private static final int PING_TIMEOUT = 60 * 1000;

   private String userName;

   private char[] password;

   private LoginHandler loginHandler;

   private LobbyHandler lobbyHandler;

   private GameHandler gameHandler;

   private SimpleClient client;

   // the current Channel I'm into (lobby or game)
   private ClientChannel channel;

   public ConnectionHandler() {
      client = new SimpleClient(this);
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public void setPassword(char[] password) {
      this.password = password;
   }

   // /////////////////////
   // handler injectors
   // /////////////////////
   public void setLoginHandler(LoginHandler loginHandler) {
      this.loginHandler = loginHandler;
      this.gameHandler = null;
      this.lobbyHandler = null;
   }

   public void setLobbyHandler(LobbyHandler lobbyHandler) {
      this.loginHandler = null;
      this.lobbyHandler = lobbyHandler;
      this.gameHandler = null;
   }

   public void setGameHandler(GameHandler gameHandler) {
      this.loginHandler = null;
      this.lobbyHandler = null;
      this.gameHandler = gameHandler;
   }

   /**
    * Disconnects from the server
    */
   public void disconnect() {
      client.logout(false);
   }

   // /////////////////////
   // SimpleClient delegate
   // /////////////////////
   public void login(Properties props) throws IOException {
      client.login(props);
   }

   // /////////////////////
   // SimpleClientListener
   // /////////////////////
   @Override
   public PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(userName, password);
   }

   @Override
   public void loggedIn() {
      loginHandler.loggedIn();
   }

   @Override
   public void loginFailed(String reason) {
      loginHandler.loginFailed(reason);
   }

   @Override
   public void disconnected(boolean forced, String reason) {
      log("disconnected: " + reason);
   }

   @Override
   public ClientChannelListener joinedChannel(ClientChannel channel) {
      log("joinedChannel: " + channel.getName());

      this.channel = channel;

      return this;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void receivedMessage(ByteBuffer buf) {
      int encodedCmd = buf.getInt();
      Command cmd = Commands.decode(encodedCmd);

      if (cmd == Command.LOBBY_DATA) {
         try {
            LobbyData data = (LobbyData) getObject(buf);
            lobbyHandler.setLobbyData(data.getPlayers(), data.getRooms());
         }
         catch (IOException e) {
            log(e.getMessage());
         }
      }
      else if (cmd == Command.ROOM_JOINED) {
         // I've successfully joined a room
         long id = buf.getLong();
         int color = buf.getInt();
         Collection<PlayerInfo> currentPlayers = new ArrayList<PlayerInfo>();
         try {
            currentPlayers = (Collection<PlayerInfo>) getObject(buf);
         }
         catch (IOException e) {
            log(e.getMessage());
         }

         // FIXME exception if double-joined
         lobbyHandler.roomJoined(id, color, currentPlayers);
      }
      else if (cmd == Command.GAME_OWNER) {
         gameHandler.setGameOwner();
      }
   }

   @Override
   public void reconnected() {
      log("reconnected");
   }

   @Override
   public void reconnecting() {
      log("reconnecting");
   }

   // Utility methods
   private void log(String str) {
      getBaseHandler().log(str);
   }

   public BaseHandler getBaseHandler() {
      // one of them must be non-null
      if (lobbyHandler != null) {
         return lobbyHandler;
      }
      else if (gameHandler != null) {
         return gameHandler;
      }
      else {
         return loginHandler;
      }
   }

   /**
    * Retrieves a serialized object from the given buffer.
    * 
    * @param data
    *           the encoded object to retrieve
    */
   private static Object getObject(ByteBuffer data) throws IOException {
      try {
         byte[] bytes = new byte[data.remaining()];
         data.get(bytes);

         ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bin);
         return ois.readObject();
      }
      catch (ClassNotFoundException cnfe) {
         throw new IOException(cnfe.getMessage());
      }
   }

   /**
    * Retrieves a serialized object from the given buffer. The first int is the
    * length of the object
    * 
    * @param data
    *           the encoded object to retrieve
    */
   private static Object getPrefixedObject(ByteBuffer data) throws IOException {
      try {
         int length = data.getInt();
         byte[] bytes = new byte[length];
         data.get(bytes);

         ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bin);
         return ois.readObject();
      }
      catch (ClassNotFoundException cnfe) {
         throw new IOException(cnfe.getMessage());
      }
   }

   /**
    * Retrieves a FigureInfo object from the given buffer.
    */
   private static FigureInfo getFigureInfo(ByteBuffer data) throws IOException {
      int type = data.getInt();
      int x = data.getInt();
      int y = data.getInt();
      int rotation = data.getInt();
      int color = data.getInt();
      return new FigureInfo(type, x, y, rotation, color);
   }

   // //////////////////////
   // ClientChannelListener
   // //////////////////////

   @Override
   public void leftChannel(ClientChannel ch) {
      log("leftChannel " + ch.getName());
      if (ch.equals(channel)) {
         channel = null;
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public void receivedMessage(ClientChannel ch, ByteBuffer buf) {
      int encodedCmd = buf.getInt();
      Command cmd = Commands.decode(encodedCmd);

      // COMMON MESSAGES
      if (cmd == Command.CHAT) {
         getBaseHandler().incomingChat(getString(buf));
      }

      // LOBBY MESSAGES
      if (ch.getName().equals("lobbyChannel") && lobbyHandler != null) {
         if (cmd == Command.USER_JOINED) {
            lobbyHandler.addPlayer(getString(buf));
         }
         else if (cmd == Command.USER_LEFT) {
            lobbyHandler.removePlayer(getString(buf));
         }
         else if (cmd == Command.ROOM_CREATED) {
            long id = buf.getLong();
            int maxPlayers = buf.getInt();
            int color = buf.getInt();
            String name = getString(buf);

            lobbyHandler.roomCreated(id, maxPlayers,
                  new PlayerInfo(name, color));
         }
         else if (cmd == Command.ROOM_DROPPED) {
            long id = buf.getLong();
            lobbyHandler.roomDropped(id);
         }
         else if (cmd == Command.ROOM_FULL) {
            long id = buf.getLong();
            lobbyHandler.roomFull(id);
         }
         else if (cmd == Command.GAME_START) {
            long id = buf.getLong();
            lobbyHandler.gameStarted(id);
         }
      }

      // MIXED MESSAGES
      if (cmd == Command.ROOM_JOINED) {
         // a user joined a room
         // we could be in the lobby or in the game
         long id = buf.getLong();
         int color = buf.getInt();
         String name = getString(buf);
         PlayerInfo player = new PlayerInfo(name, color);

         if (lobbyHandler != null) {
            lobbyHandler.roomJoined(id, player);
         }
         if (gameHandler != null) {
            gameHandler.roomJoined(player);
         }
      }

      // GAME MESSAGES
      if (ch.getName().startsWith("room") && gameHandler != null) {
         if (cmd == Command.GAME_START) {
            // game on!
            try {
               int boardWidth = buf.getInt();
               Collection<FigureInfo> figures = (Collection<FigureInfo>) getObject(buf);
               gameHandler.gameStart(boardWidth, figures);
            }
            catch (IOException e) {
               log(e.getMessage());
            }

         }
         else if (cmd == Command.ROOM_LEFT) {
            try {
               PlayerInfo player = (PlayerInfo) getPrefixedObject(buf);
               FigureInfo figure = getFigureInfo(buf);
               gameHandler.roomLeft(player, figure);
            }
            catch (IOException e) {
               log(e.getMessage());
            }
         }
         else if (cmd == Command.NEW_FIGURE) {
            try {
               FigureInfo oldF = getFigureInfo(buf);
               FigureInfo newF = getFigureInfo(buf);
               gameHandler.newFigure(oldF, newF);
            }
            catch (IOException e) {
               log(e.getMessage());
            }
         }
         else if (cmd == Command.MOVE_LEFT) {
            try {
               FigureInfo figure = getFigureInfo(buf);
               gameHandler.moveLeft(figure);
            }
            catch (IOException e) {
               log(e.getMessage());
            }
         }
         else if (cmd == Command.MOVE_RIGHT) {
            try {
               FigureInfo figure = getFigureInfo(buf);
               gameHandler.moveRight(figure);
            }
            catch (IOException e) {
               log(e.getMessage());
            }
         }
         else if (cmd == Command.MOVE_DOWN) {
            try {
               FigureInfo figure = getFigureInfo(buf);
               gameHandler.moveDown(figure);
            }
            catch (IOException e) {
               log(e.getMessage());
            }
         }
         else if (cmd == Command.MOVE_ALL_WAY_DOWN) {
            try {
               FigureInfo current = getFigureInfo(buf);
               FigureInfo newPos = getFigureInfo(buf);

               gameHandler.moveAllWayDown(current, newPos);
            }
            catch (IOException e) {
               log(e.getMessage());
            }
         }
         else if (cmd == Command.ROTATE) {
            try {
               FigureInfo figure = getFigureInfo(buf);
               gameHandler.rotate(figure);
            }
            catch (IOException e) {
               log(e.getMessage());
            }
         }
         else if (cmd == Command.GAME_OVER) {
            gameHandler.gameOver();
         }
      }
   }

   private String getString(ByteBuffer buf) {
      byte[] bytes = new byte[buf.remaining()];
      buf.get(bytes);
      return new String(bytes);
   }

   //
   // Send Methods
   // 
   private void send(ByteBuffer buff) {
      buff.rewind();
      try {
         msgTimeStamp = System.currentTimeMillis();
         client.send(buff);
      }
      catch (IOException e) {
         log(e.getMessage());
      }
   }

   private void channelSend(ByteBuffer buff) {
      buff.rewind();
      if (channel != null) {
         try {
            msgTimeStamp = System.currentTimeMillis();
            channel.send(buff);
         }
         catch (IOException e) {
            log(e.getMessage());
         }
      }
      else {
         log("trying to channelSend on a null channel");
      }
   }

   public void sendCreateRoom(int maxPlayers) {
      ByteBuffer buff = ByteBuffer.allocate(8);
      buff.putInt(Commands.encode(Command.CREATE_ROOM));
      buff.putInt(maxPlayers);

      send(buff);
   }

   public void sendRoomJoined(long id) {
      ByteBuffer buff = ByteBuffer.allocate(12);
      buff.putInt(Commands.encode(Command.ROOM_JOINED));
      buff.putLong(id);

      send(buff);
   }

   public void sendChat(String msg) {
      ByteBuffer buff = ByteBuffer.allocate(4 + msg.length());
      buff.putInt(Commands.encode(Command.CHAT));
      buff.put(msg.getBytes());

      channelSend(buff);
   }

   public void sendAbandon() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.ABANDON));

      send(buff);
   }

   public void sendRequestLobbyData() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.REQUEST_LOBBY_DATA));

      send(buff);
   }

   public void sendJoinRoom(ClientRoom r) {
      ByteBuffer buff = ByteBuffer.allocate(12);
      buff.putInt(Commands.encode(Command.JOIN_ROOM));
      buff.putLong(r.getId());

      send(buff);
   }

   public void sendGameStart() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.GAME_START));

      send(buff);
   }

   public void sendMoveLeft() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.MOVE_LEFT));

      send(buff);
   }

   public void sendMoveRight() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.MOVE_RIGHT));

      send(buff);
   }

   public void sendMoveDown() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.MOVE_DOWN));

      send(buff);
   }

   public void sendMoveAllWayDown() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.MOVE_ALL_WAY_DOWN));

      send(buff);
   }

   public void sendRotate() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.ROTATE));

      send(buff);
   }

   public void sendPingMessage() {
      // send ping only if we haven't send anything for a while
      if (System.currentTimeMillis() - msgTimeStamp > PING_TIMEOUT) {
         ByteBuffer buff = ByteBuffer.allocate(4);
         buff.putInt(Commands.encode(Command.PING));
         send(buff);
      }
   }
}