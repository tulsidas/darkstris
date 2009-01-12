package client;

import static pulpcore.image.Colors.WHITE;
import static pulpcore.image.Colors.rgb;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import pulpcore.CoreSystem;
import pulpcore.Input;
import pulpcore.Stage;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.scene.Scene;
import pulpcore.sprite.Button;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Label;
import pulpcore.sprite.Slider;
import pulpcore.sprite.Sprite;
import pulpcore.sprite.TextField;

import common.PlayerInfo;
import common.RoomInfo;

public class LobbyScene extends PingScene implements Serializable, LobbyHandler {

   private static final long serialVersionUID = -4205602381752304924L;

   private ColoredChatArea chat;

   private TextField chatTF;

   private Button sendButton;

   private PlayersBox players;

   private Slider maxPlayers;

   private RoomsBox rooms;

   private String userName;

   private Button createRoom;

   private boolean mustDisconnect;

   private ConnectionHandler connection;

   public LobbyScene(String userName, ConnectionHandler connection) {
      super(connection);

      this.userName = userName;
      this.connection = connection;
      // inject client
      connection.setLobbyHandler(this);

      this.mustDisconnect = true;
   }

   public void load() {
      CoreFont font = CoreFont.load("imgs/FS.font.png");

      // background
      add(new ImageSprite("imgs/lobby.png", 0, 0));

      // rooms box
      rooms = new RoomsBox(this, 13, 90, 238, 289, font);
      add(rooms);

      // players box
      players = new PlayersBox(270, 90, 178, 289, font);
      add(players);

      // chat text field
      chatTF = new TextField(font, font.tint(WHITE), "", 467, 395, 217, -1);
      chatTF.setMaxNumChars(80);
      add(chatTF);

      // button to send chat (bound to ENTER_KEY)
      sendButton = new Button(CoreImage.load("imgs/btn-send.png").split(3),
            688, 392);
      sendButton.setKeyBinding(Input.KEY_ENTER);
      add(sendButton);

      // button to create room
      createRoom = Button.createLabeledButton(null, font, "Create Room", 160,
            395);
      add(createRoom);

      maxPlayers = new Slider("imgs/slider.png", "imgs/slider-thumb.png", 20,
            400);
      maxPlayers.setAnchor(Sprite.WEST);
      maxPlayers.setRange(1, 8);
      Label labelSlider = new Label(font.tint(Colors.WHITE),
            "Max #Players: %d ", 150, 420);
      labelSlider.setFormatArg(maxPlayers.value);
      labelSlider.setAnchor(Sprite.EAST);
      add(labelSlider);
      add(maxPlayers);

      // chat box
      chat = new ColoredChatArea(467, 90, 236, 289, font, font
            .tint(rgb(0xaa0000)), ':', userName);
      add(chat);

      // pause until we get lobby data
      setPaused(true);

      // request lobby data
      connection.sendRequestLobbyData();
   }

   public void update(int elapsedTime) {
      super.update(elapsedTime);

      if (!isPaused()) {
         if (sendButton.isClicked()) {
            if (chatTF.getText().trim().length() > 0) {
               String msg = userName + ": " + chatTF.getText().trim();
               connection.sendChat(msg);

               chatTF.setText("");
            }
         }
         else if (createRoom.enabled.get() && createRoom.isMouseReleased()) {
            createRoom.enabled.set(false);

            connection.sendCreateRoom(maxPlayers.value.getAsInt());
         }
      }
   }

   private final void setScene(final Scene s) {
      mustDisconnect = false;
      Stage.setScene(s);
   }

   /**
    * This scene is being unloaded. If we are switching to a game, then keep the
    * connection; else (browser closed or location changed) close connection to
    * Darkstar
    */
   public void unload() {
      if (mustDisconnect) {
         connection.disconnect();
      }
   }

   // //////////////////////
   // LobbyHandler
   // //////////////////////
   public void log(final String str) {
      invokeLater(new Runnable() {
         @Override
         public void run() {
            CoreSystem.print(str);
         }
      });
   }

   public void incomingChat(final String str) {
      invokeLater(new Runnable() {
         @Override
         public void run() {
            chat.addLine(str);
         }
      });
   }

   /**
    * Add player to lobby, remove it from a room, if any
    */
   public void addPlayer(final String name) {
      invokeLater(new Runnable() {
         @Override
         public void run() {
            players.addPlayer(name);
            rooms.removePlayer(name);
         }
      });
   }

   /**
    * Remove player from lobby, also remove it from a room, if any
    */
   public void removePlayer(final String name) {
      invokeLater(new Runnable() {
         @Override
         public void run() {
            players.removePlayer(name);
            rooms.removePlayer(name);
         }
      });
   }

   @Override
   public void roomCreated(final long id, final int maxPlayers,
         final PlayerInfo player) {
      invokeLater(new Runnable() {
         public void run() {
            ClientRoom room = new ClientRoom(id, player);
            room.setMaxPlayers(maxPlayers);

            // if I'm the one who created the room, move to the GameScene
            if (player.getName().equals(userName)) {
               setScene(new DarkstrisScene(connection, player, room, true));
            }
            else {
               // add the created room
               rooms.addItem(room);

               // remove the user from the lobby list
               players.removePlayer(player.getName());
            }
         }
      });
   }

   /**
    * User has joined a room, add it to the the room list
    */
   @Override
   public void roomJoined(final long id, final PlayerInfo player) {
      invokeLater(new Runnable() {
         public void run() {
            ClientRoom room = new ClientRoom(id);
            rooms.addPlayer(room, player);
            players.removePlayer(player.getName());
         }
      });
   }

   /**
    * I've joined this room, go to game room
    */
   @Override
   public void roomJoined(long id, int color,
         Collection<PlayerInfo> currentPlayers) {
      ClientRoom room = new ClientRoom(id, currentPlayers);
      setScene(new DarkstrisScene(connection, new PlayerInfo(userName, color),
            room));
   }

   /**
    * Room was full, can't join it
    */
   @Override
   public void roomFull(long id) {
      rooms.enableButton(new ClientRoom(id), true);
   }

   /**
    * Room was dropped
    */
   @Override
   public void roomDropped(final long id) {
      invokeLater(new Runnable() {
         public void run() {
            rooms.dropRoom(id);
         }
      });
   }

   /**
    * Game started in this room, close it
    */
   @Override
   public void gameStarted(final long id) {
      invokeLater(new Runnable() {
         public void run() {
            rooms.gameStarted(new ClientRoom(id));
         }
      });
   }

   @Override
   public void joinRoomRequest(ClientRoom room) {
      if (!room.isStarted() && !room.isFull()) {
         rooms.enableButton(room, false);

         connection.sendJoinRoom(room);
      }
   }

   @Override
   public void setLobbyData(Set<String> players, Set<RoomInfo> roomInfos) {
      for (String str : players) {
         addPlayer(str);
      }

      for (RoomInfo ri : roomInfos) {
         rooms.addItem(new ClientRoom(ri));
      }

      // game on
      setPaused(false);
   }
}