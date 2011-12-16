package edu.sc.seis.fissuresUtil.display;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.border.AbstractBorder;

/**
 * LeftTitleBorder.java
 *
 *
 * Created: Fri Oct 22 11:21:22 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class LeftTitleBorder extends AbstractBorder {

    public LeftTitleBorder(String title) {
        top = 0;
        left = 0;
        right = 0;
        bottom = 0;
        setTitle(title);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(top, left, bottom, right);
    }

    public Insets getBorderInsets(Component c, Insets i) {
        i.top = top;
        i.left = left;
        i.right = right;
        i.bottom = bottom;
        return new Insets(top, left, bottom, right);
    }

    public void paintBorder(Component c,
                            Graphics g,
                            int x,
                            int y,
                            int width,
                            int height) {
        if(title == null || title.equals("")) return;
        Graphics2D g2D = (Graphics2D)g;
        g2D.setFont(DisplayUtils.BORDER_FONT);
        if(titleBounds == null){
            titleBounds = g2D.getFontMetrics().getStringBounds(title, g2D);
            left = (int)titleBounds.getHeight();
        }
        Insets componentInsets = ((JComponent)c).getInsets();
        height = height - componentInsets.bottom - componentInsets.top;
        double yTranslate = componentInsets.top +
            height - (height - y - titleBounds.getWidth())/2;
        double xTranslate = x + titleBounds.getHeight();
        g2D.translate(xTranslate, yTranslate);
        g2D.rotate(-Math.PI/2);
        g2D.drawString(title, 0, 0);
        g2D.rotate(Math.PI/2);
        g2D.translate(-xTranslate, -yTranslate);
    }

    /**
     * Get the value of Title.
     * @return Value of Title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the value of Title.
     * @param v  Value to assign to Title.
     */
    public void setTitle(String  v) {
        this.title = v;
        if(title != null && !title.equals("")){
            left = ScaleBorder.getFontHeight(title);
        }
        titleBounds = null;
    }

    private String title;

    private Rectangle2D titleBounds;

    private int top, left, bottom, right;
} // LeftTitleBorder
