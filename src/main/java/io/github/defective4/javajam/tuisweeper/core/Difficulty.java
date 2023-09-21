package io.github.defective4.javajam.tuisweeper.core;

public enum Difficulty {
    EASY(10, 10, 10),
    NORMAL(18, 18, 40),
    HARD(24, 24, 100),
    CUSTOM(0, 0, 0);

    private final int width, height, bombs;

    Difficulty(int width, int height, int bombs) {
        this.width = width;
        this.height = height;
        this.bombs = bombs;
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
        return TUISweeper.capitalize(this);
    }
}
