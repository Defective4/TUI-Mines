package io.github.defective4.javajam.tuisweeper.core.ui;

import io.github.defective4.javajam.tuisweeper.core.TUISweeper;
import io.github.defective4.javajam.tuisweeper.core.storage.Preferences;

import static com.googlecode.lanterna.TextColor.ANSI;

public enum ThemePreset {
    NONE,
    PITCH_BLACK(ANSI.BLACK, ANSI.WHITE_BRIGHT, ANSI.BLACK, ANSI.WHITE_BRIGHT, ANSI.WHITE_BRIGHT, ANSI.BLACK),
    CLASSIC_TERMINAL(ANSI.WHITE, ANSI.BLACK, ANSI.BLUE, ANSI.WHITE_BRIGHT, ANSI.BLUE, ANSI.WHITE_BRIGHT),
    GREEN_ON_BLACK(ANSI.BLACK, ANSI.GREEN_BRIGHT, ANSI.BLACK, ANSI.GREEN, ANSI.GREEN_BRIGHT, ANSI.BLACK);

    private final ANSI bb, bf, eb, ef, sb, sf;

    ThemePreset() {
        bb = null;
        bf = null;
        eb = null;
        ef = null;
        sb = null;
        sf = null;
    }

    ThemePreset(ANSI bb, ANSI bf, ANSI eb, ANSI ef, ANSI sb, ANSI sf) {
        this.bb = bb;
        this.bf = bf;
        this.eb = eb;
        this.ef = ef;
        this.sb = sb;
        this.sf = sf;
    }

    public Preferences.UserTheme toTheme() {
        return new Preferences.UserTheme(bf, bb, ef, eb, sf, sb);
    }

    public ANSI getBb() {
        return bb;
    }

    public ANSI getBf() {
        return bf;
    }

    public ANSI getEb() {
        return eb;
    }

    public ANSI getEf() {
        return ef;
    }

    public ANSI getSb() {
        return sb;
    }

    public ANSI getSf() {
        return sf;
    }

    @Override
    public String toString() {
        return this == NONE ? "<Choose>" : TUISweeper.capitalize(this);
    }
}
