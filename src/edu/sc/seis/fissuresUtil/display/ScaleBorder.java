package edu.sc.seis.fissuresUtil.display;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
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
            try {
		Font f = new Font("SansSerif", Font.PLAIN, 9);
                copy.setFont(f);
                // in case there are borders inside of this one
                Insets insets = ((JComponent)c).getInsets();
		/*System.out.println("x:     " + x + " y:       " + y);
		System.out.println("itop:   " + insets.top + " ibottom: " + insets.bottom + "ileft: " + insets.left +  " iright: " + 
				   insets.right);
				   System.out.println("top:   " + top + " bottom: " + bottom + " right:   " + right +  "left:  " + left);*/
		FontMetrics fm = copy.getFontMetrics();
		String label;
		// top
		int numTicks;
		int pixelLoc;
		if (topScaleMap != null) {
		    numTicks = topScaleMap.getNumTicks();
		    for (int i=0; i<numTicks; i++) {
			pixelLoc = insets.left + topScaleMap.getPixelLocation(i);
			if(topScaleMap.isMajorTick(i))
			    copy.draw(new Line2D.Float(pixelLoc, top, pixelLoc, top - majorTickLength));
			else
			    copy.draw(new Line2D.Float(pixelLoc, top, pixelLoc, top - minorTickLength));
			label = topScaleMap.getLabel(i);
                        int labelWidth = (int)fm.getStringBounds(label, copy).getWidth();
			    if (label != null && label.length() != 0 && pixelLoc + labelWidth < width) {
				copy.drawString(label,
						pixelLoc - labelWidth/2,
						top - majorTickLength-
						fm.getLeading());
                        }
		    }
		}

		// left
		if (leftScaleMap != null) {
		    numTicks = leftScaleMap.getNumTicks();
		    for (int i=0; i<numTicks; i++) {
			pixelLoc = height - leftScaleMap.getPixelLocation(i) - bottom;
			if (leftScaleMap.isMajorTick(i)) {
			    copy.draw(new Line2D.Float(insets.left -majorTickLength - insets.right,
						       pixelLoc,
						       insets.left - insets.right,
						       pixelLoc));
			} else {
			    copy.draw(new Line2D.Float(insets.left - insets.right  - minorTickLength,
						       pixelLoc,
						       insets.left - insets.right,
						       pixelLoc));
			}
			label = leftScaleMap.getLabel(i);
			if (label != null && label.length() != 0) {
			    if(i == 0)
				copy.drawString(label,
						insets.left - insets.right - 45,
						pixelLoc + 5);
			    else if(i == numTicks - 1)
				copy.drawString(label,
						insets.left - insets.right - 45,
						pixelLoc + 5);
			    else
				copy.drawString(label,
						insets.left - insets.right - 45,
						pixelLoc);
			}
		    }
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
                            } else {
                                copy.draw(new Line2D.Float(pixelLoc,
							   height-bottom,
							   pixelLoc,
							   height-bottom+minorTickLength));
			    }
                            label = bottomScaleMap.getLabel(i);
			    int labelWidth = (int)fm.getStringBounds(label, copy).getWidth();
			    if (label != null && label.length() != 0 && pixelLoc + labelWidth < width) {
				copy.drawString(label,
                                         pixelLoc - labelWidth/2,
                                         height-fm.getLeading());
                            }
		    }
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
                        label = rightScaleMap.getLabel(i);
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

    public void setTopScaleMapper(ScaleMapper scaleMap) {
        this.topScaleMap = scaleMap;
        top = 20;
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
        left = 50;
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
        bottom = 20;
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
       * Get the value of top.
       * @return Value of top.
       */
    public int getTop() {return top;}
    
    /**
       * Set the value of top.
       * @param v  Value to assign to top.
       */
    public void setTop(int  v) {this.top = v;}
    
    /**
       * Get the value of left.
       * @return Value of left.
       */
    public int getLeft() {return left;}
    
    /**
       * Set the value of left.
       * @param v  Value to assign to left.
       */
    public void setLeft(int  v) {this.left = v;}
    
    /**
       * Get the value of bottom.
       * @return Value of bottom.
       */
    public int getBottom() {return bottom;}
    
    /**
       * Set the value of bottom.
       * @param v  Value to assign to bottom.
       */
    public void setBottom(int  v) {this.bottom = v;}
    
    /**
       * Get the value of right.
       * @return Value of right.
       */
    public int getRight() {return right;}
    
    /**
       * Set the value of right.
       * @param v  Value to assign to right.
       */
    public void setRight(int  v) {this.right = v;}

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

    protected int top, left, bottom, right;

    protected int majorTickLength = 8;

    protected int minorTickLength = 4;
    
    protected ScaleMapper topScaleMap;
    protected ScaleMapper leftScaleMap;
    protected ScaleMapper bottomScaleMap;
    protected ScaleMapper rightScaleMap;
    
    } // ScaleBorder
