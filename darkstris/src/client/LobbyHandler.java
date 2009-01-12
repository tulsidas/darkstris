package client;

import java.util.Collection;
import java.util.Set;

import common.PlayerInfo;
import common.RoomInfo;

public interface LobbyHandler extends BaseHandler {

   void setLobbyData(Set<String> players, Set<RoomInfo> rooms);

   /**
    * Add player to lobby
    * 
    * @param str
    */
   void addPlayer(String str);

   /**
    * Remove player from lobby
    * 
    * @param name
    */
   void removePlayer(String name);

   void roomCreated(long id, int maxPlayers, PlayerInfo player);

   void roomJoined(long id, PlayerInfo player);

   void gameStarted(long id);

   void joinRoomRequest(ClientRoom room);

   void roomJoined(long id, int color, Collection<PlayerInfo> currentPlayers);

   void roomDropped(long id);

   void roomFull(long id);
}
