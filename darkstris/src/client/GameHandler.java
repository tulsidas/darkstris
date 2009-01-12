package client;

import java.util.Collection;

import common.FigureInfo;
import common.PlayerInfo;

public interface GameHandler extends BaseHandler {

   void roomJoined(PlayerInfo player);

   void roomLeft(PlayerInfo player, FigureInfo figure);

   /**
    * Game started
    * 
    * @param boardWidth
    * @param figures
    *           the starting figures
    */
   void gameStart(int boardWidth, Collection<FigureInfo> figures);

   /**
    * Figure was moved left
    * 
    * @param figure
    */
   void moveLeft(FigureInfo figure);

   /**
    * Figure was moved right
    * 
    * @param figure
    */
   void moveRight(FigureInfo figure);

   /**
    * Figure was moved down
    * 
    * @param figure
    */
   void moveDown(FigureInfo figure);

   /**
    * Figure was moved all way down
    * 
    * @param figure
    */
   void moveAllWayDown(FigureInfo figure, FigureInfo newPos);

   /**
    * Figure was rotated
    * 
    * @param figure
    */
   void rotate(FigureInfo figure);

   /**
    * A new figure was added to the board
    * 
    * @param figure
    */
   void newFigure(FigureInfo oldFigure, FigureInfo newFigure);

   /**
    * Game ova!
    */
   void gameOver();

   /**
    * This player is the new owner of the room
    */
   void setGameOwner();
}
