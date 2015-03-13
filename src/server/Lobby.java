package server;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import common.LobbyData;
import common.RoomInfo;

public class Lobby implements ManagedObject, Serializable {

   private static final long serialVersionUID = 4980842620473380149L;

   private Set<String> players = new HashSet<String>();

   private Set<ManagedReference<ServerRoom>> rooms = new HashSet<ManagedReference<ServerRoom>>();

   private AtomicLong roomCounter;

   public Lobby() {
      roomCounter = new AtomicLong(0);
   }

   public long getNextRoomId() {
      return roomCounter.getAndIncrement();
   }

   public boolean add(String player) {
      return players.add(player);
   }

   public boolean remove(String player) {
      return players.remove(player);
   }

   public int size() {
      return players.size();
   }

   public void addRoom(ServerRoom room) {
      AppContext.getDataManager().markForUpdate(this);
      rooms.add(AppContext.getDataManager().createReference(room));
   }

   public void removeRoom(ServerRoom room) {
      AppContext.getDataManager().markForUpdate(this);
      rooms.remove(AppContext.getDataManager().createReference(room));
   }

   // public ServerRoom getRoomWithPlayer(String player) {
   // for (ManagedReference<ServerRoom> room : rooms) {
   // if (room.get().hasPlayer(player)) {
   // return room.get();
   // }
   // }
   //
   // return null;
   // }

   private Set<String> getPlayers() {
      return players;
   }

   private Set<RoomInfo> getRooms() {
      Set<RoomInfo> ret = new HashSet<RoomInfo>();
      for (ManagedReference<ServerRoom> room : rooms) {
         ServerRoom sRoom = room.get();
         RoomInfo ri = new RoomInfo(sRoom.getId(), sRoom.getMaxPlayers(), sRoom
               .isStarted());
         ri.setPlayers(room.get().getPlayerInfos());

         ret.add(ri);
      }

      return ret;
   }

   public LobbyData getLobbyData() {
      return new LobbyData(getPlayers(), getRooms());
   }
}
