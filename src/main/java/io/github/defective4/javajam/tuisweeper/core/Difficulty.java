package io.github.defective4.javajam.tuisweeper.core;

/**
 * An enum containing all difficulties the player can choose
 *
 * @author Defective
 */
public enum Difficulty {
    EASY(1, 10, 10, 10),
    NORMAL(2, 18, 18, 40),
    HARD(3, 24, 24, 100),
    CUSTOM(4, 0, 0, 0);

    private final int width, height, bombs, id;

    Difficulty(int id, int width, int height, int bombs) {
        this.width = width;
        this.height = height;
        this.bombs = bombs;
        this.id = id;
    }

    public static Difficulty getByID(int id) {
        for (Difficulty dif : values())
            if (dif.getId() == id) return dif;
        return CUSTOM;
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBombs() {
        return bombs;
    }

    @Override
    public String toString() {
        return TUIMines.capitalize(this);
    }
}
