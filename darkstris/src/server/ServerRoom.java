package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;
import common.FigureInfo;
import common.PlayerInfo;

public class ServerRoom implements ManagedObject, Serializable {

   private static final long serialVersionUID = 1691085231984583898L;

   private int maxPlayers;

   private long id;

   private ManagedReference<Channel> roomChannelRef;

   private List<ManagedReference<Player>> players;

   private ManagedReference<SquareBoard> boardRef;

   // private static Logger log = Logger.getLogger(ServerRoom.class.getName());

   /** Available colors */
   private static final List<Integer> COLORS = new ArrayList<Integer>(Arrays
         .asList(0xFFFF0000, // Colors.RED
               0xFF00FF00, // Colors.GREEN
               0xFFFFC800, // Colors.ORANGE
               0xFF0000FF,// Colors.BLUE
               0xFFFFFF00,// Colors.YELLOW
               0xFFA200FF,// Colors.PURPLE
               0xFFFFFFFF,// Colors.WHITE
               0xFFFF00FF// Colors.MAGENTA
         ));

   /** game started in this room? */
   private boolean started;

   /** game finished in this room? */
   private boolean gameOver;

   private List<Integer> availableColors;

   public ServerRoom(long id, int maxPlayers, Player player) {
      this.id = id;
      this.maxPlayers = maxPlayers;
      this.started = false;
      this.players = new ArrayList<ManagedReference<Player>>();
      this.started = false;
      this.gameOver = false;

      availableColors = new ArrayList<Integer>(COLORS);
      Collections.shuffle(availableColors);

      Channel roomChannel = AppContext.getChannelManager().createChannel(
            "room" + id, null, Delivery.RELIABLE);

      roomChannelRef = AppContext.getDataManager().createReference(roomChannel);

      addPlayer(player);
   }

   public Channel getChannel() {
      return roomChannelRef.get();
   }

   public long getId() {
      return id;
   }

   public int getMaxPlayers() {
      return maxPlayers;
   }

   public void addPlayer(Player player) {
      players.add(AppContext.getDataManager().createReference(player));

      System.out.println(player.getName() + " joined room" + id
            + ", availableColors: " + availableColors.size());

      if (availableColors.size() > 0) {
         // assign one of the remaining colors to the player
         player.setColor(availableColors.remove(0));
      }

      player.joinedRoom(this);
   }

   public void removePlayer(Player player) {
      ManagedReference<Player> playerRef = AppContext.getDataManager()
            .createReference(player);

      // if game hasn't started yet and the room creator left, then transfer
      // the ownership to the next player, so he can signal "start"
      if (!started && playerRef.equals(players.get(0)) && players.size() > 1) {
         // room owner left the room, transfer ownership to another one
         players.get(1).get().getClientSession().send(Protocol.gameOwner());
      }

      // remove player from room
      players.remove(playerRef);

      System.out.println("removePlayer " + player.getName() + " from room" + id
            + ", remaining: " + players.size());

      if (players.size() == 0) {
         // last one went away
         AppContext.getTaskManager().scheduleTask(new DropRoomTask(id));
      }
      else {
         // remove figure, if any, from board
         ServerFigure figure = player.getFigure();
         FigureInfo figInfo = null;
         if (figure != null) {
            figInfo = getFigureInfo(figure);
            figure.clear();
         }

         // make board smaller
         // incomplete method
         // boardRef.getForUpdate().removePlayer();

         // broadcast player left to players
         getChannel().send(null,
               Protocol.roomLeft(player.getPlayerInfo(), figInfo));

         // leave the room channel
         getChannel().leave(player.getClientSession());

         // get color back in pool
         availableColors.add(player.getColor());
      }
   }

   public List<Player> getPlayers() {
      List<Player> ret = new ArrayList<Player>();
      Iterator<ManagedReference<Player>> playerIt = players.iterator();
      while (playerIt.hasNext()) {
         ManagedReference<Player> playerRef = playerIt.next();
         try {
            ret.add(playerRef.get());
         }
         catch (ObjectNotFoundException onfe) {
            // user disconnected
            playerIt.remove();
         }
      }
      return ret;
   }

   public boolean hasPlayer(Player player) {
      return getPlayers().contains(player);
   }

   public List<PlayerInfo> getPlayerInfos() {
      List<PlayerInfo> ret = new ArrayList<PlayerInfo>(players.size());
      for (Player player : getPlayers()) {
         ret.add(player.getPlayerInfo());
      }
      return ret;
   }

   public boolean isFull() {
      return players.size() == maxPlayers;
   }

   public boolean isStarted() {
      return started;
   }

   public void gameStart() {
      AppContext.getDataManager().markForUpdate(this);
      started = true;

      int numPlayers = players.size();

      SquareBoard board = new SquareBoard(8 + (numPlayers - 1) * 4, 20);
      boardRef = AppContext.getDataManager().createReference(board);

      List<FigureInfo> figures = new ArrayList<FigureInfo>(numPlayers);

      int x = 3;
      // set up players
      for (Player ply : getPlayers()) {
         ply.setX(x);
         x += 4;

         ServerFigure fig = newFigure(ply);

         figures.add(new FigureInfo(fig.getType(), fig.getX(), fig.getY(), fig
               .getRotation(), fig.getColor()));
      }

      // broadcast game start with pieces
      getChannel().send(null,
            Protocol.gameStart(board.getBoardWidth(), figures));

      // broadcast to lobby that this game has started
      AppContext.getChannelManager().getChannel(DarkstrisServer.LOBBY_CHANNEL)
            .send(null, Protocol.gameStarted(id));
   }

   // /////////////////
   // Piece Movements
   // /////////////////
   public void moveLeft(ServerFigure figure) {
      if (!gameOver && figure.canMoveLeft()) {
         getChannel().send(null, Protocol.moveLeft(getFigureInfo(figure)));

         // save current position
         figure.moveLeft();
      }
      // else do nothing
   }

   public void moveRight(ServerFigure figure) {
      if (!gameOver && figure.canMoveRight()) {
         getChannel().send(null, Protocol.moveRight(getFigureInfo(figure)));

         // save current position
         figure.moveRight();
      }
      // else do nothing
   }

   public void rotate(ServerFigure figure) {
      if (!gameOver && figure.canRotate()) {
         getChannel().send(null, Protocol.rotate(getFigureInfo(figure)));

         figure.rotate();
      }
      // else do nothing
   }

   public void moveDown(ServerFigure figure, Player ply) {
      if (!gameOver) {
         if (figure.canMoveDown()) {
            getChannel().send(null, Protocol.moveDown(getFigureInfo(figure)));

            // save current position
            figure.moveDown();
         }
         else if (figure.isAllWayDown()) {
            handleFigureBottom(figure, ply);
         }
         else {
            // the figure is pushing against another player's figure
         }
      }
      // else do nothing
   }

   public void moveAllWayDown(ServerFigure figure, Player ply) {
      if (!gameOver) {
         if (figure.canMoveDown()) {
            FigureInfo old = getFigureInfo(figure);

            while (figure.canMoveDown()) {
               figure.moveDown();
            }

            FigureInfo actual = getFigureInfo(figure);

            getChannel().send(null, Protocol.moveAllWayDown(old, actual));
         }

         if (figure.isAllWayDown()) {
            handleFigureBottom(figure, ply);
         }
         else {
            // the figure is pushing against another player's figure
         }
      }
      // else do nothing
   }

   private static FigureInfo getFigureInfo(ServerFigure fig) {
      return new FigureInfo(fig.getType(), fig.getX(), fig.getY(), fig
            .getRotation(), fig.getColor());
   }

   private ServerFigure newFigure(Player ply) {
      ServerFigure fig = new ServerFigure((int) (1 + (Math.random() * 7)));
      fig.attach(boardRef.getForUpdate(), ply.getX());
      fig.setColor(ply.getColor());
      ply.setFigure(fig);

      return fig;
   }

   private void handleFigureBottom(ServerFigure figure, Player ply) {
      figure.fix();

      boardRef.getForUpdate().removeFullLines();

      if (!figure.isAllVisible()) {
         gameOver = true;

         // send gameover
         getChannel().send(null, Protocol.gameOver());
         // leave room open so players can talk about the game
      }
      else {
         ServerFigure newFigure = newFigure(ply);

         // send new piece
         getChannel().send(
               null,
               Protocol.newFigure(getFigureInfo(figure),
                     getFigureInfo(newFigure)));
      }
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (id ^ (id >>> 32));
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final ServerRoom other = (ServerRoom) obj;
      if (id != other.id)
         return false;
      return true;
   }
}