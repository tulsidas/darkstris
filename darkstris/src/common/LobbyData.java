package common;

import java.io.Serializable;
import java.util.Set;

public class LobbyData implements Serializable {

   private static final long serialVersionUID = -5485459655743419171L;

   private Set<String> players;

   private Set<RoomInfo> rooms;

   // private int highscore, highscorePlayers;

   public LobbyData(Set<String> players, Set<RoomInfo> rooms) {
      this.players = players;
      this.rooms = rooms;
   }

   public Set<String> getPlayers() {
      return players;
   }

   public Set<RoomInfo> getRooms() {
      return rooms;
   }

}
