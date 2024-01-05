package io.github.defective4.javajam.tuisweeper.components;

public final class Strings {
    private Strings() {
    }

    public static String capitalize(Enum<?> en) {
        String[] split = en.name().split("_");
        for (int x = 0; x < split.length; x++)
            split[x] = split[x].substring(0, 1).toUpperCase() + split[x].substring(1).toLowerCase();
        return String.join(" ", split);
    }
}
