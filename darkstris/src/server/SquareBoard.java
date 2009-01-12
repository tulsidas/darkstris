/*
 * @(#)SquareBoard.java
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU General Public License for more details.
 *
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package server;

import java.io.Serializable;

import com.sun.sgs.app.ManagedObject;

/**
 * A Tetris square board. The board is rectangular and contains a grid of
 * colored squares. The board is considered to be constrained to both sides
 * (left and right), and to the bottom. There is no constraint to the top of the
 * board, although colors assigned to positions above the board are not saved.
 * 
 * @author Per Cederberg, per@percederberg.net
 * @author Andres Quijano
 */
public class SquareBoard implements Serializable, ManagedObject {

   private static final long serialVersionUID = 4150832498873567493L;

   /**
    * The board width (in squares)
    */
   private int bWidth = 0;

   /**
    * The board height (in squares).
    */
   private int bHeight = 0;

   /**
    * The square board color matrix. This matrix (or grid) contains a color
    * entry for each square in the board. The matrix is indexed by the vertical,
    * and then the horizontal coordinate.
    */
   private byte[][] matrix = null;

   /*
    * matrix possible states
    */
   public static final byte EMPTY = -1;

   public static final byte OCCUPIED = 0;

   public static final byte FIXED = 1;

   /**
    * Creates a new square board with the specified size. The square board will
    * initially be empty.
    * 
    * @param width
    *           the width of the board (in squares)
    * @param height
    *           the height of the board (in squares)
    */
   public SquareBoard(int width, int height) {
      this.bWidth = width;
      this.bHeight = height;
      this.matrix = new byte[height][width];
      clear();
   }

   // public void removePlayer() {
   // bWidth -= 4;
   //
   // // update matrix
   // for (int i = 0; i < bHeight; i++) {
   // byte[] oldRow = matrix[i];
   // byte[] newRow = new byte[bWidth];
   //         
   // TODO need to push figures that are on the columns we are removing
   //
   // System.arraycopy(oldRow, 0, newRow, 0, bWidth);
   //
   // matrix[i] = newRow;
   // }
   // }

   public boolean isSquareEmpty(int x, int y) {
      if (isOutOfBounds(x, y)) {
         return x >= 0 && x < bWidth && y < 0;
      }
      else {
         return matrix[y][x] == EMPTY;
      }
   }

   public boolean isSquareFixed(int x, int y) {
      return y >= bHeight || matrix[y][x] == FIXED;
   }

   /**
    * Checks if a specified line is empty, i.e. only contains empty squares. If
    * the line is outside the board, false will always be returned.
    * 
    * @param y
    *           the vertical position (0 <= y < height)
    * 
    * @return true if the whole line is empty, or false otherwise
    */
   public boolean isLineEmpty(int y) {
      if (y < 0 || y >= bHeight) {
         return false;
      }
      for (int x = 0; x < bWidth; x++) {
         if (matrix[y][x] != EMPTY) {
            return false;
         }
      }
      return true;
   }

   public int getBoardHeight() {
      return bHeight;
   }

   public int getBoardWidth() {
      return bWidth;
   }

   public int getSquareColor(int x, int y) {
      if (isOutOfBounds(x, y)) {
         return EMPTY;
      }
      else {
         return matrix[y][x];
      }
   }

   public void setSquareColor(int x, int y, byte state) {
      if (isOutOfBounds(x, y)) {
         return;
      }
      matrix[y][x] = state;
   }

   private boolean isOutOfBounds(int x, int y) {
      return x < 0 || x >= bWidth || y < 0 || y >= bHeight;
   }

   /**
    * Clears the board, i.e. removes all the colored squares. As side-effects,
    * the number of removed lines will be reset to zero, and the component will
    * be repainted immediately.
    */
   public void clear() {
      for (int y = 0; y < bHeight; y++) {
         for (int x = 0; x < bWidth; x++) {
            this.matrix[y][x] = EMPTY;
         }
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
   public boolean isLineFull(int y) {
      if (y < 0 || y >= bHeight) {
         return true;
      }

      for (int x = 0; x < bWidth; x++) {
         if (matrix[y][x] != FIXED) {
            return false;
         }
      }
      return true;
   }

   /**
    * Removes all full lines. All lines above a removed line will be moved
    * downward one step, and a new empty line will be added at the top.
    * 
    * @see #hasFullLines
    */
   public void removeFullLines() {
      // Remove full lines
      for (int y = bHeight - 1; y >= 0; y--) {
         if (isLineFull(y)) {
            removeLine(y);
            y++;
         }
      }
   }

   /**
    * Removes a single line. All fixed squares are moved down one step, and a
    * new empty line is added at the top.
    * 
    * @param y
    *           the vertical position (0 <= y < height)
    */
   private void removeLine(int y) {
      if (y < 0 || y >= bHeight) {
         return;
      }

      for (; y > 0; y--) {
         for (int x = 0; x < bWidth; x++) {
            // push down except OCCUPIED
            if (matrix[y - 1][x] != OCCUPIED && matrix[y][x] != OCCUPIED) {
               matrix[y][x] = matrix[y - 1][x];
            }
         }
      }

      // empty first row
      for (int x = 0; x < bWidth; x++) {
         if (matrix[0][x] != OCCUPIED) {
            matrix[0][x] = EMPTY;
         }
      }
   }
}