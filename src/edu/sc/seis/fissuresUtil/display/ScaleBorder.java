package edu.sc.seis.fissuresUtil.display;

import java.awt.*;

import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
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
            Rectangle currentClip = copy.getClip().getBounds();
            copy.setClip(currentClip.x,currentClip.y, width, height);
            try {
                copy.setFont(DisplayUtils.BORDER_FONT);
                copy.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
                // in case there are borders inside of this one
                Insets insets = ((JComponent)c).getInsets();
                FontMetrics fm = copy.getFontMetrics();

                // top
                int numTicks;
                int pixelLoc;
                ScaleMapper map = topScaleMap;
                if (map != null) {
                    numTicks = map.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = insets.left + map.getPixelLocation(i);
                        if(map.isMajorTick(i)){
                            copy.draw(new Line2D.Float(pixelLoc, top, pixelLoc, top - majorTickLength));
                            String label = map.getLabel(i);
                            if (label != null && label.length() != 0 ) {
                                int labelWidth = (int)fm.getStringBounds(label, copy).getWidth();
                                copy.drawString(label,
                                                pixelLoc - labelWidth/2,
                                                top - majorTickLength - 3);
                            }
                        }else{
                            copy.draw(new Line2D.Float(pixelLoc, top, pixelLoc, top - minorTickLength));
                        }
                    }
                    copy.drawString(map.getAxisLabel(),
                                    width/2,
                                    fontHeight);

                }

                // left
                map = leftScaleMap;
                if (map != null) {
                    numTicks = map.getNumTicks();
                    if ( numTicks == 0 || numTicks ==1) {
                        copy.drawString("No Data",
                                        fontHeight + 2,
                                        top + (height - top - bottom)/2-
                                            fm.getLeading());
                    }else{ // end of if ()
                        for (int i=0; i<numTicks; i++) {
                            pixelLoc = height - map.getPixelLocation(i) - bottom;
                            if (map.isMajorTick(i)) {
                                copy.draw(new Line2D.Float(left,
                                                           pixelLoc,
                                                           left - majorTickLength,
                                                           pixelLoc));
                                String label = map.getLabel(i);
                                if (label != null && label.length() != 0) {
                                    Rectangle2D stringBounds = fm.getStringBounds(label, copy);
                                    copy.drawString(label,
                                                        (int)(left - majorTickLength - stringBounds.getWidth()) - 2,
                                                    pixelLoc +  fm.getAscent()/2 - 2);
                                }
                            } else {
                                copy.draw(new Line2D.Float(left - minorTickLength,
                                                           pixelLoc,
                                                           left,
                                                           pixelLoc));
                            }
                        }
                        if(leftAxisLabelBounds == null){
                            leftAxisLabelBounds = copy.getFontMetrics().getStringBounds(map.getAxisLabel(), copy);
                        }
                        double yTranslate = insets.top + (height - insets.top - insets.bottom + leftAxisLabelBounds.getWidth())/2;
                        double xTranslate = leftAxisLabelBounds.getHeight();
                        copy.translate(xTranslate, yTranslate);
                        copy.rotate(-Math.PI/2);
                        copy.drawString(map.getAxisLabel(), 0, 0);
                        copy.rotate(Math.PI/2);
                        copy.translate(-xTranslate, -yTranslate);
                    }
                }

                // bottom
                map = bottomScaleMap;
                if (map != null) {
                    numTicks = map.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = insets.left + map.getPixelLocation(i);
                        if (map.isMajorTick(i)) {
                            copy.draw(new Line2D.Float(pixelLoc,
                                                       height-bottom,
                                                       pixelLoc,
                                                       height-bottom+majorTickLength));
                            String label = map.getLabel(i);
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
                    String label = map.getAxisLabel();
                    Rectangle2D labelBounds = fm.getStringBounds(label, copy);
                    copy.drawString(label,
                                        (int)(insets.left + (width - insets.left - insets.right - labelBounds.getWidth())/2),
                                    height);
                }

                // right
                map = rightScaleMap;
                if (map != null) {
                    numTicks = map.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = height - map.getPixelLocation(i) - bottom;
                        copy.draw(new Line2D.Float(pixelLoc,
                                                   c.getSize().height-bottom,
                                                   pixelLoc,
                                                   c.getSize().height-bottom/2));
                        String label = map.getLabel(i);
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
            top = majorTickLength + 2 * getFontHeight(scaleMap.getAxisLabel()) + 5;
        }else{
            top = majorTickLength + getFontHeight("test") + 5;
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
        if(scaleMap.getAxisLabel() != null){
            left = getFontHeight(scaleMap.getAxisLabel()) + 60;
        }else{
            left = 60;
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

    public static int getFontHeight(String text){
        if(fontHeight == 0){
            LineMetrics lm = DisplayUtils.BORDER_FONT.getLineMetrics(text,frc);
            fontHeight = (int)lm.getHeight();
        }
        return fontHeight;
    }


    private static FontRenderContext frc =  new FontRenderContext(new AffineTransform(),
                                                                  false, false);

    protected int top, left, bottom, right;

    protected int majorTickLength = 8;

    protected int minorTickLength = 4;

    private static int fontHeight;

    protected ScaleMapper topScaleMap;
    protected ScaleMapper leftScaleMap;
    protected ScaleMapper bottomScaleMap;
    protected ScaleMapper rightScaleMap;

} // ScaleBorder
