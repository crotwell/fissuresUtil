package edu.sc.seis.fissuresUtil.display;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import java.awt.Font;
/**
 * ScaleBorder.java
 *
 *
 * Created: Thu Oct  7 10:49:26 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class ScaleBorder extends javax.swing.border.AbstractBorder {

    public ScaleBorder() {
        top = 0;
        left = 0;
        right = 0;
        bottom = 0;
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

        Graphics2D copy = (Graphics2D)g.create();
        if (copy != null) {
            copy.translate(x,y);
            try {
                copy.setFont(DisplayUtils.BORDER_FONT);
                // in case there are borders inside of this one
                Insets insets = ((JComponent)c).getInsets();
                FontMetrics fm = copy.getFontMetrics();

                // top
                int numTicks;
                int pixelLoc;
                if (topScaleMap != null) {
                    numTicks = topScaleMap.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = insets.left + topScaleMap.getPixelLocation(i);
                        if(topScaleMap.isMajorTick(i)){
                            copy.draw(new Line2D.Float(pixelLoc, top, pixelLoc, top - majorTickLength));
                            String label = topScaleMap.getLabel(i);
                            if (label != null && label.length() != 0 ) {
                                int labelWidth = (int)fm.getStringBounds(label, copy).getWidth();
                                copy.drawString(label,
                                                pixelLoc - labelWidth/2,
                                                top - majorTickLength-
                                                    fm.getLeading());
                            }
                        }else{
                            copy.draw(new Line2D.Float(pixelLoc, top, pixelLoc, top - minorTickLength));

                        }
                    }
                    copy.drawString(topScaleMap.getAxisLabel(),
                                    width/2,
                                    fontHeight);

                }

                // left
                if (leftScaleMap != null) {
                    numTicks = leftScaleMap.getNumTicks();
                    if ( numTicks == 0 || numTicks ==1) {
                        copy.drawString("No Data",
                                        fontHeight + 2,
                                        top + (height - top - bottom)/2-
                                            fm.getLeading());
                    } // end of if ()
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = height - leftScaleMap.getPixelLocation(i) - bottom;
                        if (leftScaleMap.isMajorTick(i)) {
                            copy.draw(new Line2D.Float(left,
                                                       pixelLoc,
                                                       left - majorTickLength,
                                                       pixelLoc));
                            String label = leftScaleMap.getLabel(i);
                            if (label != null && label.length() != 0) {
                                Rectangle2D stringBounds = fm.getStringBounds(label, copy);
                                copy.drawString(label,
                                                    (int)(left - majorTickLength - stringBounds.getWidth()) - 5,
                                                pixelLoc + (int)(stringBounds.getHeight()/2));
                            }
                        } else {
                            copy.draw(new Line2D.Float(left - minorTickLength,
                                                       pixelLoc,
                                                       left,
                                                       pixelLoc));
                        }
                    }
                    if(leftAxisLabelBounds == null){
                        leftAxisLabelBounds = copy.getFontMetrics().getStringBounds(leftScaleMap.getAxisLabel(), copy);
                    }
                    double yTranslate = height - bottom + top - (height - bottom + top - leftAxisLabelBounds.getWidth())/2;
                    double xTranslate = leftAxisLabelBounds.getHeight();
                    copy.translate(xTranslate, yTranslate);
                    copy.rotate(-Math.PI/2);
                    copy.drawString(leftScaleMap.getAxisLabel(), 0, 0);
                    copy.rotate(Math.PI/2);
                    copy.translate(-xTranslate, -yTranslate);
                }

                // bottom
                if (bottomScaleMap != null) {
                    numTicks = bottomScaleMap.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = insets.left + bottomScaleMap.getPixelLocation(i);
                        if (bottomScaleMap.isMajorTick(i)) {
                            copy.draw(new Line2D.Float(pixelLoc,
                                                       height-bottom,
                                                       pixelLoc,
                                                       height-bottom+majorTickLength));
                            String label = bottomScaleMap.getLabel(i);
                            Rectangle2D labelBounds = fm.getStringBounds(label, copy);
                            int labelWidth = (int)labelBounds.getWidth();
                            int labelHeight = (int)labelBounds.getHeight();
                            if (label != null && label.length() != 0) {
                                copy.drawString(label,
                                                pixelLoc - labelWidth/2,
                                                height- bottom + majorTickLength + labelHeight);
                            }
                        } else {
                            copy.draw(new Line2D.Float(pixelLoc,
                                                       height-bottom,
                                                       pixelLoc,
                                                       height-bottom+minorTickLength));
                        }
                    }
                    copy.drawString(bottomScaleMap.getAxisLabel(),
                                    left + (width - left)/2,
                                    height);
                }

                // right
                if (rightScaleMap != null) {
                    numTicks = rightScaleMap.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = height - rightScaleMap.getPixelLocation(i) - bottom;
                        copy.draw(new Line2D.Float(pixelLoc,
                                                   c.getSize().height-bottom,
                                                   pixelLoc,
                                                   c.getSize().height-bottom/2));
                        String label = rightScaleMap.getLabel(i);
                        if (label != null && label.length() != 0) {
                            copy.drawString(label,
                                            pixelLoc,
                                            c.getSize().height-fm.getLeading());
                        }
                    }
                }
            } finally {
                copy.dispose();
            }
        }
    }

    private Rectangle2D leftAxisLabelBounds;

    public void setTopScaleMapper(ScaleMapper scaleMap) {
        this.topScaleMap = scaleMap;
        if(scaleMap.getAxisLabel() != null && !scaleMap.getAxisLabel().equals("")){
            top = 20 + getFontHeight(scaleMap.getAxisLabel());
        }else{
            top = 20;
        }
    }

    public void clearTopScaleMapper() {
        this.topScaleMap = null;
        top = 0;
    }

    public ScaleMapper getTopScaleMapper() {
        return topScaleMap;
    }

    public void setLeftScaleMapper(ScaleMapper scaleMap) {
        this.leftScaleMap = scaleMap;
        if(scaleMap.getAxisLabel() != null && !scaleMap.getAxisLabel().equals("")){
            left = 50 + getFontHeight(scaleMap.getAxisLabel());
        }else{
            left = 50;
        }
    }

    public void clearLeftScaleMapper() {
        this.leftScaleMap = null;
        left = 0;
    }

    public ScaleMapper getLeftScaleMapper() {
        return leftScaleMap;
    }

    public void setBottomScaleMapper(ScaleMapper scaleMap) {
        this.bottomScaleMap = scaleMap;
        if(scaleMap.getAxisLabel() != null && !scaleMap.getAxisLabel().equals("")){
            bottom = 20 + getFontHeight(scaleMap.getAxisLabel());
        }else{
            bottom = 20;
        }
    }

    public void clearBottomScaleMapper() {
        this.bottomScaleMap = null;
        bottom = 0;
    }

    public ScaleMapper getBottomScaleMapper() {
        return bottomScaleMap;
    }

    public void setRightScaleMapper(ScaleMapper scaleMap) {
        this.rightScaleMap = scaleMap;
        right = 50;
    }

    public void clearRightScaleMapper() {
        this.rightScaleMap = null;
        right = 0;
    }

    public ScaleMapper getRightScaleMapper() {
        return rightScaleMap;
    }

    /**
     * Get the value of majorTickLength.
     * @return Value of majorTickLength.
     */
    public int getMajorTickLength() {return majorTickLength;}

    /**
     * Set the value of majorTickLength.
     * @param v  Value to assign to majorTickLength.
     */
    public void setMajorTickLength(int  v) {this.majorTickLength = v;}

    /**
     * Get the value of minorTickLength.
     * @return Value of minorTickLength.
     */
    public int getMinorTickLength() {return minorTickLength;}

    /**
     * Set the value of minorTickLength.
     * @param v  Value to assign to minorTickLength.
     */
    public void setMinorTickLength(int  v) {this.minorTickLength = v;}

    private int getFontHeight(String text){
        if(fontHeight == 0){
            LineMetrics lm = DisplayUtils.BORDER_FONT.getLineMetrics(text,frc);
            fontHeight = (int)lm.getAscent();
        }
        return fontHeight;
    }


    private static FontRenderContext frc =  new FontRenderContext(new AffineTransform(),
                                                                  false, false);

    protected int top, left, bottom, right;

    protected int majorTickLength = 8;

    protected int minorTickLength = 4;



    private int fontHeight;

    protected ScaleMapper topScaleMap;
    protected ScaleMapper leftScaleMap;
    protected ScaleMapper bottomScaleMap;
    protected ScaleMapper rightScaleMap;

} // ScaleBorder
