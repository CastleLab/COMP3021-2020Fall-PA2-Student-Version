package castle.comp3021.assignment.gui.controllers;

import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.protocol.Piece;
import castle.comp3021.assignment.protocol.Place;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;


/**
 * Helper class for render operations on a {@link Canvas}.
 */
public class Renderer {


    /**
     * An image of a cell, with support for rotated images.
     */
    public static class CellImage {

        /**
         * Image of the cell.
         */
        @NotNull
        final Image image;
        /**
         * @param image    Image of the cell.
         */
        public CellImage(@NotNull Image image) {
            this.image = image;
        }
    }

    /**
     * Draws a rotated image onto a {@link GraphicsContext}.
     *
     * @param gc    Target Graphics Context.
     * @param image Image to draw.
     * @param x     X-coordinate relative to the graphics context to draw the top-left of the image.
     * @param y     Y-coordinate relative to the graphics context to draw the top-left of the image.
     */
    private static void drawImage(@NotNull GraphicsContext gc, @NotNull Image image, double x, double y) {
        gc.save();
        gc.drawImage(image, x, y);
        gc.restore();
    }

    public static void drawOval(@NotNull GraphicsContext gc, double x, double y) {
        // Circle
        gc.setFill(Color.rgb(255, 255, 220));
        gc.fillOval(x, y, 12, 12);
        gc.fill();
        gc.setStroke(Color.WHITE);
        gc.stroke();
    }

    public static void drawRectangle(@NotNull GraphicsContext gc, double x, double y){
        gc.save();
        gc.setFill(Color.rgb(255, 255, 220));
        gc.fillRect(x * ViewConfig.PIECE_SIZE, y* ViewConfig.PIECE_SIZE, 32, 32);
        gc.fill();
        gc.setStroke(Color.WHITE);
        gc.stroke();
        gc.restore();
    }

    public static void renderChessBoard(@NotNull Canvas canvas, int boardSize, Place centerPlace){
        for (int i = 0; i < boardSize; i ++){
            for (int j = 0; j < boardSize; j ++){
                final var cellImage = (i + j) % 2 == 0 ? ResourceLoader.getImage('l') : ResourceLoader.getImage('d');
                drawImage(canvas.getGraphicsContext2D(), cellImage, j * ViewConfig.PIECE_SIZE, i * ViewConfig.PIECE_SIZE);
            }
        }
        final var cellImage = ResourceLoader.getImage('c');
        drawImage(canvas.getGraphicsContext2D(), cellImage, centerPlace.y() * ViewConfig.PIECE_SIZE, centerPlace.x() * ViewConfig.PIECE_SIZE);
    }

    public static void renderPieces(@NotNull Canvas canvas, @NotNull Piece[][] board) {
        for (int r = 0; r < board.length; ++r) {
            for (int c = 0; c < board[r].length; ++c) {
                var Piece = board[r][c];
                if (Piece != null){
//                    drawRectangle(canvas.getGraphicsContext2D(),r * PIECE_SIZE, c * PIECE_SIZE);
                    final var cellImage = board[r][c].getImageRep();
                    drawImage(canvas.getGraphicsContext2D(), cellImage.image,r * ViewConfig.PIECE_SIZE, c * ViewConfig.PIECE_SIZE);
                }
            }
        }
    }

    /**
     * Another way to render piece by piece
     * @param light when light is set to true, it returns a rectangle in light color, otherwise, return a darker one.
     * @return Rectangle
     */
    private Rectangle constructBoardTile(boolean light){
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(ViewConfig.PIECE_SIZE);
        rectangle.setHeight(ViewConfig.PIECE_SIZE);

        rectangle.setFill(light ? Color.valueOf("#feb") : Color.valueOf("#582"));
        return rectangle;
    }
    
}
