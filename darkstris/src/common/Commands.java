package common;

import java.util.HashMap;
import java.util.Map;

public final class Commands {

   private static final Map<Integer, Commands.Command> ordinalToEnum = new HashMap<Integer, Commands.Command>();

   static {
      for (Commands.Command cmd : Commands.Command.values()) {
         ordinalToEnum.put(cmd.ordinal(), cmd);
      }
   }

   public static int encode(Commands.Command cmd) {
      return cmd.ordinal();
   }

   public static Commands.Command decode(int encodedType) {
      Command cmd = ordinalToEnum.get(encodedType);
      if (cmd == null) {
         throw new IllegalArgumentException("Unknown encoding of command: "
               + encodedType);
      }
      return cmd;
   }

   private Commands() {
   }

   public static enum Command {
      /** Chat message */
      CHAT,
      /** User joined the lobby */
      USER_JOINED,
      /** User left the lobby */
      USER_LEFT,
      /** Lobby data: current players and rooms */
      LOBBY_DATA,
      /** User wants to create a new room */
      CREATE_ROOM,
      /** Confirmation that the room was successfully created */
      ROOM_CREATED,
      /** User joined a room */
      ROOM_JOINED,
      /** Room is full (user can't join it) */
      ROOM_FULL,
      /** User has left a room */
      ROOM_LEFT,
      /** Room was dropped (no players left in room) */
      ROOM_DROPPED,
      /** User abandoned the game */
      ABANDON,
      /** User requests lobby data */
      REQUEST_LOBBY_DATA,
      /** User wants to join a room */
      JOIN_ROOM,
      /** Game start signal */
      GAME_START,
      /** Game over */
      GAME_OVER,
      /** Move figure left */
      MOVE_LEFT,
      /** Move figure right */
      MOVE_RIGHT,
      /** Move figure down */
      MOVE_DOWN,
      /** Move figure all way down */
      MOVE_ALL_WAY_DOWN,
      /** Rotate figure */
      ROTATE,
      /** New figure (after old one has been placed) */
      NEW_FIGURE,
      /** Change game owner (when game creator leaves the room) */
      GAME_OWNER,
      /** PING! */
      PING
   }
}