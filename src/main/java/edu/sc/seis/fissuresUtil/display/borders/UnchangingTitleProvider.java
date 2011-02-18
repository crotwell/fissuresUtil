package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Color;
import java.awt.Font;

import edu.sc.seis.fissuresUtil.display.DisplayUtils;

public class UnchangingTitleProvider implements TitleProvider {

    public UnchangingTitleProvider(String title) {
        this(title, DisplayUtils.DEFAULT_FONT);
    }

    public UnchangingTitleProvider(String title, Font f) {
        this.title = title;
        setTitleFont(f);
    }

    public String getTitle() {
        return title;
    }

    private String title;

    public Font getTitleFont() {
        return font;
    }

    public void setTitleFont(Font f) {
        this.font = f;
    }

    public void setTitleColor(Color c) {
        this.color = c;
    }

    public Color getTitleColor() {
        return color;
    }

    private Color color = null;

    private Font font = DisplayUtils.DEFAULT_FONT;
}