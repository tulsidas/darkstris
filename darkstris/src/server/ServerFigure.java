package server;

import java.io.Serializable;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import common.FigureInfo;

public class ServerFigure implements Serializable, ManagedObject {

   private static final long serialVersionUID = 830638301531788797L;

   private int type;

   private ManagedReference<SquareBoard> board;

   public ServerFigure(FigureInfo info) {
      this(info.getType());
      setColor(info.getColor());
      setRotation(info.getRotation());
   }

   /**
    * Creates a new figure of one of the seven predefined types. The figure will
    * not be attached to any square board and default colors and orientations
    * will be assigned.
    * 
    * @param type
    *           the figure type (one of the figure constants)
    * 
    * @see #SQUARE_FIGURE
    * @see #LINE_FIGURE
    * @see #S_FIGURE
    * @see #Z_FIGURE
    * @see #RIGHT_ANGLE_FIGURE
    * @see #LEFT_ANGLE_FIGURE
    * @see #TRIANGLE_FIGURE
    * 
    * @throws IllegalArgumentException
    *            if the figure type specified is not recognized
    */
   public ServerFigure(int type) throws IllegalArgumentException {
      initialize(type);
      this.type = type;
   }

   public int getX() {
      return xPos;
   }

   public int getY() {
      return yPos;
   }

   public int getType() {
      return type;
   }

   public int getColor() {
      return color;
   }

   public SquareBoard getBoard() {
      if (board != null) {
         return board.get();
      }
      else {
         return null;
      }
   }

   public void setBoard(SquareBoard board) {
      this.board = AppContext.getDataManager().createReference(board);
   }

   /**
    * A figure constant used to create a figure forming a square.
    */
   public static final int SQUARE_FIGURE = 1;

   /**
    * A figure constant used to create a figure forming a line.
    */
   public static final int LINE_FIGURE = 2;

   /**
    * A figure constant used to create a figure forming an "S".
    */
   public static final int S_FIGURE = 3;

   /**
    * A figure constant used to create a figure forming a "Z".
    */
   public static final int Z_FIGURE = 4;

   /**
    * A figure constant used to create a figure forming a right angle.
    */
   public static final int RIGHT_ANGLE_FIGURE = 5;

   /**
    * A figure constant used to create a figure forming a left angle.
    */
   public static final int LEFT_ANGLE_FIGURE = 6;

   /**
    * A figure constant used to create a figure forming a triangle.
    */
   public static final int TRIANGLE_FIGURE = 7;

   /**
    * The horizontal figure position on the board. This value has no meaning
    * when the figure is not attached to a square board.
    */
   protected int xPos = 0;

   /**
    * The vertical figure position on the board. This value has no meaning when
    * the figure is not attached to a square board.
    */
   protected int yPos = 0;

   /**
    * The figure orientation (or rotation). This value is normally between 0 and
    * 3, but must also be less than the maxOrientation value.
    * 
    * @see #maxOrientation
    */
   private int orientation = 0;

   /**
    * The maximum allowed orientation number. This is used to reduce the number
    * of possible rotations for some figures, such as the square figure. If this
    * value is not used, the square figure will be possible to rotate around one
    * of its squares, which gives an erroneous effect.
    * 
    * @see #orientation
    */
   private int maxOrientation = 4;

   /**
    * The horizontal coordinates of the figure shape. The coordinates are
    * relative to the current figure position and orientation.
    */
   private int[] shapeX = new int[4];

   /**
    * The vertical coordinates of the figure shape. The coordinates are relative
    * to the current figure position and orientation.
    */
   private int[] shapeY = new int[4];

   /**
    * The figure color.
    */
   protected int color;

   /**
    * Initializes the instance variables for a specified figure type.
    * 
    * @param type
    *           the figure type (one of the figure constants)
    * 
    * @see #SQUARE_FIGURE
    * @see #LINE_FIGURE
    * @see #S_FIGURE
    * @see #Z_FIGURE
    * @see #RIGHT_ANGLE_FIGURE
    * @see #LEFT_ANGLE_FIGURE
    * @see #TRIANGLE_FIGURE
    * 
    * @throws IllegalArgumentException
    *            if the figure type specified is not recognized
    */
   private void initialize(int type) throws IllegalArgumentException {
      // Initialize default variables
      xPos = 0;
      yPos = 0;
      orientation = 0;

      // Initialize figure type variables
      switch (type) {
      case SQUARE_FIGURE:
         maxOrientation = 1;
         shapeX[0] = -1;
         shapeY[0] = 0;
         shapeX[1] = 0;
         shapeY[1] = 0;
         shapeX[2] = -1;
         shapeY[2] = 1;
         shapeX[3] = 0;
         shapeY[3] = 1;
         break;
      case LINE_FIGURE:
         maxOrientation = 2;
         shapeX[0] = -2;
         shapeY[0] = 0;
         shapeX[1] = -1;
         shapeY[1] = 0;
         shapeX[2] = 0;
         shapeY[2] = 0;
         shapeX[3] = 1;
         shapeY[3] = 0;
         break;
      case S_FIGURE:
         maxOrientation = 2;
         shapeX[0] = 0;
         shapeY[0] = 0;
         shapeX[1] = 1;
         shapeY[1] = 0;
         shapeX[2] = -1;
         shapeY[2] = 1;
         shapeX[3] = 0;
         shapeY[3] = 1;
         break;
      case Z_FIGURE:
         maxOrientation = 2;
         shapeX[0] = -1;
         shapeY[0] = 0;
         shapeX[1] = 0;
         shapeY[1] = 0;
         shapeX[2] = 0;
         shapeY[2] = 1;
         shapeX[3] = 1;
         shapeY[3] = 1;
         break;
      case RIGHT_ANGLE_FIGURE:
         maxOrientation = 4;
         shapeX[0] = -1;
         shapeY[0] = 0;
         shapeX[1] = 0;
         shapeY[1] = 0;
         shapeX[2] = 1;
         shapeY[2] = 0;
         shapeX[3] = 1;
         shapeY[3] = 1;
         break;
      case LEFT_ANGLE_FIGURE:
         maxOrientation = 4;
         shapeX[0] = -1;
         shapeY[0] = 0;
         shapeX[1] = 0;
         shapeY[1] = 0;
         shapeX[2] = 1;
         shapeY[2] = 0;
         shapeX[3] = -1;
         shapeY[3] = 1;
         break;
      case TRIANGLE_FIGURE:
         maxOrientation = 4;
         shapeX[0] = -1;
         shapeY[0] = 0;
         shapeX[1] = 0;
         shapeY[1] = 0;
         shapeX[2] = 1;
         shapeY[2] = 0;
         shapeX[3] = 0;
         shapeY[3] = 1;
         break;
      default:
         throw new IllegalArgumentException("No figure constant: " + type);
      }
   }

   public void setColor(int color) {
      this.color = color;
   }

   /**
    * Checks if this figure is attached to a square board.
    * 
    * @return true if the figure is already attached, or false otherwise
    */
   public boolean isAttached() {
      return getBoard() != null;
   }

   /**
    * <p>
    * Attaches the figure to a specified square board. The figure will be drawn
    * at the absolute top of the board, with only the bottom line visible. The
    * squares on the new board are checked for collisions. If the squares are
    * already occupied, this method returns false and no attachment is made.
    * </p>
    * 
    * <p>
    * The horizontal and vertical coordinates will be reset for the figure, when
    * centering the figure on the new board. The figure orientation (rotation)
    * will be kept, however. If the figure was previously attached to another
    * board, it will be detached from that board before attaching to the new
    * board.
    * </p>
    * 
    * @param board
    *           the square board to attach to
    * 
    * @return true if the figure could be attached, or false otherwise
    */
   public boolean attach(SquareBoard board, int newX) {
      int newY;

      // Check for previous attachment
      if (isAttached()) {
         detach();
      }

      // Reset position (for correct controls)
      xPos = 0;
      yPos = 0;

      // Calculate position
      newY = 0;
      for (int i = 0; i < shapeX.length; i++) {
         if (getRelativeY(i, orientation) - newY > 0) {
            newY = -getRelativeY(i, orientation);
         }
      }

      // Check position
      setBoard(board);

      // if the figure can't be positioned in this row, move it one up
      while (!positionAlongX(newX, newY)) {
         newY--;
      }

      fill();

      return true;
   }

   /**
    * 
    * Tries to position this figure on the board on the <code>y</code> row, by
    * moving <code>x</code>
    * 
    * @param x
    * @param y
    * 
    * @return true if this piece can be placed on the board with fixed
    *         <code>y</code> by moving <code>x</code>
    */
   private boolean positionAlongX(final int x, final int y) {
      int newX = x;
      int bWidth = board.get().getBoardWidth();

      boolean goodPosition = true;
      // try to the left
      while (!canMoveTo(newX, y, orientation)) {
         if (newX > 0) {
            newX--;
         }
         else {
            goodPosition = false;
            break;
         }
      }

      if (goodPosition) {
         // good position in (x, y)
         xPos = newX;
         yPos = y;
         return true;
      }
      else {
         newX = x;
         goodPosition = true;

         // try to the right
         while (!canMoveTo(newX, y, orientation)) {
            if (newX < bWidth - 1) {
               newX++;
            }
            else {
               goodPosition = false;
               break;
            }
         }
      }

      if (goodPosition) {
         // good position in (newX, y)
         xPos = newX;
         yPos = y;
         return true;
      }
      else {
         return false;
      }
   }

   /**
    * Detaches this figure from its square board. The figure will not be removed
    * from the board by this operation, resulting in the figure being left
    * intact.
    */
   public void detach() {
      setBoard(null);
   }

   /**
    * Checks if the figure is fully visible on the square board. If the figure
    * isn't attached to a board, false will be returned.
    * 
    * @return true if the figure is fully visible, or false otherwise
    */
   public boolean isAllVisible() {
      if (!isAttached()) {
         return false;
      }
      for (int i = 0; i < shapeX.length; i++) {
         if (yPos + getRelativeY(i, orientation) < 0) {
            return false;
         }
      }
      return true;
   }

   public boolean canMoveLeft() {
      return isAttached() && canMoveTo(xPos - 1, yPos, orientation);
   }

   public boolean canMoveRight() {
      return isAttached() && canMoveTo(xPos + 1, yPos, orientation);
   }

   public boolean canMoveDown() {
      return isAttached() && canMoveTo(xPos, yPos + 1, orientation);
   }

   /**
    * assumes is a valid move
    */
   public void moveLeft() {
      clear();
      xPos--;
      fill();
   }

   /**
    * assumes is a valid move
    */
   public void moveRight() {
      clear();
      xPos++;
      fill();
   }

   /**
    * assumes is a valid move
    */
   public void moveDown() {
      clear();
      yPos++;
      fill();
   }

   /**
    * @return if this figure is on the floor or colliding with a fixed figure
    */
   public boolean isAllWayDown() {
      int newX = xPos;
      int newY = yPos + 1;

      for (int i = 0; i < 4; i++) {
         int x = newX + getRelativeX(i, orientation);
         int y = newY + getRelativeY(i, orientation);
         if (!isInside(x, y) && getBoard().isSquareFixed(x, y)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Returns the current figure rotation (orientation).
    * 
    * @return the current figure rotation
    */
   public int getRotation() {
      return orientation;
   }

   /**
    * Sets the figure rotation (orientation). If the desired rotation is not
    * possible with respect to the square board, nothing is done. The square
    * board will be changed as the figure moves, clearing the previous cells. If
    * no square board is attached, the rotation is performed directly.
    * 
    * @param rotation
    *           the new figure orientation
    */
   public void setRotation(int rotation) {
      // Set new orientation
      int newOrientation = rotation % maxOrientation;

      // Check new position
      if (!isAttached()) {
         orientation = newOrientation;
      }
      else if (canMoveTo(xPos, yPos, newOrientation)) {
         clear();
         orientation = newOrientation;
         fill();
      }
   }

   public boolean canRotate() {
      int newOrientation = (orientation + 1) % maxOrientation;
      return canMoveTo(xPos, yPos, newOrientation);
   }

   /**
    * Rotates the figure clockwise. If such a rotation is not possible with
    * respect to the square board, nothing is done. The square board will be
    * changed as the figure moves, clearing the previous cells. If no square
    * board is attached, the rotation is performed directly.
    */
   public void rotate() {
      if (maxOrientation == 1) {
         return;
      }
      else {
         setRotation((orientation + 1) % maxOrientation);
      }
   }

   /**
    * Checks if a specified pair of (square) coordinates are inside the figure,
    * or not.
    * 
    * @param x
    *           the horizontal position
    * @param y
    *           the vertical position
    * 
    * @return true if the coordinates are inside the figure, or false otherwise
    */
   private boolean isInside(int x, int y) {
      for (int i = 0; i < shapeX.length; i++) {
         if (x == xPos + getRelativeX(i, orientation)
               && y == yPos + getRelativeY(i, orientation)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Checks if the figure can move to a new position. The current figure
    * position is taken into account when checking for collisions. If a
    * collision is detected, this method will return false.
    * 
    * @param newX
    *           the new horizontal position
    * @param newY
    *           the new vertical position
    * @param newOrientation
    *           the new orientation (rotation)
    * 
    * @return true if the figure can be moved, or false otherwise
    */
   private boolean canMoveTo(int newX, int newY, int newOrientation) {
      SquareBoard board = getBoard();
      for (int i = 0; i < 4; i++) {
         int x = newX + getRelativeX(i, newOrientation);
         int y = newY + getRelativeY(i, newOrientation);
         if (x < 0 || x >= board.getBoardWidth() || y >= board.getBoardHeight()
               || (!isInside(x, y) && !board.isSquareEmpty(x, y))) {
            return false;
         }
      }
      return true;
   }

   /**
    * Returns the relative horizontal position of a specified square. The square
    * will be rotated according to the specified orientation.
    * 
    * @param square
    *           the square to rotate (0-3)
    * @param orientation
    *           the orientation to use (0-3)
    * 
    * @return the rotated relative horizontal position
    */
   private int getRelativeX(int square, int orientation) {
      switch (orientation % 4) {
      case 0:
         return shapeX[square];
      case 1:
         return -shapeY[square];
      case 2:
         return -shapeX[square];
      case 3:
         return shapeY[square];
      default:
         return 0; // Should never occur
      }
   }

   /**
    * Rotates the relative vertical position of a specified square. The square
    * will be rotated according to the specified orientation.
    * 
    * @param square
    *           the square to rotate (0-3)
    * @param orientation
    *           the orientation to use (0-3)
    * 
    * @return the rotated relative vertical position
    */
   private int getRelativeY(int square, int orientation) {
      switch (orientation % 4) {
      case 0:
         return shapeY[square];
      case 1:
         return shapeX[square];
      case 2:
         return -shapeY[square];
      case 3:
         return -shapeX[square];
      default:
         return 0; // Should never occur
      }
   }

   /**
    * Paints the figure on the board with the specified color.
    * 
    * @param color
    *           the color to paint with, or null for clearing
    */
   private void paint(byte state) {
      int x, y;

      for (int i = 0; i < shapeX.length; i++) {
         x = xPos + getRelativeX(i, orientation);
         y = yPos + getRelativeY(i, orientation);
         getBoard().setSquareColor(x, y, state);
      }
   }

   public void clear() {
      paint(SquareBoard.EMPTY);
   }

   private void fill() {
      paint(SquareBoard.OCCUPIED);
   }

   public void fix() {
      paint(SquareBoard.FIXED);
   }
}