package server;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import common.Commands;
import common.PlayerInfo;
import common.Commands.Command;

public class Player implements ManagedObject, ClientSessionListener,
      Serializable {

   private static final long serialVersionUID = 1L;

   private ManagedReference<ClientSession> currentSessionRef;

   /** the room the player is in */
   private ManagedReference<ServerRoom> roomRef;

   /** the current Figure the player is handling */
   private ManagedReference<ServerFigure> figureRef;

   /** the color assigned to this Player */
   private int color;

   /** the position where the pieces are added for this player in the board */
   private int x;

   private static Logger log = Logger.getLogger(Player.class.getName());

   private String name;

   public Player(ClientSession sess) {
      log.setLevel(Level.FINE);

      this.name = sess.getName();

      AppContext.getDataManager().setBinding(
            DarkstrisServer.USER_PREFIX + name, this);

      // save reference
      currentSessionRef = AppContext.getDataManager().createReference(sess);
   }

   public ClientSession getClientSession() {
      return currentSessionRef.get();
   }

   public int getColor() {
      return color;
   }

   public void setColor(int color) {
      this.color = color;
   }

   public int getX() {
      return x;
   }

   public void setX(int x) {
      this.x = x;
   }

   public String getName() {
      return name;
   }

   public ServerFigure getFigure() {
      if (figureRef != null) {
         return figureRef.get();
      }
      else {
         return null;
      }
   }

   public void setFigure(ServerFigure figure) {
      figureRef = AppContext.getDataManager().createReference(figure);
   }

   public PlayerInfo getPlayerInfo() {
      return new PlayerInfo(name, color);
   }

   public void removeFromRoom(ServerRoom serverRoom) {
      log("removeFromRoom(room" + serverRoom.getId() + ")");
      if (roomRef != null && serverRoom.equals(roomRef.get())) {
         roomRef = null;
      }
   }

   /**
    * Player has joined the room
    * 
    * @param room
    */
   public void joinedRoom(ServerRoom room) {
      log("joinedRoom(room" + room.getId() + ")");
      roomRef = AppContext.getDataManager().createReference(room);
   }

   // //////////////////////
   // ClientSessionListener
   // //////////////////////
   @Override
   public void disconnected(boolean forced) {
      log("disconnected " + (forced ? "forced" : "easy"));

      abandonRoom();

      // remove the user
      Lobby lobby = (Lobby) AppContext.getDataManager().getBinding(
            DarkstrisServer.LOBBY);
      AppContext.getDataManager().markForUpdate(lobby);
      lobby.remove(name);

      // broadcast player removed to the lobby
      Channel lobbyChannel = AppContext.getChannelManager().getChannel(
            DarkstrisServer.LOBBY_CHANNEL);
      lobbyChannel.send(null, Protocol.userLeftLobby(name));

      // remove binding
      AppContext.getDataManager().removeBinding(
            DarkstrisServer.USER_PREFIX + name);

      // bye bye life
      AppContext.getDataManager().removeObject(this);

      currentSessionRef = null;
   }

   private void abandonRoom() {
      log("abandonRoom()");
      if (roomRef != null) {
         roomRef.getForUpdate().removePlayer(this);
         roomRef = null;
         figureRef = null;
      }
   }

   @Override
   public void receivedMessage(ByteBuffer buf) {
      int encodedCmd = buf.getInt();
      Command cmd = Commands.decode(encodedCmd);

      if (cmd == Command.PING) {
         log("ping");
      }
      else if (cmd == Command.CREATE_ROOM) {
         log("create room");

         int maxPlayers = buf.getInt();
         AppContext.getTaskManager().scheduleTask(
               new CreateAndJoinTask(this, maxPlayers));
      }
      else if (cmd == Command.ROOM_JOINED) {
         // notification that a user is now inside a room
         long roomId = buf.getLong();

         log("is inside room" + roomId);

         Channel roomChannel = AppContext.getChannelManager().getChannel(
               "room" + roomId);

         // add the user to the room channel
         roomChannel.join(currentSessionRef.get());
      }
      else if (cmd == Command.JOIN_ROOM) {
         // user wants to join a room
         long roomId = buf.getLong();

         log("wants to join room" + roomId);

         AppContext.getTaskManager().scheduleTask(
               new JoinRoomTask(roomId, this));
      }
      else if (cmd == Command.ABANDON) {
         // remove user from the room
         abandonRoom();
      }
      else if (cmd == Command.REQUEST_LOBBY_DATA) {
         log("requests lobby data");
         Channel lobbyChannel = AppContext.getChannelManager().getChannel(
               DarkstrisServer.LOBBY_CHANNEL);
         Lobby lobby = (Lobby) AppContext.getDataManager().getBinding(
               DarkstrisServer.LOBBY);
         AppContext.getDataManager().markForUpdate(lobby);

         // user is in lobby, add him to lobby channel
         // playerManagerRef.getForUpdate().addToLobby(currentSessionRef.get());
         AppContext.getDataManager().markForUpdate(lobby);
         lobby.add(name);

         // remove client from lobbyChannel
         lobbyChannel.join(getClientSession());

         // broadcast lobby that this user is in lobby

         lobbyChannel.send(null, Protocol.userJoinedLobby(name));

         // send lobby data back to user
         currentSessionRef.get().send(Protocol.lobbyData(lobby.getLobbyData()));
      }
      else if (cmd == Command.GAME_START) {
         roomRef.get().gameStart();
      }
      else if (cmd == Command.MOVE_LEFT) {
         roomRef.get().moveLeft(figureRef.get());
      }
      else if (cmd == Command.MOVE_RIGHT) {
         roomRef.get().moveRight(figureRef.get());
      }
      else if (cmd == Command.MOVE_DOWN) {
         roomRef.get().moveDown(figureRef.get(), this);
      }
      else if (cmd == Command.MOVE_ALL_WAY_DOWN) {
         roomRef.get().moveAllWayDown(figureRef.get(), this);
      }
      else if (cmd == Command.ROTATE) {
         roomRef.get().rotate(figureRef.get());
      }
   }

   private void log(String msg) {
      System.out
            .println(System.currentTimeMillis() + " - " + name + ": " + msg);
   }
}