package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Font;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;

public class UnchangingTitleProvider implements TitleProvider {

    public UnchangingTitleProvider(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    private String title;

    public Font getFont() {
        return font;
    }

    public void setFont(Font f) {
        this.font = f;
    }

    private Font font = DisplayUtils.DEFAULT_FONT;
}