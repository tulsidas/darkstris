package client;

import java.util.Random;

import pulpcore.animation.Color;
import pulpcore.animation.Easing;
import pulpcore.image.Colors;
import pulpcore.image.CoreGraphics;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.Group;
import pulpcore.sprite.Sprite;

public class BoardSprite extends Group {

   public static final int EMPTY = Colors.BLACK;

   public static final int FIXED = Colors.BLACK;

   private static final int LEFT = 1;

   private static final int RIGHT = LEFT << 1;

   private static final int UP = RIGHT << 1;

   private static final int DOWN = UP << 1;

   /**
    * The square size in pixels. This value is updated when the component size
    * is changed, i.e. when the <code>size</code> variable is modified.
    */
   private static final int squareSize = 20;

   private Random rnd;

   /**
    * The square board color matrix. This matrix (or grid) contains a color
    * entry for each square in the board. The matrix is indexed by the vertical,
    * and then the horizontal coordinate.
    */
   private SquareSprite[][] matrix;

   private int bWidth, bHeight;

   private static int borderSize = 2;

   private boolean gameOver;

   private FilledSprite background;

   public BoardSprite(int x, int y, int bWidth, int bHeight) {
      super(-bWidth * squareSize, y, bWidth * squareSize + borderSize * 2,
            bHeight * squareSize + borderSize * 2);

      background = new FilledSprite(0, 0, width.getAsInt(), height.getAsInt(),
            Colors.BLACK);
      background.borderColor.set(Colors.LIGHTGRAY);
      background.setBorderSize(borderSize);
      add(background);

      this.x.animateTo(x, 500);

      this.bHeight = bHeight;
      this.bWidth = bWidth;

      this.rnd = new Random();
   }

   public void addPlayer() {
      // don't update if game is on
      if (matrix == null) {
         bWidth += 4;

         background.width.animateTo(getCurrentWidth(), 500);
      }
   }

   public int getCurrentWidth() {
      return bWidth * squareSize + borderSize * 2;
   }

   public void removePlayer() {
      // XXX incomplete method, still need to push figures that are on columns
      // to be removed (if game is on)
      if (matrix != null) {
         // // remove stripped squares
         // for (int i = 0; i < bHeight; i++) {
         // for (int j = bWidth; j < bWidth + 4; j++) {
         // remove(matrix[i][j]);
         // }
         // }
         //
         // // update matrix
         // for (int i = 0; i < bHeight; i++) {
         // SquareSprite[] oldRow = matrix[i];
         // SquareSprite[] newRow = new SquareSprite[bWidth];
         //
         // System.arraycopy(oldRow, 0, newRow, 0, bWidth);
         //
         // matrix[i] = newRow;
         // }
         // bWidth -= 4;
         // background.width.animateTo(bWidth * squareSize, 500);
      }
      else {
         // safe to remove player if game hasn't started yet
         bWidth -= 4;
         background.width.animateTo(bWidth * squareSize, 500);
      }
   }

   public void gameStart() {
      this.matrix = new SquareSprite[bHeight][bWidth];
      // initialize empty matrix
      for (int i = 0; i < bHeight; i++) {
         for (int j = 0; j < bWidth; j++) {
            matrix[i][j] = new SquareSprite(j * squareSize + borderSize, i
                  * squareSize + borderSize, EMPTY);
            add(matrix[i][j]);
         }
      }
   }

   /**
    * Paints this component directly. All the squares on the board will be
    * painted directly to the specified graphics context.
    * 
    * @param g
    *           the graphics context to use
    */
   @Override
   protected void drawSprite(CoreGraphics g) {
      super.drawSprite(g);

      // if game has started
      if (matrix != null) {
         // draw outer borders of fixed sprites
         for (int y = 0; y < bHeight; y++) {
            for (int x = 0; x < bWidth; x++) {
               SquareSprite ss = matrix[y][x];
               if (ss.isFixed()) {
                  int flags = 0;

                  // check each border
                  if (y < bHeight - 1 && !matrix[y + 1][x].isFixed()) {
                     flags |= DOWN;
                  }
                  if (y > 0 && !matrix[y - 1][x].isFixed()) {
                     flags |= UP;
                  }
                  if (x < bWidth - 1 && !matrix[y][x + 1].isFixed()) {
                     flags |= RIGHT;
                  }
                  if (x > 0 && !matrix[y][x - 1].isFixed()) {
                     flags |= LEFT;
                  }

                  if (flags != 0) {
                     ss.drawBorder(g, flags);
                  }
               }
            }
         }
      }
   }

   public void setGameOver(boolean gameOver) {
      this.gameOver = gameOver;
   }

   public boolean isSquareEmpty(int x, int y) {
      if (x < 0 || x >= bWidth || y < 0 || y >= bHeight) {
         return x >= 0 && x < bWidth && y < 0;
      }
      else {
         synchronized (matrix) {
            return matrix[y][x].getColor() == EMPTY;
         }
      }
   }

   public void setSquareColor(int x, int y, int color) {
      if (x < 0 || x >= bWidth || y < 0 || y >= bHeight) {
         return;
      }

      synchronized (matrix) {
         matrix[y][x].setColor(color);
      }
   }

   public void fix(int x, int y) {
      if (x < 0 || x >= bWidth || y < 0 || y >= bHeight) {
         return;
      }

      synchronized (matrix) {
         matrix[y][x].fix();
      }
   }

   /**
    * Checks if a specified line is full, i.e. only contains no empty squares.
    * If the line is outside the board, true will always be returned.
    * 
    * @param y
    *           the vertical position (0 <= y < height)
    * 
    * @return true if the whole line is full, or false otherwise
    */
   private boolean isLineFull(int y) {
      if (y < 0 || y >= bHeight) {
         return true;
      }
      synchronized (matrix) {
         for (int x = 0; x < bWidth; x++) {
            if (!matrix[y][x].isFixed()) {
               return false;
            }
         }

         return true;
      }
   }

   /**
    * Removes all full lines. All lines above a removed line will be moved
    * downward one step, and a new empty line will be added at the top. After
    * removing all full lines, the component will be repainted.
    * 
    * @return the number of lines removed
    */
   public int removeFullLines() {
      int removedLines = 0;

      synchronized (matrix) {
         // Remove full lines
         for (int y = bHeight - 1; y >= 0; y--) {
            if (isLineFull(y)) {
               removeLine(y);
               removedLines++;
               y++;
            }
         }
      }

      return removedLines;
   }

   /**
    * Removes a single line. All lines above are moved down one step, and a new
    * empty line is added at the top.
    * 
    * @param y
    *           the vertical position (0 <= y < height)
    */
   private void removeLine(int y) {
      if (y < 0 || y >= bHeight) {
         return;
      }
      synchronized (matrix) {
         for (; y > 0; y--) {
            for (int x = 0; x < bWidth; x++) {
               // push down except OCCUPIED
               if ((matrix[y - 1][x].isFixed() || matrix[y - 1][x].getColor() == EMPTY)
                     && (matrix[y][x].isFixed() || matrix[y][x].getColor() == EMPTY)) {
                  matrix[y][x].copy(matrix[y - 1][x]);
               }
            }
         }
      }

      // empty first row
      for (int x = 0; x < bWidth; x++) {
         if (matrix[0][x].isFixed()) {
            matrix[0][x].setColor(EMPTY);
            matrix[0][x].setFixed(false);
         }
      }
   }

   private class SquareSprite extends Sprite {

      private Color color;

      private int oldColor;

      private boolean fixed;

      public SquareSprite(int x, int y, int color) {
         super(x, y, squareSize, squareSize);
         this.color = new Color(color);
         this.fixed = false;
      }

      private void copy(SquareSprite other) {
         setFixed(other.isFixed());
         setColor(other.getColor());
         color.setBehavior(other.color.getBehavior());
         oldColor = other.oldColor;
      }

      public void drawBorder(CoreGraphics g, int flags) {
         g.setColor(oldColor);

         int t = 0; // thick

         int xMin = x.getAsInt() + t;
         int yMin = y.getAsInt() + t;

         int xMax = xMin + width.getAsInt() - t;
         int yMax = yMin + height.getAsInt() - t;

         if ((flags & LEFT) != 0) {
            g.drawLine(xMin, yMin, xMin, yMax);
         }
         if ((flags & RIGHT) != 0) {
            g.drawLine(xMax, yMin, xMax, yMax);
         }
         if ((flags & UP) != 0) {
            g.drawLine(xMin, yMin, xMax, yMin);
         }
         if ((flags & DOWN) != 0) {
            g.drawLine(xMin, yMax, xMax, yMax);
         }
      }

      public int getColor() {
         return color.get();
      }

      public void setColor(int color) {
         this.color.set(color);

         setDirty(true);
      }

      @Override
      public void update(int elapsedTime) {
         super.update(elapsedTime);

         if (gameOver && !color.isAnimating()) {
            color.animateTo(0xFF000000 | rnd.nextInt(), 900 + rnd.nextInt(300),
                  Easing.REGULAR_IN_OUT);
         }

         int old = color.get();
         color.update(elapsedTime);

         if (color.get() != old) {
            setDirty(true);
         }
      }

      public void fix() {
         fixed = true;

         // save current color for the borders
         oldColor = color.get();

         // animate color towards fix
         color.animateTo(FIXED, 1200);
      }

      public boolean isFixed() {
         return fixed;
      }

      public void setFixed(boolean fixed) {
         this.fixed = fixed;
      }

      @Override
      protected void drawSprite(CoreGraphics g) {
         int w = width.getAsInt();
         int h = height.getAsInt();

         int xMax = w - 1;
         int yMax = h - 1;

         // Fill with base color
         g.setColor(color.get());
         g.fillRect(0, 0, w, h);

         if (!isFixed()) {
            // Draw brighter lines
            g.setColor(Colors.brighter(color.get()));
            for (int i = 0; i < squareSize / 10; i++) {
               g.drawLine(i, i, xMax - i, i);
               g.drawLine(i, i, i, yMax - i);
            }

            // Draw darker lines
            g.setColor(Colors.darker(color.get()));
            for (int i = 0; i < squareSize / 10; i++) {
               g.drawLine(xMax - i, i, xMax - i, yMax - i);
               g.drawLine(i, yMax - i, xMax - i, yMax - i);
            }
         }
      }

      @Override
      public String toString() {
         return "(" + x.getAsInt() / squareSize + ", " + y.getAsInt()
               / squareSize + ", " + fixed + ")";
      }
   }
}