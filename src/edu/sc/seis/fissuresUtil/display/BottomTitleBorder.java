package edu.sc.seis.fissuresUtil.display;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;

/**
 * BottomTitleBorder.java
 *
 *
 * Created: Fri Oct 22 11:21:22 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class BottomTitleBorder extends javax.swing.border.AbstractBorder {

    public BottomTitleBorder(String title) {
        top = 0;
        left = 0;
        right = 0;
        bottom = 16;
        this.title = title;
    }

    public Insets getBorderInsets(Component c) {
        if (title == null || title.equals("")) {
            return new Insets(0,0,0,0);
        } else {
            return new Insets(top, left, bottom, right);
        }
    }

    public Insets getBorderInsets(Component c, Insets i) {
        if (title == null || title.equals("")) {
            i.top = 0;
            i.left = 0;
            i.right = 0;
            i.bottom = 0;
            return new Insets(0,0,0,0);
        } else {
            i.top = top;
            i.left = left;
            i.right = right;
            i.bottom = bottom;
            return new Insets(top, left, bottom, right);
        }
    }

    public void paintBorder(Component c,
                            Graphics g,
                            int x,
                            int y,
                            int width,
                            int height) {

        Graphics2D copy = (Graphics2D)g.create();
        if (copy != null) {
            try {
                AffineTransform insetMove = AffineTransform.getTranslateInstance(x, y);
                copy.transform(insetMove);
                FontMetrics fm = copy.getFontMetrics();
                //uses components insets to center text under the components window
                //instead of centered under the total width
                Insets compInsets = ((JComponent)c).getInsets();
                width = width - compInsets.left - compInsets.right;
                copy.drawString(title,
                               compInsets.left+ (width - fm.stringWidth(title))/2,
                                    (height-bottom/2)+(fm.getAscent())/2);

            } finally {
                copy.dispose();
            }
        }
    }

    /**
     * Get the value of Title.
     * @return Value of Title.
     */
    public String getTitle() {return title;}

    /**
     * Set the value of Title.
     * @param v  Value to assign to Title.
     */
    public void setTitle(String  v) {this.title = v;}

    protected String title;

    protected int top, left, bottom, right;

} // BottomTitleBorder

