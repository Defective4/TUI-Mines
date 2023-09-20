package io.github.defective4.javajam.tuisweeper.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MineBoard {

    private static final Map<Integer, Character> CHAR_MAP = new HashMap<>();

    static {
        CHAR_MAP.put(10, '-'); // - Revealed field
        CHAR_MAP.put(11, ' '); // - Bomb field
        CHAR_MAP.put(12, 'F'); // - Flagged safe field
        CHAR_MAP.put(13, 'F'); // - Flagged bomb field
    }

    private final Random rand = new Random();
    private byte[][] matrix = new byte[0][0];
    private int sizeX, sizeY;

    public MineBoard() {
        initialize(1, 1, 0);
    }

    public char getFieldCharAt(int x, int y) {
        int field = getFieldAt(x, y);
        if (field > 0 && field < 10) return Integer.toString(field).charAt(0);
        return CHAR_MAP.getOrDefault(field, ' ');
    }

    public void setFieldAt(int x, int y, int field) {
        matrix[x][y] = (byte) field;
    }

    public byte getFieldAt(int x, int y) {
        return matrix[x][y];
    }

    public void initialize(int sizeX, int sizeY, int bombs) {
        matrix = new byte[sizeX][sizeY];
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        for (int x = 0; x < bombs; x++) {
            byte current;
            int lx, ly;
            do {
                lx = rand.nextInt(sizeX);
                ly = rand.nextInt(sizeY);
                current = getFieldAt(lx, ly);
            } while (current != 0);
            setFieldAt(lx, ly, 11);
        }
    }

    public int countBombs(int x, int y) {
        return countFields(x, y, 11, 13);
    }

    public int countFlags(int x, int y) {
        return countFields(x, y, 12, 13);
    }

    public int countFields(int x, int y, int... types) {
        int bombs = 0;
        for (int i = x - 1; i <= x + 1; i++)
            for (int j = y - 1; j <= y + 1; j++) {
                if (i >= 0 && j >= 0 && i < getSizeX() && j < getSizeY()) {
                    byte field = getFieldAt(i, j);
                    for (int type : types)
                        if (type == field) {
                            bombs++;
                            break;
                        }
                }
            }
        return bombs;
    }

    public byte[][] getMatrix() {
        return matrix;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }
}
