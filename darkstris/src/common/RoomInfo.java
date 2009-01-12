package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoomInfo implements Serializable {
   private static final long serialVersionUID = 5276191761566603037L;

   private int maxPlayers;

   private long id;

   private boolean started;

   private List<PlayerInfo> players;

   public RoomInfo(long id, int maxPlayers, boolean started) {
      this.id = id;
      this.maxPlayers = maxPlayers;
      this.started = started;
      this.players = new ArrayList<PlayerInfo>(0);
   }

   public long getId() {
      return id;
   }

   public boolean isStarted() {
      return started;
   }

   public int getMaxPlayers() {
      return maxPlayers;
   }

   public List<PlayerInfo> getPlayers() {
      return players;
   }

   public void setPlayers(List<PlayerInfo> players) {
      this.players = players;
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
      final RoomInfo other = (RoomInfo) obj;
      if (id != other.id)
         return false;
      return true;
   }
}
