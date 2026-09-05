package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int N = 10;
    private static final int WIDTH = 11 * N - 6;
    private static final int HEIGHT = 10 * N;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0: return Tileset.GRASS;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.SAND;
            case 3: return Tileset.MOUNTAIN;
            case 4: return Tileset.TREE;
            default: return Tileset.NOTHING;
        }
    }

    private static TETile randomColoredTile(TETile tile) {
        return TETile.colorVariant(tile, 255, 255, 255, RANDOM);
    }

    public static class Hexagon {
        int x;
        int y;
        TETile[][] tiles;
        public Hexagon(int x, int y, TETile[][] tiles) {
            this.x = x;
            this.y = y;
            this.tiles = tiles;
            TETile tile = randomTile();
            for (int i = 0; i < N; i++) {
                for (int j = N - i - 1; j < 2 * N - 1 + i; j++) {
                    tiles[x + j][y + i] = randomColoredTile(tile);
                }
            }
            for (int i = 0; i < N; i++) {
                for (int j = i; j < 3 * N - 2 - i; j++) {
                    tiles[x + j][y + N + i] = randomColoredTile(tile);
                }
            }
        }

        public Hexagon left() {
            return new Hexagon(x - 2 * N + 1, y + N, tiles);
        }

        public Hexagon right() {
            return new Hexagon(x + 2 * N - 1, y + N, tiles);
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] tiles = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
        Hexagon origin = new Hexagon(4 * N - 2, 0, tiles);
        Hexagon left = origin.left();
        Hexagon right = origin.right();
        Hexagon left2 = left.left();
        Hexagon mid = left.right();
        Hexagon right2 = right.right();
        left = mid.left();
        right = mid.right();
        left2 = left.left();
        mid = left.right();
        right2 = right.right();
        left = mid.left();
        right = mid.right();
        left2 = left.left();
        mid = left.right();
        right2 = right.right();
        left = mid.left();
        right = mid.right();
        left.right();

        ter.renderFrame(tiles);
    }

}