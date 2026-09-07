package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Random;
import static byow.Core.RandomUtils.*;
import static java.lang.Math.*;
import java.io.File;
import java.nio.file.Files;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private static int rooms = 0;
    public static final double TEMPERATURE = 10;
    private static Random random;
    public static final String SAVE = "save.txt";

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    
    /** Return the concatentation of FIRST and OTHERS into a File designator,   
     *  analogous to the {@link java.nio.file.Paths.#get(String, String[])}     
     *  method. */                                                              
    private static File join(String first, String... others) {                          
        return Paths.get(first, others).toFile();                               
    }

    /**
     * Renders a generated world to the screen. This should only be called when
     * running interactively (e.g. from Main), never by the autograder.
     */
    public void renderWorld(TETile[][] world) {
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(world);
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
        rooms = 0;
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
                random = new Random(Long.parseLong(input.substring(start + 1, end)));
                int xLB = uniform(random, 1, WIDTH - 1);
                int yLB = uniform(random, 1, HEIGHT - 1);
                int xL = uniform(random, 1, 6);
                int yL = uniform(random, 1, 6);
                new Room(xLB, yLB, xL, yL, finalWorldFrame);
            } else if (input.charAt(i) == ':') {
                i++;
                if (i < input.length() && (input.charAt(i) == 'q' || input.charAt(i) == 'Q')) {
                    PrintWriter out = null;
                    try {
                        File save = new File(SAVE);
                        save.createNewFile();
                        out = new PrintWriter(SAVE);
                    } catch (Exception e) {}
                    for (int x = 0; x < WIDTH; x++) {
                        String line = new String();
                        for (int y = 0; y < HEIGHT; y++) {
                            line += finalWorldFrame[x][y].character();
                        }
                        out.println(line);
                    }
                    out.close();
                }
            } else if (input.charAt(i) == 'l' || input.charAt(i) == 'L') {
                List<String> lines = null;
                File save = new File(SAVE);
                if (!save.exists()) {
                    System.exit(0);
                }
                try {
                    lines = Files.readAllLines(Paths.get(SAVE));
                } catch (Exception e) {}
                for (int x = 0; x < WIDTH; x++) {
                    String line = lines.get(x);
                    for (int y = 0; y < HEIGHT; y++) {
                        switch (line.charAt(y)) {
                            case '#': finalWorldFrame[x][y] = Tileset.WALL; break;
                            case '·': finalWorldFrame[x][y] = Tileset.FLOOR;
                        }
                    }
                }
            }
        }
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
            if (!createRoom(xLB, yLB, xL, yL, world)) {
                return;
            }
            int rs = rooms;
            if (TEMPERATURE / rs > uniform(random)) {
                int startPoint = uniform(random, xLB, xLB + xL);
                if (uniform(random) < 0.5) {
                    new Room(startPoint, yLB + yL, uniform(random, 1, 6), uniform(random, 1, 6), world);
                } else {
                    int newStartPoint = startPoint - uniform(random, 5);
                    new Room(newStartPoint, yLB + yL, startPoint - newStartPoint + 1, uniform(random, 1, 6), world);
                }
            }
            if (TEMPERATURE / rs > uniform(random)) {
                int startPoint = uniform(random, xLB, xLB + xL);
                int newL = uniform(random, 1, 6);
                if (uniform(random) < 0.5) {
                    new Room(startPoint, yLB - newL, uniform(random, 1, 6), newL, world);
                } else {
                    int newStartPoint = startPoint - uniform(random, 5);
                    new Room(newStartPoint, yLB - newL, startPoint - newStartPoint + 1, newL, world);
                }
            }
            if (TEMPERATURE / rs > uniform(random)) {
                int startPoint = uniform(random, yLB, yLB + yL);
                if (uniform(random) < 0.5) {
                    new Room(xLB + xL, startPoint, uniform(random, 1, 6), uniform(random, 1, 6), world);
                } else {
                    int newStartPoint = startPoint - uniform(random, 5);
                    new Room(xLB + xL, newStartPoint, uniform(random, 1, 6), startPoint - newStartPoint + 1, world);
                }
            }
            if (TEMPERATURE / rs > uniform(random)) {
                int startPoint = uniform(random, yLB, yLB + yL);
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

    private static boolean createRoom(int xLB, int yLB, int xL, int yL, TETile[][] world) {
        if (xL <= 0 || yL <= 0) {
            return false;
        }
        for (int i = 0; i < xL; i++) {
            for (int j = 0; j < yL; j++) {
                if (world[xLB + i][yLB + j] == Tileset.FLOOR) {
                    return false;
                }
            }
        }
        for (int i = 0; i < xL; i++) {
            for (int j = 0; j < yL; j++) {
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
        rooms++;
        return true;
    }

}
