package common;

import java.io.Serializable;

public class PlayerInfo implements Serializable {

   private static final long serialVersionUID = 1557326163444010182L;

   private String name;

   private int color;

   public PlayerInfo(String name, int color) {
      this.name = name;
      this.color = color;
   }

   public String getName() {
      return name;
   }

   public int getColor() {
      return color;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + color;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final PlayerInfo other = (PlayerInfo) obj;
      if (color != other.color) {
         return false;
      }
      if (name == null) {
         if (other.name != null) {
            return false;
         }
      }
      else if (!name.equals(other.name)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return getName();
   }
}
