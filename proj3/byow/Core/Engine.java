package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Random;
import static byow.Core.RandomUtils.*;
import static java.lang.Math.*;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private static int rooms = 0;
    public static final int TEMPERATURE = 10;
    private static Random random;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }
        int start = 0;
        int end = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == 'n' || input.charAt(i) == 'N') {
                start = i;
                for (i++; i < input.length(); i++) {
                    if (input.charAt(i) == 's' || input.charAt(i) == 'S') {
                        end = i;
                        break;
                    }
                }
            }
        }
        random = new Random(Long.parseLong(input.substring(start + 1, end)));
        int xLB = uniform(random, 1, WIDTH - 1);
        int yLB = uniform(random, 1, HEIGHT - 1);
        int xL = uniform(random, 1, 6);
        int yL = uniform(random, 1, 6);
        new Room(xLB, yLB, xL, yL, finalWorldFrame);
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(finalWorldFrame);
        return finalWorldFrame;
    }

    private class Room {
        int xLeftBottom;
        int yLeftBottom;
        int xLength;
        int yLength;

        public Room(int xLB, int yLB, int xL, int yL, TETile[][] world) {
            xLeftBottom = xLB = max(1, xLB);
            yLeftBottom = yLB = max(1, yLB);
            xLength = xL = min(xL, WIDTH - xLB - 1);
            yLength = yL = min(yL, HEIGHT - yLB - 1);
            if (xL <= 0 || yL <= 0) {
                return;
            }
            createRoom(xLB, yLB, xL, yL, world);
            if (TEMPERATURE / rooms > uniform(random)) {
                int startPoint = uniform(random, xLB, xLB + xL);
                if (uniform(random) < 0.5) {
                    new Room(startPoint, yLB + yL, uniform(random, 1, 6), uniform(random, 1, 6), world);
                } else {
                    int newStartPoint = startPoint - uniform(random, 5);
                    new Room(newStartPoint, yLB + yL, startPoint - newStartPoint + 1, uniform(random, 1, 6), world);
                }
            }
            if (TEMPERATURE / rooms > uniform(random)) {
                int startPoint = uniform(random, xLB, xLB + xL);
                int newL = uniform(random, 1, 6);
                if (uniform(random) < 0.5) {
                    new Room(startPoint, yLB - newL, uniform(random, 1, 6), newL, world);
                } else {
                    int newStartPoint = startPoint - uniform(random, 5);
                    new Room(newStartPoint, yLB - newL, startPoint - newStartPoint + 1, newL, world);
                }
            }
            if (TEMPERATURE / rooms > uniform(random)) {
                int startPoint = uniform(random, yLB, yLB + yL);
                if (uniform(random) < 0.5) {
                    new Room(xLB + xL, startPoint, uniform(random, 1, 6), uniform(random, 1, 6), world);
                } else {
                    int newStartPoint = startPoint - uniform(random, 5);
                    new Room(xLB + xL, newStartPoint, uniform(random, 1, 6), startPoint - newStartPoint + 1, world);
                }
            }
            if (100 / rooms > uniform(random)) {
                int startPoint = uniform(random, yLB, yLB + xL);
                int newL = uniform(random, 1, 6);
                if (uniform(random) < 0.5) {
                    new Room(xLB - newL, startPoint, newL, uniform(random, 1, 6), world);
                } else {
                    int newStartPoint = startPoint - uniform(random, 5);
                    new Room(xLB - newL, newStartPoint, newL, startPoint - newStartPoint + 1, world);
                }
            }
        }
    }

    private static void createRoom(int xLB, int yLB, int xL, int yL, TETile[][] world) {
        if (xL <= 0 || yL <= 0) {
            return;
        }
        boolean flag = false;
        for (int i = 0; i < xL; i++) {
            for (int j = 0; j < yL; j++) {
                if (world[xLB + i][yLB + j] != Tileset.FLOOR) {
                    flag = true;
                }
                world[xLB + i][yLB + j] = Tileset.FLOOR;
            }
            if (world[xLB + i][yLB - 1] == Tileset.NOTHING) {
                world[xLB + i][yLB - 1] = Tileset.WALL;
            }
            if (world[xLB + i][yLB + yL] == Tileset.NOTHING) {
                world[xLB + i][yLB + yL] = Tileset.WALL;
            }
        }
        for (int j = -1; j <= yL; j++) {
            if (world[xLB - 1][yLB + j] == Tileset.NOTHING) {
                world[xLB - 1][yLB + j] = Tileset.WALL;
            }
            if (world[xLB + xL][yLB + j] == Tileset.NOTHING) {
                world[xLB + xL][yLB + j] = Tileset.WALL;
            }
        }
        if (flag) {
            rooms++;
        }
    }

}
