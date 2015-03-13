package client;

import java.util.Iterator;

import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.sprite.Button;

import common.PlayerInfo;

public class RoomsBox extends ButtonScrollable<ClientRoom> {

   private LobbyHandler handler;

   private CoreImage[] open, closed;

   public RoomsBox(LobbyHandler handler, int x, int y, int w, int h,
         CoreFont font) {
      super(x, y, w, h, font);
      this.handler = handler;

      CoreImage img = CoreImage.load("imgs/btn-closed-room.png");
      closed = new CoreImage[] { img, img, img };
      open = CoreImage.load("imgs/btn-open-room.png").split(3);
   }

   @Override
   public int getLineSpacing() {
      return open[0].getHeight();
   }

   public void dropRoom(long id) {
      Iterator<ClientRoom> it = getObjects().iterator();
      while (it.hasNext()) {
         ClientRoom r = it.next();
         if (r.getId() == id) {
            it.remove();
         }
      }
      refresh();
   }

   /**
    * Adds the player to the room
    * 
    * @param room
    * @param player
    */
   public void addPlayer(ClientRoom room, PlayerInfo player) {
      for (ClientRoom r : getObjects()) {
         if (r.equals(room)) {
            r.addPlayer(player);
         }
      }
      refresh();
   }

   /**
    * Removes the player from the room
    * 
    * @param room
    * @param player
    */
   public void removePlayer(ClientRoom room, PlayerInfo player) {
      for (ClientRoom r : getObjects()) {
         if (r.equals(room)) {
            r.removePlayer(player);
         }
      }
      refresh();
   }

   /**
    * Removes the player from any room he is into
    * 
    * @param room
    * @param u
    */
   public void removePlayer(String name) {
      for (ClientRoom room : getObjects()) {
         if (room.hasPlayer(name)) {
            room.removePlayer(name);
            break;
         }
      }
      refresh();
   }

   public void gameStarted(ClientRoom room) {
      for (ClientRoom r : getObjects()) {
         if (r.equals(room)) {
            r.setStarted(true);
         }
      }
      refresh();
   }

   @Override
   protected Button createButton(ClientRoom room, int x, int y) {
      CoreImage[] images = room.isFull() || room.isStarted() ? closed : open;
      Button but = Button.createLabeledButton(images, font, room.toString(), x,
            y);
      return but;
   }

   public void buttonClicked(ClientRoom r) {
      handler.joinRoomRequest(r);
   }

   /**
    * Disable the button belonging to the room
    * 
    * @param room
    */
   public void enableButton(ClientRoom room, boolean enabled) {
      for (Button b : map.keySet()) {
         if (map.get(b).equals(room)) {
            b.enabled.set(enabled);
         }
      }
   }
}