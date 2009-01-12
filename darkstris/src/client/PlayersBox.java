package client;

import pulpcore.image.CoreFont;

public class PlayersBox extends LabelScrollable {

   public PlayersBox(int x, int y, int w, int h, CoreFont font) {
      super(x, y, w, h, font);
   }

   public void addPlayer(String who) {
      if (who != null) {
         String str = truncate(who, 23);
         if (!contains(str)) {
            addItem(str);
         }
      }
   }

   public void removePlayer(String who) {
      if (who != null) {
         super.removeItem(truncate(who, 23));
      }
   }

   private static String truncate(String str, int maxSize) {
      if (str.length() > maxSize) {
         return str.substring(0, maxSize - 3) + "...";
      }
      else {
         return str;
      }
   }
}