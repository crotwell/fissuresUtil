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
import javax.swing.SwingUtilities;
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
            //Rectangle currentClip = copy.getClip().getBounds();
            //copy.setClip(currentClip.x,currentClip.y, width, height);
            try {
                copy.setFont(DisplayUtils.BORDER_FONT);
                copy.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
                // in case there are borders inside of this one
                Insets insets = ((JComponent)c).getInsets();
                insets = new Insets(insets.top - y, insets.left - x,
                                    insets.bottom, insets.right);
                FontMetrics fm = copy.getFontMetrics();

                // top
                int numTicks;
                int pixelLoc;
                ScaleMapper map = topScaleMap;
                if (map != null) {
                    numTicks = map.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = map.getPixelLocation(i) + insets.left;
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
                                    height - 5);
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

    public int paintLeftAmpBorder(Component c,
                                  Graphics g,
                                  int x,
                                  int y,
                                  int width,
                                  int height) {

        int saveLeft = left;
        left = width;


        Graphics2D copy = (Graphics2D)g.create();
        if (copy != null) {
            copy.translate(x,y);
            copy.setClip(0, 0, width, height);
            try {
                copy.setFont(DisplayUtils.BORDER_FONT);
                copy.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
                // in case there are borders inside of this one
                Insets insets = ((JComponent)c).getInsets();
                insets = new Insets(insets.top - y, insets.left - x,
                                    insets.bottom, insets.right);
                FontMetrics fm = copy.getFontMetrics();

                // top
                int numTicks;
                int pixelLoc;

                // left
                ScaleMapper map = leftScaleMap;
                if (map != null) {
                    numTicks = map.getNumTicks();
                    if ( numTicks == 0 || numTicks ==1) {
                        copy.drawString("No Data",
                                        fontHeight + 2,
                                        top + (height - top)/2-
                                            fm.getLeading());
                    }else{ // end of if ()
                        for (int i=0; i<numTicks; i++) {
                            pixelLoc = map.getPixelLocation(i) + 3;
                            pixelLoc = this.remapPixels(pixelLoc,0,height,map.getTotalPixels(), 0);
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
                        //                        double yTranslate = insets.top + (height - insets.top - insets.bottom + leftAxisLabelBounds.getWidth())/2;
                        double yTranslate = insets.top + (height - insets.top + leftAxisLabelBounds.getWidth())/2;
                        double xTranslate = leftAxisLabelBounds.getHeight();
                        copy.translate(xTranslate, yTranslate);
                        copy.rotate(-Math.PI/2);
                        copy.drawString(map.getAxisLabel(), 0, 0);
                        copy.rotate(Math.PI/2);
                        copy.translate(-xTranslate, -yTranslate);
                    }
                }

            } finally {
                copy.dispose();
            }
        }
        left = saveLeft;
        return width;
    }

    public void paintAmpBorder(Component c,
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
                copy.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
                // in case there are borders inside of this one
                Insets insets = ((JComponent)c).getInsets();
                insets = new Insets(insets.top - y, insets.left - x,
                                    insets.bottom, insets.right);
                FontMetrics fm = copy.getFontMetrics();

                // top
                int numTicks;
                int pixelLoc;
                ScaleMapper map = topScaleMap;
                if (map != null) {
                    numTicks = map.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = map.getPixelLocation(i) + insets.left;
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
                                    height - 5);
                }

            } finally {
                copy.dispose();
            }
        }
    }

    /**
     * used for PDF printing.
     * @param c
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     * @return the edge of the topAmpBorder (int)
     */
    public int paintTopTimeBorder(Component c,
                                  Graphics g,
                                  int x,
                                  int y,
                                  int width,
                                  int height) {

        int saveTop = top;
        top = height;

        Graphics2D copy = (Graphics2D)g.create();
        if (copy != null) {
            copy.translate(x,y);
            try {
                copy.setFont(DisplayUtils.BORDER_FONT);
                copy.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
                // in case there are borders inside of this one
                Insets insets = ((JComponent)c).getInsets();
                // new insets( top, left, bottom, right)
                insets = new Insets(y, x, 0, 0);
                FontMetrics fm = copy.getFontMetrics();

                // top
                int numTicks;
                int pixelLoc;
                ScaleMapper map = topScaleMap;
                if (map != null) {
                    numTicks = map.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = map.getPixelLocation(i) + insets.left;
                        pixelLoc = this.remapPixels(pixelLoc,0,0,map.getTotalPixels(), width);

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
                                    width/2 - SwingUtilities.computeStringWidth(fm, map.getAxisLabel())/2,
                                    top - majorTickLength - fontHeight - 3);

                }

            } finally {
                copy.dispose();
            }
        }
        top = saveTop;
        return height;
    }

    public int paintBottomTimeBorder(Component c,
                                     Graphics g,
                                     int x,
                                     int y,
                                     int width,
                                     int height) {

        int saveTop = top;
        int saveBottom = bottom;

        top = height;
        //      bottom = height;

        Graphics2D copy = (Graphics2D)g.create();
        if (copy != null) {
            copy.translate(x,y);
            //Rectangle currentClip = copy.getClip().getBounds();
            //copy.setClip(currentClip.x,currentClip.y, width, height);
            try {
                copy.setFont(DisplayUtils.BORDER_FONT);
                copy.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
                // in case there are borders inside of this one
                Insets insets = ((JComponent)c).getInsets();
                insets = new Insets(insets.top - y, insets.left - x,
                                    insets.bottom, insets.right);
                FontMetrics fm = copy.getFontMetrics();

                // top
                int numTicks;
                int pixelLoc;

                // bottom
                ScaleMapper map = bottomScaleMap;
                if (map != null) {
                    numTicks = map.getNumTicks();
                    for (int i=0; i<numTicks; i++) {
                        pixelLoc = insets.left + map.getPixelLocation(i);
                        pixelLoc = this.remapPixels(pixelLoc, 0, 0, map.getTotalPixels(), width);
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
                                    height - 5);

                }

            } finally {
                copy.dispose();
            }

        }
        top = saveTop;
        bottom = saveBottom;

        return width;
    }

    /**
     * get the borderwidth for drawing Amplitude labels.
     * @return
     */
    public int getLabelWidth() {
        return left;
    }

    /**
     * get the borderwidth for drawing Time labels.
     * @return
     */
    public int getLabelHeight() {
        return top;
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
            bottom = majorTickLength + 2 * getFontHeight(scaleMap.getAxisLabel()) + 5;
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

    /**
     *@returns the height of the DisplayUtils.BORDER_FONT for the given text
     */
    public static int getFontHeight(String text){
        if(fontHeight == 0){
            LineMetrics lm = DisplayUtils.BORDER_FONT.getLineMetrics(text,frc);
            fontHeight = (int)lm.getHeight();
        }
        return fontHeight;
    }




    public int remapPixels(int xval, int xa, int ya, int xb, int yb) {

        return (int)Math.round(SimplePlotUtil.linearInterp(xa, ya,
                                                           xb, yb, xval));


        /*      if(ascending){
         return (int)Math.round(SimplePlotUtil.linearInterp(minTick, 0,
         calcRange, tmpTotalPixels,
         minTick + i * tickInc));
         }else{
         return (int)Math.round(SimplePlotUtil.linearInterp(minTick, 0,
         calcRange, tmpTotalPixels,
         minTick + (numTicks - i) * tickInc));
         }
         */
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
