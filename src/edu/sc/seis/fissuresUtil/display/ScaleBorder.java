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

        Graphics2D copy = (Graphics2D)g;
        if (copy != null) {
            try {
                AffineTransform insetMove = AffineTransform.getTranslateInstance(x, y);
		copy.transform(insetMove);
		Font f = new Font("SansSerif", Font.PLAIN, 9);
                copy.setFont(f);
                // in case there are borders inside of this one
                Insets insets = ((JComponent)c).getInsets();
                insets.left -= x+left;
                insets.top -= y+top;
                insets.right -= right + c.getSize().width - x - left;
                insets.bottom -= bottom + c.getSize().height - y - top;
                FontMetrics fm = copy.getFontMetrics();

                String labelTemp;
		// top
		int numTicks;
		int pixelLoc;
		if (topScaleMap != null) {
		    numTicks = topScaleMap.getNumTicks();
		    for (int i=0; i<numTicks; i++) {
			pixelLoc = insets.left + left + topScaleMap.getPixelLocation(i);
			if(topScaleMap.isMajorTick(i))
			    copy.draw(new Line2D.Float(pixelLoc, top, pixelLoc, top - majorTickLength));
			else
			    copy.draw(new Line2D.Float(pixelLoc, top, pixelLoc, top - minorTickLength));
			labelTemp = topScaleMap.getLabel(i);
                        if (labelTemp != null && labelTemp.length() != 0) {
                            copy.drawString(labelTemp,
                                            pixelLoc,
					    top - majorTickLength-
                                            fm.getLeading());
                        }
		    }
		}

		// left
		if (leftScaleMap != null) {
		    numTicks = leftScaleMap.getNumTicks();
		    for (int i=0; i<numTicks; i++) {
			pixelLoc = insets.top+top+
                            leftScaleMap.getPixelLocation(i);
                        if (pixelLoc >=insets.top+top &&
                            pixelLoc <=height-insets.bottom-bottom) {
                            if (leftScaleMap.isMajorTick(i)) {
                                copy.draw(new Line2D.Float(left-majorTickLength,
							   pixelLoc,
							   left,
							   pixelLoc));
                            } else {
                                copy.draw(new Line2D.Float(left-minorTickLength,
							   pixelLoc,
							   left,
							   pixelLoc));
                            }
                            labelTemp = leftScaleMap.getLabel(i);
                            if (labelTemp != null && labelTemp.length() != 0) {
				if(i == 0)
				    copy.drawString(labelTemp,
						    0,
						    pixelLoc - 5);
				else if(i == numTicks - 1)
				    copy.drawString(labelTemp,
						    0,
						    pixelLoc + 5);
				else
				    copy.drawString(labelTemp,
						    0,
						    pixelLoc);
                            }
                        }
                    }
		}

		// bottom
		if (bottomScaleMap != null) {
		    numTicks = bottomScaleMap.getNumTicks();
		    for (int i=0; i<numTicks; i++) {
			pixelLoc = insets.left + left+
                            bottomScaleMap.getPixelLocation(i);
                        if (pixelLoc >=insets.left + left &&
                            pixelLoc <= width - insets.right) {
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
                            labelTemp = bottomScaleMap.getLabel(i);
                            if (labelTemp != null && labelTemp.length() != 0) {
                                copy.drawString(labelTemp,
                                         pixelLoc,
                                         height-fm.getLeading());
                            }
                        }
		    }
		}

		// right
		if (rightScaleMap != null) {
		    numTicks = rightScaleMap.getNumTicks();
		    for (int i=0; i<numTicks; i++) {
			pixelLoc = insets.top + top+
                            rightScaleMap.getPixelLocation(i);
			System.out.println("HScaleBorder pixelLoc="+pixelLoc);
			copy.draw(new Line2D.Float(pixelLoc,
				   c.getSize().height-bottom,
				   pixelLoc,
				   c.getSize().height-bottom/2));
                        labelTemp = rightScaleMap.getLabel(i);
                        if (labelTemp != null && labelTemp.length() != 0) {
                            copy.drawString(labelTemp,
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
        bottom = majorTickLength+15; //guess as not sure of what font will be
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
        right = 40;
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
