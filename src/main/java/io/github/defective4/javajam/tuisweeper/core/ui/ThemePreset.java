package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.TextColor;
import io.github.defective4.javajam.tuisweeper.core.TUISweeper;

public enum ThemePreset {
    NONE,
    PITCH_BLACK(TextColor.ANSI.BLACK,
                TextColor.ANSI.WHITE_BRIGHT,
                TextColor.ANSI.BLACK,
                TextColor.ANSI.WHITE_BRIGHT,
                TextColor.ANSI.WHITE_BRIGHT,
                TextColor.ANSI.BLACK),
    CLASSIC_TERMINAL(TextColor.ANSI.WHITE,
                     TextColor.ANSI.BLACK,
                     TextColor.ANSI.BLUE,
                     TextColor.ANSI.WHITE_BRIGHT,
                     TextColor.ANSI.BLUE,
                     TextColor.ANSI.WHITE_BRIGHT),
    GREEN_ON_BLACK(TextColor.ANSI.BLACK,
                   TextColor.ANSI.GREEN_BRIGHT,
                   TextColor.ANSI.BLACK,
                   TextColor.ANSI.GREEN,
                   TextColor.ANSI.GREEN_BRIGHT,
                   TextColor.ANSI.BLACK);

    private final TextColor.ANSI bb, bf, eb, ef, sb, sf;

    ThemePreset() {
        bb = null;
        bf = null;
        eb = null;
        ef = null;
        sb = null;
        sf = null;
    }

    ThemePreset(TextColor.ANSI bb, TextColor.ANSI bf, TextColor.ANSI eb, TextColor.ANSI ef, TextColor.ANSI sb, TextColor.ANSI sf) {
        this.bb = bb;
        this.bf = bf;
        this.eb = eb;
        this.ef = ef;
        this.sb = sb;
        this.sf = sf;
    }

    public TextColor.ANSI getBb() {
        return bb;
    }

    public TextColor.ANSI getBf() {
        return bf;
    }

    public TextColor.ANSI getEb() {
        return eb;
    }

    public TextColor.ANSI getEf() {
        return ef;
    }

    public TextColor.ANSI getSb() {
        return sb;
    }

    public TextColor.ANSI getSf() {
        return sf;
    }

    @Override
    public String toString() {
        return this == NONE ? "<Choose>" : TUISweeper.capitalize(this);
    }
}
