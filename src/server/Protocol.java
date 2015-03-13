package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import common.Commands;
import common.FigureInfo;
import common.LobbyData;
import common.PlayerInfo;
import common.Commands.Command;

public class Protocol {

   /**
    * @return the serialized byte array representation of the object
    */
   private static byte[] encode(Object obj) {
      try {
         // serialize object to a stream
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(bout);

         oos.writeObject(obj);
         oos.close();

         // return the bytes
         return bout.toByteArray();
      }
      catch (IOException ioe) {
         throw new IllegalArgumentException("couldn't encode object", ioe);
      }
   }

   /**
    * Encode a FigureInfo into a byte array (this method does not use
    * Serialization)
    * 
    * @param fig
    * @return
    */
   private static byte[] encodeFigure(FigureInfo fig) {
      ByteBuffer buff = ByteBuffer.allocate(20);
      buff.putInt(fig.getType());
      buff.putInt(fig.getX());
      buff.putInt(fig.getY());
      buff.putInt(fig.getRotation());
      buff.putInt(fig.getColor());
      buff.flip();

      return buff.array();
   }

   /**
    * Message that the user <code>player</code> has joined the lobby
    */
   public static ByteBuffer userJoinedLobby(String player) {
      ByteBuffer buff = ByteBuffer.allocate(4 + player.length());
      buff.putInt(Commands.encode(Command.USER_JOINED));
      buff.put(player.getBytes());
      buff.rewind();

      return buff;
   }

   /**
    * Message that the user <code>player</code> has left the lobby
    */
   public static ByteBuffer userLeftLobby(String player) {
      ByteBuffer buff = ByteBuffer.allocate(4 + player.length());
      buff.putInt(Commands.encode(Command.USER_LEFT));
      buff.put(player.getBytes());
      buff.rewind();

      return buff;
   }

   /**
    * Message that the room of id <code>id</code> was dropped
    * 
    * @param id
    *           the id of the room
    */
   public static ByteBuffer roomDropped(long id) {
      ByteBuffer buff = ByteBuffer.allocate(12);
      buff.putInt(Commands.encode(Command.ROOM_DROPPED));
      buff.putLong(id);
      buff.rewind();

      return buff;
   }

   /**
    * Message to the players in the room that the game has started
    * 
    * @param boardWidth
    *           the width of the board (depending of the number of players
    *           inside it)
    * 
    * @param figures
    *           the starting figures
    */
   public static ByteBuffer gameStart(int boardWidth, List<FigureInfo> figures) {
      byte[] data = encode(figures);

      ByteBuffer buff = ByteBuffer.allocate(8 + data.length);
      buff.putInt(Commands.encode(Command.GAME_START));
      buff.putInt(boardWidth);
      buff.put(data);
      buff.rewind();

      return buff;
   }

   /**
    * Message to the lobby that the game started in the room of id
    * <code>id</code>, so it's closed from now
    */
   public static ByteBuffer gameStarted(long id) {
      ByteBuffer buff = ByteBuffer.allocate(12);
      buff.putInt(Commands.encode(Command.GAME_START));
      buff.putLong(id);
      buff.rewind();

      return buff;
   }

   /**
    * Message to the room signaling that the <code>oldFigure</code> is now
    * fixed into the board and <code>newFigure</code> was added to the board
    * 
    * @param oldFigure
    *           the figure that was fixed
    * @param newFigure
    *           the new figure added to the board
    */
   public static ByteBuffer newFigure(FigureInfo oldFigure, FigureInfo newFigure) {
      byte[] dataOld = encodeFigure(oldFigure);
      byte[] dataNew = encodeFigure(newFigure);

      ByteBuffer buff = ByteBuffer.allocate(12 + dataOld.length
            + dataNew.length);
      buff.putInt(Commands.encode(Command.NEW_FIGURE));
      buff.put(dataOld);
      buff.put(dataNew);
      buff.rewind();

      return buff;
   }

   /**
    * Message to a player containing the current data of the lobby (rooms and
    * players)
    */
   public static ByteBuffer lobbyData(LobbyData lobbyData) {
      byte[] data = encode(lobbyData);

      ByteBuffer bb = ByteBuffer.allocate(data.length + 4);
      bb.putInt(Commands.encode(Command.LOBBY_DATA));
      bb.put(data);
      bb.rewind();

      return bb;
   }

   /**
    * Message that signals that a room was created
    * 
    * @param id
    *           the room id
    * @param maxPlayers
    *           the maximum number of players this room allows
    * @param player
    *           the creator (and current user) of the room
    */
   public static ByteBuffer roomCreated(long id, int maxPlayers,
         PlayerInfo player) {
      ByteBuffer buff = ByteBuffer.allocate(20 + player.getName().length());
      buff.putInt(Commands.encode(Command.ROOM_CREATED));
      buff.putLong(id);
      buff.putInt(maxPlayers);
      buff.putInt(player.getColor());
      buff.put(player.getName().getBytes());

      buff.rewind();

      return buff;
   }

   /**
    * Message that signals that a player has joined a room
    * 
    * @param id
    *           room id
    * @param color
    *           the color assigned to the player
    * @param name
    *           the player name
    */
   public static ByteBuffer roomJoined(long id, int color, String name) {
      ByteBuffer buff = ByteBuffer.allocate(16 + name.length());
      buff.putInt(Commands.encode(Command.ROOM_JOINED));
      buff.putLong(id);
      buff.putInt(color);
      buff.put(name.getBytes());

      buff.rewind();

      return buff;
   }

   /**
    * Message to a user that he has joined a room
    * 
    * @param id
    *           room id
    * @param color
    *           the color assigned to the player
    * @param players
    *           the current players in the room
    */
   public static ByteBuffer roomJoined(long id, int color,
         List<PlayerInfo> players) {
      byte[] data = Protocol.encode(players);

      ByteBuffer buff = ByteBuffer.allocate(16 + data.length);
      buff.putInt(Commands.encode(Command.ROOM_JOINED));
      buff.putLong(id);
      buff.putInt(color);
      buff.put(data);

      buff.rewind();

      return buff;
   }

   /**
    * Message to the room players that a player left the room
    * 
    * @param playerInfo
    *           the player that left
    * @param figInfo
    *           the current figure the player was handling (to be removed from
    *           the board)
    */
   public static ByteBuffer roomLeft(PlayerInfo playerInfo, FigureInfo figInfo) {
      byte[] dataPlayer = encode(playerInfo);
      byte[] dataFigure = encodeFigure(figInfo);

      ByteBuffer buff = ByteBuffer.allocate(12 + dataPlayer.length
            + dataFigure.length);
      buff.putInt(Commands.encode(Command.ROOM_LEFT));
      buff.putInt(dataPlayer.length);
      buff.put(dataPlayer);
      buff.put(dataFigure);

      buff.rewind();

      return buff;
   }

   /**
    * Message to a player that the room he wanted to join is full
    * 
    * @param id
    *           the room id
    */
   public static ByteBuffer roomFull(long id) {
      ByteBuffer buff = ByteBuffer.allocate(12);
      buff.putInt(Commands.encode(Command.ROOM_FULL));
      buff.putLong(id);
      buff.rewind();

      return buff;
   }

   /**
    * Message to a room to move a figure to the left
    * 
    * @param figureInfo
    *           the figure to move
    */
   public static ByteBuffer moveLeft(FigureInfo figureInfo) {
      byte[] data = encodeFigure(figureInfo);

      ByteBuffer buff = ByteBuffer.allocate(4 + data.length);
      buff.putInt(Commands.encode(Command.MOVE_LEFT));
      buff.put(data);
      buff.rewind();

      return buff;
   }

   /**
    * Message to a room to move a figure to the right
    * 
    * @param figureInfo
    *           the figure to move
    */
   public static ByteBuffer moveRight(FigureInfo figureInfo) {
      byte[] data = encodeFigure(figureInfo);

      ByteBuffer buff = ByteBuffer.allocate(4 + data.length);
      buff.putInt(Commands.encode(Command.MOVE_RIGHT));
      buff.put(data);
      buff.rewind();

      return buff;
   }

   /**
    * Message to a room to move a figure down
    * 
    * @param figureInfo
    *           the figure to move
    */
   public static ByteBuffer moveDown(FigureInfo figureInfo) {
      byte[] data = encodeFigure(figureInfo);

      ByteBuffer buff = ByteBuffer.allocate(4 + data.length);
      buff.putInt(Commands.encode(Command.MOVE_DOWN));
      buff.put(data);
      buff.rewind();

      return buff;
   }

   /**
    * Message to a room to move a figure all way down
    * 
    * @param old
    *           the figure info to move from
    * @param actual
    *           the figure info to move to
    */
   public static ByteBuffer moveAllWayDown(FigureInfo old, FigureInfo actual) {
      byte[] dataOld = encodeFigure(old);
      byte[] dataActual = encodeFigure(actual);

      ByteBuffer buff = ByteBuffer.allocate(4 + dataOld.length
            + dataActual.length);
      buff.putInt(Commands.encode(Command.MOVE_ALL_WAY_DOWN));
      buff.put(dataOld);
      buff.put(dataActual);

      buff.rewind();

      return buff;
   }

   /**
    * Message to a room to rotate a figure
    * 
    * @param figureInfo
    *           the figure to rotate
    */
   public static ByteBuffer rotate(FigureInfo figureInfo) {
      byte[] data = encodeFigure(figureInfo);

      ByteBuffer buff = ByteBuffer.allocate(4 + data.length);
      buff.putInt(Commands.encode(Command.ROTATE));
      buff.put(data);
      buff.rewind();

      return buff;
   }

   /**
    * Message to a room signaling that the game is over
    */
   public static ByteBuffer gameOver() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.GAME_OVER));
      buff.rewind();

      return buff;
   }

   /**
    * Message to a player signaling that now he is the owner of the room (he can
    * start the game)
    * 
    * @param figureInfo
    *           the figure to move
    */
   public static ByteBuffer gameOwner() {
      ByteBuffer buff = ByteBuffer.allocate(4);
      buff.putInt(Commands.encode(Command.GAME_OWNER));
      buff.rewind();

      return buff;
   }
}