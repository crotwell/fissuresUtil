package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Color;
import java.awt.Font;

public interface TitleProvider {

    public String getTitle();

    public Font getTitleFont();

    public void setTitleFont(Font f);

    /**
     * To use the color of the Border, it return null
     * 
     * @return the color to draw the title with
     */
    public Color getTitleColor();

    public void setTitleColor(Color c);
}