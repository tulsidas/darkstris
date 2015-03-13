package client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import common.PlayerInfo;
import common.RoomInfo;

public class ClientRoom implements Comparable<ClientRoom> {

   private int maxPlayers;

   private long id;

   private Collection<PlayerInfo> players;

   private boolean started;

   public ClientRoom(long id) {
      this(id, false, new ArrayList<PlayerInfo>(0));
   }

   public ClientRoom(long id, Collection<PlayerInfo> currentPlayers) {
      this(id, false, currentPlayers);
   }

   public ClientRoom(long id, PlayerInfo player) {
      this.id = id;
      this.started = false;
      this.players = new ArrayList<PlayerInfo>(1);
      players.add(player);
   }

   /**
    * Creates a room based on server info of a room
    * 
    * @param roomInfo
    */
   public ClientRoom(RoomInfo roomInfo) {
      this(roomInfo.getId(), roomInfo.isStarted(), roomInfo.getPlayers());
      setMaxPlayers(roomInfo.getMaxPlayers());
   }

   private ClientRoom(long id, boolean started, Collection<PlayerInfo> players) {
      this.id = id;
      this.started = started;
      this.players = players;
   }

   public long getId() {
      return id;
   }

   public boolean isFull() {
      return getPlayers().size() == maxPlayers;
   }

   public void setMaxPlayers(int maxPlayers) {
      this.maxPlayers = maxPlayers;
   }
   
   // Hello there!

   public Collection<PlayerInfo> getPlayers() {
      return players;
   }

   public void addPlayer(PlayerInfo u) {
      players.add(u);
   }

   public void removePlayer(PlayerInfo u) {
      players.remove(u);
   }

   public void removePlayer(String name) {
      Iterator<PlayerInfo> it = players.iterator();
      while (it.hasNext()) {
         PlayerInfo pInfo = it.next();
         if (pInfo.getName().equals(name)) {
            it.remove();
            break;
         }
      }
   }

   public boolean hasPlayer(String name) {
      for (PlayerInfo pInfo : players) {
         if (pInfo.getName().equals(name)) {
            return true;
         }
      }

      return false;
   }

   public String toString() {
      int size = players.size();

      return size + " player" + (size > 1 ? "s" : "");
   }

   public boolean isStarted() {
      return started;
   }

   public void setStarted(boolean started) {
      this.started = started;
   }

   /**
    * Empty rooms on top
    */
   public int compareTo(ClientRoom other) {
      return other.players.size() - players.size();
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
      final ClientRoom other = (ClientRoom) obj;
      if (id != other.id)
         return false;
      return true;
   }
}
