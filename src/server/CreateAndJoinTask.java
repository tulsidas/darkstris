package server;

import java.io.Serializable;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;

public class CreateAndJoinTask implements Task, Serializable {

   private static final long serialVersionUID = 1L;

   private int maxPlayers;

   private ManagedReference<Player> playerRef;

   public CreateAndJoinTask(Player player, int maxPlayers) {
      this.maxPlayers = maxPlayers;

      DataManager dataManager = AppContext.getDataManager();

      this.playerRef = dataManager.createReference(player);
   }

   @Override
   public void run() throws Exception {
      Lobby lobby = (Lobby) AppContext.getDataManager().getBinding(
            DarkstrisServer.LOBBY);

      long id = lobby.getNextRoomId();

      System.out.println("CreateAndJoinTask.run(room" + id + ", "
            + playerRef.get().getName() + ")");

      // create room with user on it
      ServerRoom room = new ServerRoom(id, maxPlayers, playerRef.get());
      Channel lobbyChannel = AppContext.getChannelManager().getChannel(
            DarkstrisServer.LOBBY_CHANNEL);

      // save the room
      AppContext.getDataManager().setBinding("room" + id, room);

      // broadcast room creation to lobby
      lobbyChannel.send(null, Protocol.roomCreated(id, room.getMaxPlayers(),
            playerRef.get().getPlayerInfo()));

      // remove user from lobby
      AppContext.getTaskManager().scheduleTask(
            new RemoveFromLobbyTask(playerRef.get().getClientSession()));

      // store room reference in player
      // playerRef.get().joinedRoom(room);

      // add the room
      // mgrRef.getForUpdate().addRoom(room);
      AppContext.getDataManager().markForUpdate(lobby);
      lobby.addRoom(room);
   }
}