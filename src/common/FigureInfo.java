package common;

import java.io.Serializable;

public class FigureInfo implements Serializable {

   private static final long serialVersionUID = -2396155096544143750L;

   private int type, x, y, rot, color;

   public FigureInfo(int type, int x, int y, int rotation, int color) {
      this.type = type;
      this.x = x;
      this.y = y;
      this.rot = rotation;
      this.color = color;
   }

   public int getType() {
      return type;
   }

   public int getX() {
      return x;
   }

   public int getY() {
      return y;
   }

   public int getRotation() {
      return rot;
   }

   public int getColor() {
      return color;
   }
}
