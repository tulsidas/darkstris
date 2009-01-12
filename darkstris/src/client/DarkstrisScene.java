package client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import pulpcore.CoreSystem;
import pulpcore.Input;
import pulpcore.Stage;
import pulpcore.animation.Easing;
import pulpcore.animation.Timeline;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.scene.Scene;
import pulpcore.sprite.Button;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.Label;
import pulpcore.sprite.Sprite;

import common.FigureInfo;
import common.PlayerInfo;

public class DarkstrisScene extends PingScene implements GameHandler,
      ChatListener {

   private ConnectionHandler connection;

   private ClientRoom room;

   private boolean mustDisconnect;

   private Button abandonGame, startGame;

   private ChatBox chatBox;

   private CoreFont font, fontW;

   private String userName;

   private int color;

   /** are we playing or waiting for players? */
   private boolean inGame;

   /**
    * are we the creator of this room? (only the creator can issue the game
    * start)
    */
   private boolean creator;

   private final int PLAYER_Y = 30;

   private int playerLblX;

   private List<Label> playerLabels;

   private BoardSprite board;

   /**
    * The game level. The level will be increased for every 20 lines removed
    * from the square board.
    */
   private int level = 0;

   /**
    * The current score. The score is increased for every figure that is
    * possible to place on the main board.
    */
   private int score = 0;

   /**
    * Time passed since last tick
    */
   private int currentTime;

   /**
    * Length of current tick, it gets shorter on harder levels
    */
   private int currentTick;

   /** Timers to delay the button presses */
   private int delayLeft, delayRight, delayDown;

   private static final int KEY_DELAY = 100;

   /**
    * The score label.
    */
   private Label scoreLabel;

   /**
    * The level label.
    */
   private Label levelLabel;

   private Easing customEasing = new Easing() {
      @Override
      protected double ease(double t) {
         return Math.sin(t * 2 * Math.PI) / 3 + t;
      }
   };

   public DarkstrisScene(ConnectionHandler connection, PlayerInfo player,
         ClientRoom room) {
      this(connection, player, room, false);
   }

   public DarkstrisScene(ConnectionHandler connection, PlayerInfo player,
         ClientRoom room, boolean creator) {
      super(connection);

      this.connection = connection;
      // inject client
      connection.setGameHandler(this);

      this.mustDisconnect = true;

      this.room = room;
      this.userName = player.getName();
      this.color = player.getColor();
      this.creator = creator;

      this.delayLeft = 0;
      this.delayRight = 0;
      this.delayDown = 0;

      this.currentTick = 1000;

      this.playerLabels = new ArrayList<Label>();

      // initialize player label locations
      playerLblX = 10;
   }

   @Override
   public void load() {
      // fonts
      font = CoreFont.load("imgs/FS.font.png");
      fontW = font.tint(Colors.WHITE);

      add(new FilledSprite(Colors.BLACK));

      addPlayerLabel(new PlayerInfo(userName, color));

      // the other ones
      for (PlayerInfo ply : room.getPlayers()) {
         if (!ply.getName().equals(userName)) {
            addPlayerLabel(ply);
         }
      }

      // the board, with the current # of players
      // FIXME extract formula to common place with server
      board = new BoardSprite(0, 50, 8 + (playerLabels.size() - 1) * 4, 20);
      add(board);

      // chatbox above the board so we can still chat even if it's huge
      chatBox = new ChatBox(this, 430, 80);
      add(chatBox);

      scoreLabel = new Label(fontW, "Score: 0", 10, 10);
      add(scoreLabel);

      levelLabel = new Label(fontW, "Level: 1", 110, 10);
      add(levelLabel);

      startGame = Button.createLabeledButton("Start!", 190, 5);
      if (creator) {
         // add start game
         add(startGame);
      }

      // abandon button
      abandonGame = Button.createLabeledButton("Leave Room", 590, 5);
      add(abandonGame);

      // let the server know that we are in the game room
      connection.sendRoomJoined(room.getId());
   }

   /*
    * This scene is being unloaded if we are switching to the lobby, then keep
    * the connection else (browser closed or location changed) close connection
    * to Darkstar
    */
   public void unload() {
      if (mustDisconnect) {
         connection.disconnect();
      }
   }

   @Override
   public void update(int elapsedTime) {
      super.update(elapsedTime);

      if (startGame.isClicked()) {
         startGame.enabled.set(false);
         connection.sendGameStart();
      }

      if (inGame) {
         this.currentTime += elapsedTime;

         if (currentTime >= currentTick) {
            handleTimer();
            currentTime = 0;
         }

         // update key delays
         if (delayLeft > 0) {
            delayLeft -= elapsedTime;
         }
         if (delayRight > 0) {
            delayRight -= elapsedTime;
         }
         if (delayDown > 0) {
            delayDown -= elapsedTime;
         }

         // Handle remaining key events
         if (Input.isDown(Input.KEY_LEFT) && delayLeft <= 0) {
            connection.sendMoveLeft();
            delayLeft = KEY_DELAY;
         }
         else if (Input.isDown(Input.KEY_RIGHT) && delayRight <= 0) {
            connection.sendMoveRight();
            delayRight = KEY_DELAY;
         }
         else if (Input.isDown(Input.KEY_DOWN) && delayDown <= 0) {
            connection.sendMoveDown();
            delayDown = KEY_DELAY;
         }
         else if (Input.isReleased(Input.KEY_SPACE)) {
            connection.sendMoveAllWayDown();
         }
         else if (Input.isReleased(Input.KEY_UP)) {
            connection.sendRotate();
         }
      }

      if (abandonGame.enabled.get() && abandonGame.isClicked()) {
         abandonGame();
      }
   }

   public void incomingChat(final String msg) {
      invokeLater(new Runnable() {
         public void run() {
            // tic.play();

            chatBox.addLine(msg);
         }
      });
   }

   /*
    * (non-Javadoc)
    * 
    * @see client.ChatListener#sendChat(java.lang.String)
    */
   public void sendChat(String msg) {
      connection.sendChat(userName + ": " + msg);
   }

   private final void setScene(final Scene s) {
      mustDisconnect = false;
      Stage.setScene(s);
   }

   private void abandonGame() {
      // envio abandono
      connection.sendAbandon();

      invokeLater(new Runnable() {
         public void run() {
            // me rajo al lobby
            setScene(new LobbyScene(userName, connection));
         }
      });
   }

   // /////////////////////
   // Board & Game methods
   // /////////////////////
   /**
    * Handles a game start event. Both the main and preview square boards will
    * be reset, and all other game parameters will be reset. Finally the game
    * thread will be launched.
    */
   private void handleStart() {
      // Reset score and figures
      inGame = true;
      level = 1;
      score = 0;

      // Reset components
      handleScoreModification();
   }

   /**
    * Handles a level modification event. This will modify the level label and
    * adjust the thread speed.
    */
   private void handleLevelModification() {
      levelLabel.setText("Level: " + level);
      currentTick *= 0.8;
   }

   /**
    * Handle a score modification event. This will modify the score label.
    */
   private void handleScoreModification() {
      scoreLabel.setText("Score: " + score);
   }

   /**
    * Handles a timer event. This will normally move the figure down one step,
    * but when a figure has landed or isn't ready other events will be launched.
    * This method is synchronized to avoid race conditions with other
    * asynchronous events (keyboard and mouse).
    */
   private void handleTimer() {
      if (inGame) {
         connection.sendMoveDown();
      }
   }

   public void log(final String str) {
      invokeLater(new Runnable() {
         @Override
         public void run() {
            CoreSystem.print(str);
         }
      });
   }

   // /////////////
   // GameHandler
   // /////////////
   /**
    * A user joined this room
    */
   @Override
   public void roomJoined(final PlayerInfo player) {
      invokeLater(new Runnable() {
         public void run() {
            if (!room.hasPlayer(player.getName())) {
               addPlayerLabel(player);
               room.addPlayer(player);
               board.addPlayer();
            }
         }
      });
   }

   /**
    * A user left this room
    */
   @Override
   public void roomLeft(final PlayerInfo player, final FigureInfo figInfo) {
      invokeLater(new Runnable() {
         public void run() {
            removePlayerLabel(player);
            room.removePlayer(player);

            board.removePlayer();

            if (figInfo != null) {
               Figure fig = new Figure(figInfo);
               fig.attach(board, figInfo.getX(), figInfo.getY());
               fig.clear();
            }
         }
      });
   }

   @Override
   public void gameStart(final int boardWidth,
         final Collection<FigureInfo> figures) {
      invokeLater(new Runnable() {
         public void run() {
            // move and dissapear chat box
            chatBox.alpha.animateTo(0, 1500);
            chatBox.x.animateTo(Stage.getWidth() + 50, 1500, Easing.STRONG_OUT,
                  500);
            chatBox.enabled.set(false);

            // center board
            board.x.animateTo((Stage.getWidth() - board.getCurrentWidth()) / 2,
                  1500);

            room.setStarted(true);
            board.gameStart();

            remove(startGame);
            handleStart();

            // set up figures
            for (FigureInfo figInfo : figures) {
               Figure fig = new Figure(figInfo);
               fig.attach(board, figInfo.getX(), figInfo.getY());
            }
         }
      });
   }

   public void gameOver() {
      invokeLater(new Runnable() {
         public void run() {
            inGame = false;
            board.setGameOver(true);

            // restore chat box
            chatBox.x.animateTo(430, 1500, Easing.STRONG_OUT);
            chatBox.alpha.animateTo(0xFF, 1500, Easing.NONE, 500);
            chatBox.enabled.set(true);

            // animate game over label
            Label label = new Label(fontW, "GAME OVER", Stage.getWidth() / 2,
                  Stage.getHeight() / 2);
            label.setAnchor(Sprite.CENTER);

            Label label2 = new Label(font, "GAME OVER", Stage.getWidth() / 2,
                  Stage.getHeight() / 2);
            label2.width.set(label.width.getAsInt() + 2);
            label2.height.set(label.height.getAsInt() + 2);
            label2.setAnchor(Sprite.CENTER);

            add(label2);
            add(label);

            int lw = label.width.getAsInt();
            int lh = label.height.getAsInt();
            int lw2 = label2.width.getAsInt();
            int lh2 = label2.height.getAsInt();

            int duration = 1500;

            Timeline timeline = new Timeline();
            timeline.scale(label, lw, lh, lw * 4, lh * 4, duration);
            timeline.at(duration).scale(label, lw * 4, lh * 4, lw * 1.5,
                  lh * 1.5, duration);
            timeline.scale(label2, lw2, lh2, lw2 * 4, lh2 * 4, duration);
            timeline.at(duration).scale(label2, lw2 * 4, lh2 * 4, lw2 * 1.5,
                  lh2 * 1.5, duration);

            addTimeline(timeline);
         }
      });
   }

   /**
    * New figure has arrived
    */
   @Override
   public void newFigure(final FigureInfo oldFigure, final FigureInfo newFigure) {
      invokeLater(new Runnable() {
         public void run() {
            // fix oldFigure
            Figure oldFig = new Figure(oldFigure);
            oldFig.attach(board, oldFigure.getX(), oldFigure.getY());
            oldFig.fix();

            // 1 points for placing a figure
            score += 1;

            // check for full lines
            int removedLines = board.removeFullLines();

            if (removedLines > 0) {
               // 10, 20, 40, 80
               score += 10 * Math.pow(2, removedLines - 1);

               // shake it baby!!
               int duration = 200;
               int offset = 10;
               Timeline timeline = new Timeline();
               timeline.animateTo(board.x, board.x.get() + offset, duration,
                     customEasing);
               timeline.at(duration).animate(board.x, board.x.get() + offset,
                     board.x.get(), duration, customEasing);
               addTimeline(timeline);
            }

            if (level < 9 && score / 150 > level) {
               level++;
               handleLevelModification();
            }

            handleScoreModification();

            Figure fig = new Figure(newFigure);
            fig.attach(board, newFigure.getX(), newFigure.getY());
         }
      });
   }

   @Override
   public void moveDown(final FigureInfo figInfo) {
      invokeLater(new Runnable() {
         public void run() {
            Figure fig = new Figure(figInfo);
            fig.attach(board, figInfo.getX(), figInfo.getY());
            fig.moveDown();
         }
      });
   }

   public void moveAllWayDown(final FigureInfo figInfo, final FigureInfo newPos) {
      invokeLater(new Runnable() {
         public void run() {
            Figure fig = new Figure(figInfo);
            fig.attach(board, figInfo.getX(), figInfo.getY());
            fig.moveTo(newPos);
         }
      });
   }

   @Override
   public void moveLeft(final FigureInfo figInfo) {
      invokeLater(new Runnable() {
         public void run() {
            Figure fig = new Figure(figInfo);
            fig.attach(board, figInfo.getX(), figInfo.getY());
            fig.moveLeft();
         }
      });
   }

   @Override
   public void moveRight(final FigureInfo figInfo) {
      invokeLater(new Runnable() {
         public void run() {
            Figure fig = new Figure(figInfo);
            fig.attach(board, figInfo.getX(), figInfo.getY());
            fig.moveRight();
         }
      });
   }

   @Override
   public void rotate(final FigureInfo figInfo) {
      invokeLater(new Runnable() {
         public void run() {
            Figure fig = new Figure(figInfo);
            fig.attach(board, figInfo.getX(), figInfo.getY());
            fig.rotateClockwise();
         }
      });
   }

   @Override
   public void setGameOwner() {
      invokeLater(new Runnable() {
         public void run() {
            creator = true;

            // add start game
            add(startGame);
         }
      });
   }

   private void addPlayerLabel(PlayerInfo player) {
      CoreFont coloredFont = font.tint(player.getColor());

      // truncate if name too large
      String name = getTruncatedName(coloredFont, player.getName());

      Label lbl = new Label(coloredFont, name, 0, PLAYER_Y);
      lbl.x.animateTo(playerLblX, 500);

      // add it to the scene
      add(lbl);

      // store it
      playerLabels.add(lbl);

      playerLblX += coloredFont.getStringWidth(name) + 8;
   }

   private void removePlayerLabel(PlayerInfo player) {
      int width = 0;
      boolean found = false;
      Iterator<Label> it = playerLabels.iterator();
      while (it.hasNext()) {
         Label lbl = it.next();
         String name = getTruncatedName(font, player.getName());
         if (lbl.getText().equals(name) && !found) {
            remove(lbl);
            it.remove();

            width = font.getStringWidth(name);
            playerLblX -= font.getStringWidth(player.getName()) + 8;

            found = true;
         }
         else if (found) {
            // shift label left
            lbl.x.animateTo(lbl.x.getAsInt() - width, 500);
         }
      }
   }

   private String getTruncatedName(CoreFont font, String name) {
      // truncate if name too large
      int length = font.getStringWidth(name);
      while (length > 95) {
         name = name.substring(0, name.length() - 1);
         length = font.getStringWidth(name);
      }

      return name;
   }
}

// TODO highscores @ lobby