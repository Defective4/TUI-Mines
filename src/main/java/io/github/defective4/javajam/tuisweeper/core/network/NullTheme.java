package io.github.defective4.javajam.tuisweeper.core.network;

public class NullTheme extends RemoteTheme {
    public NullTheme() {
        super(null, null, null, null, null, 0);
    }

    @Override
    public String toString() {
        return "<No themes>";
    }
}
