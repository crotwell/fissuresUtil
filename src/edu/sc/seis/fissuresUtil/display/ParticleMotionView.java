package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.display.*;
import org.apache.log4j.*;

/**
 * ParticleMotionView.java
 *
 *
 * Created: Tue Jun 11 15:14:17 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ParticleMotionView extends JComponent{
    public ParticleMotionView (final LocalSeismogramImpl hseis, 
			       LocalSeismogramImpl vseis,
			       TimeRangeConfig timeRangeConfig,
			       final AmpRangeConfig hAmpRangeConfig,
			       AmpRangeConfig vAmpRangeConfig, ParticleMotionDisplay particleMotionDisplay){
	
	this.particleMotionDisplay = particleMotionDisplay;
	if(timeRangeConfig != null) {

	    this.microSecondTimeRange = timeRangeConfig.getTimeRange();
	}
	ParticleMotion particleMotion = new ParticleMotion(hseis,
							   vseis,
							   hAmpRangeConfig,
							   vAmpRangeConfig);
	displays.add(particleMotion);
	    
	vunitRangeImpl = vAmpRangeConfig.getAmpRange(vseis);
	hunitRangeImpl = hAmpRangeConfig.getAmpRange(hseis);
	
	this.addMouseListener(new MouseAdapter() {

		public void mouseClicked(MouseEvent me) {
		    //UnitRangeImpl unitRangeImpl = hAmpRangeConfig.getAmpRange(hseis);
		    //double min = unitRangeImpl.getMinValue();
		    //double max = unitRangeImpl.getMaxValue();
		    ///Insets insets = getInsets();
		    //double  fmin = getSize().getWidth() - insets.left - insets.right;
		    //double fmax = getSize().getHeight() - insets.top - insets.bottom;
		    //int newx = (int)(((max - min) / fmin * me.getX()) - max);
		    //(int)(max - (((max - min) * (fmin - me.getX()) )/ (fmax - me.getY())));
		    //int newy =  (int)(((min - max) / fmax * me.getY()) + max);
		    int clickCount = 0;
		    if(zoomIn)  {
			clickCount = 1;
			//particleMotionDisplay.fireAmpRangeEvent(new AmpSyncEvent(50.0, 50.0, true));
		    }
		    if(zoomOut) { 
			clickCount = 2;
			//	particleMotionDisplay.fireAmpRangeEvent(new AmpSyncEvent(-50.0, -50.0, true));
		    }
		    
		    zoomInParticleMotionDisplay(clickCount, me.getX(), me.getY());
		    //logger.debug("me x "+me.getX()+" me y "+me.getY());
		    //findPoint(me.getClickCount(), me.getX(), me.getY());
		    startPoint = null;
		    endPoint = null;
		}

		public void mousePressed(MouseEvent me) {
		    startPoint = new java.awt.geom.Point2D.Float((float)me.getX(), (float)me.getY());
    		}
		
		public void mouseReleased(MouseEvent me) {
		    // endPoint = new java.awt.geom.Point2D.Float(me.getX(), me.getY());
		    
		    // zoomInParticleMotionDisplay(me.getX(), me.getY());
		  
		    
		}
	
	    });
	

	this.addMouseMotionListener(new MouseMotionAdapter() {
		
		public void mouseDragged(MouseEvent me) {
		    endPoint = new java.awt.geom.Point2D.Float((float)me.getX(), (float)me.getY());
		    repaint();
		}
	    });
	
				    
    }

    public void zoomInParticleMotionDisplay(int clickCount, int mx, int my) {
	
	double hmin = hunitRangeImpl.getMinValue();
	double hmax = hunitRangeImpl.getMaxValue();
	double vmin = vunitRangeImpl.getMinValue();
	double vmax = vunitRangeImpl.getMaxValue();
	if(hmin > hmax) { double temp = hmax; hmax = hmin; hmin = temp;}
	if(vmin > vmax) { double temp = vmax; vmax = vmin; vmin = temp;}
	Insets insets = getInsets();
	double width = getSize().getWidth() - insets.left - insets.right;
	double height = getSize().getHeight() - insets.top - insets.bottom;
	
	int centerx, centery;
	if(clickCount == 1) {
	    centerx = (int)((hmax - hmin) / 4);
	    centery = (int)((vmax - vmin) / 4);
	} else {
	    centerx = (int)((hmax - hmin) / 4);
	    centery = (int)((hmax - hmin) / 4);
	}
	int xone = (int)(((hmax - hmin)/width * mx) + hmin);
	int yone = (int)(((vmin - vmax)/height * my) + vmax);
	logger.debug("----------------------------------------------------------");
	logger.debug("clickCount = "+clickCount);
	logger.debug(" hmin = "+hmin+" hmax = "+hmax);
	logger.debug(" vmin = "+vmin+" vmax = "+vmax);
	logger.debug(" xone = "+xone+" yone = "+yone);
	logger.debug(" bcenterx = "+centerx+" bcentery = "+centery);	
	if(clickCount == 1) {
	    if(xone < 0) centerx = -centerx;
	    if(yone < 0) centery = -centery;
	} else {
	    //if(hmin < 0 ) centerx = -centerx;
	    //	if(vmin < 0) centery = -centery;
	}
	int xa, xs, ya, ys;
	if(clickCount == 1) {
	    
	    xa = xone - centerx;
	    xs = xone + centerx;
	    ya = yone - centery;
	    ys = yone + centery;
	} else {
	    if(centerx < 0) centerx = -centerx;
	    if(centery < 0) centery = -centery;
	    xa = (int)hmin - centerx;
	    xs = (int)hmax + centerx;
	    ya = (int)vmin - centery;
	    ys = (int)vmax + centery;
	    if((xs - xa) < 50){ xs = xs + 50; xa = xa - 50;}
	    if((ys - ya) < 50) { ys = ys + 50; ya = ya - 50;}
	}
	if(xa > xs) { int temp = xs; xs = xa; xa = temp;}
	if(ya > ys) {int temp = ys; ys = ya; ya = temp;}
	logger.debug(" acenterx = "+centerx+" acentery = "+centery);
	logger.debug(" xa = "+xa+" xs = "+xs);	
	logger.debug(" ya = "+ya+" ys = "+ys);
	//int xtwo = (int)(((hmax - hmin)/width * endPoint.getX()) - hmax);
	//int ytwo = (int)(((vmin - vmax)/height * endPoint.getY()) + vmax);
	startPoint = null;
	endPoint = null;
	if(xa < xs && ya < ys || clickCount != 1) {
	    particleMotionDisplay.updateHorizontalAmpScale(new UnitRangeImpl(xa, xs, UnitImpl.COUNT));
	    particleMotionDisplay.updateVerticalAmpScale(new UnitRangeImpl(ya, ys, UnitImpl.COUNT));
	    vunitRangeImpl = new UnitRangeImpl(ya, ys, UnitImpl.COUNT);
	    hunitRangeImpl = new UnitRangeImpl(xa, xs, UnitImpl.COUNT);
	    particleMotionDisplay.fireAmpRangeEvent(new AmpSyncEvent(ya, ys, true));
	}
    }
  
    public boolean findPoint(int count, int newx, int newy) {
	
	 
	  boolean rtn = false;
	  
	  int size = displays.size();
	  for(int counter = 0; counter < size; counter++) {
	      ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
	      Shape shape = particleMotion.getShape();
	      //for(int i = -4; i < 4; i++) {
	      //	  for(int j = -4; j < 4; j++) {
		      if(shape.contains(newx, newy)){  
			  particleMotion.setSelected(true);rtn = true;
			  // particleMotionDisplay.updateAmpScale(particleMotion.hAmpRangeConfig.getAmpRange(particleMotion.hseis));
		      }
		      // }
		      //}
	  }
	 
	  //logger.debug("The return value is "+rtn);
	  if(rtn == true) {repaint();}
	  return rtn;
			
    }

    
  
    /*public void update(Graphics g) {
	paintComponent(g);
	}*/

    public void paintComponent(Graphics g) {

	//	if(setSelected == 1) g.setColor(Color.red);
	//else if(setSelected == 2) g.setColor(getBackground());
	Graphics2D graphics2D = (Graphics2D)g;

   
	if(startPoint != null && endPoint != null) {
	    graphics2D.setColor(Color.yellow);
	    //logger.debug("Start Point "+startPoint.getX()+"  "+startPoint.getY());
	    //logger.debug("End Point "+endPoint.getX()+"  "+endPoint.getY());
	    java.awt.geom.Rectangle2D.Double rect = new java.awt.geom.Rectangle2D.Double(startPoint.getX(),
											 startPoint.getY(),
											 endPoint.getX() - startPoint.getX(),
											 endPoint.getY() - startPoint.getY());
	    graphics2D.fill(rect);
	    graphics2D.draw(rect);
	}
	Dimension dimension = getSize();
	int size = displays.size();
	Shape sector = getSectorShape();
	graphics2D.setColor(Color.blue);
	graphics2D.fill(sector);
	graphics2D.draw(sector);
	graphics2D.setStroke(new BasicStroke(2.0f));
	graphics2D.setColor(Color.green);
	Shape azimuth = getAzimuthPath();
	graphics2D.draw(azimuth);
	graphics2D.setStroke(new BasicStroke(1.0f));
	for(int counter = 0; counter < displays.size(); counter++) {
	    ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
	    LocalSeismogramImpl hseis = particleMotion.hseis;
	    LocalSeismogramImpl vseis = particleMotion.vseis;
	    AmpRangeConfig vAmpRangeConfig = particleMotion.vAmpRangeConfig;
	    AmpRangeConfig hAmpRangeConfig = particleMotion.hAmpRangeConfig;
	    
	    Color color = particleMotion.getColor();
	    if(color == null) {

		color = COLORS[RGBCOLOR];
		particleMotion.setColor(color);
		RGBCOLOR++;
		if(RGBCOLOR == COLORS.length) RGBCOLOR = 0;
	    }
	    graphics2D.setColor(color);
	    Insets insets = getInsets();
	   dimension = new java.awt.Dimension(dimension.width - insets.left - insets.right,
					       dimension.height - insets.top - insets.bottom);
					       
	    Dimension flipDimension = new Dimension(dimension.height,
						    dimension.width);
	    logger.debug("The width is "+dimension.width+" height is "+dimension.height);
	    
	    try {

		
		logger.debug("In PaintSeismogram hmax = "+hunitRangeImpl.getMaxValue()+
				   " hmin = "+hunitRangeImpl.getMinValue());
		logger.debug("In PaintSeismogram vmax = "+vunitRangeImpl.getMaxValue()+
				   " vmin = "+vunitRangeImpl.getMinValue());
				   
		if(microSecondTimeRange == null) {
		    microSecondTimeRange = new MicroSecondTimeRange(new MicroSecondDate(hseis.getBeginTime()),
								    new MicroSecondDate(hseis.getEndTime()));
		} 

		
		int[][] hPixels = SimplePlotUtil.compressYvalues(hseis, 
								 microSecondTimeRange,
								 hunitRangeImpl,					
								 flipDimension);



		SimplePlotUtil.scaleYvalues(hPixels,
					    hseis, 
					    microSecondTimeRange,
					    hunitRangeImpl,							
					    flipDimension);
	

		int[][] vPixels = SimplePlotUtil.compressYvalues(vseis, 
								 microSecondTimeRange,
								 vunitRangeImpl,			
								 dimension);


		logger.debug("---------------------->Scaling THE Y VALUES ");
		SimplePlotUtil.scaleYvalues(vPixels,
					    vseis, 
					    microSecondTimeRange,
					    vunitRangeImpl,			
					    dimension);

		SimplePlotUtil.flipArray(vPixels[1], dimension.height);

		int len = vPixels[1].length;
		int[] x, y;
		x = new int[hPixels[1].length];
		y = new int[vPixels[1].length];
		x = hPixels[1];
		y = vPixels[1];
		if (hPixels[1].length < len) { len = hPixels.length; }
		logger.debug("-----------------------------------------");
		for(int c = 0;  c < len; c++) {

		    // logger.debug("x = "+hPixels[1][c]+" y = "+vPixels[1][c]);
		  
		}
		g.drawPolyline(hPixels[1], vPixels[1], len);
		Shape shape = getParticleMotionPath(hPixels[1], vPixels[1]);
		particleMotion.setShape(shape);
		graphics2D.draw(shape);
		Composite c = AlphaComposite.getInstance(AlphaComposite.SRC_OUT);
		//graphics2D.setComposite(c);
			
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	    
	 
	}//end of for
	
    }
    
    public Shape getParticleMotionPath(int[] x, int[] y) {
	int len = x.length;
	if(y.length < len) { len = y.length;}
	GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD); 
	if(len != 0) {
		generalPath.moveTo(x[0], y[0]);
	}
	for(int counter = 1; counter < len; counter++) {
	    generalPath.lineTo(x[counter], y[counter]);
	    
	}
	for(int counter = 0; counter < len; counter++) {
	    generalPath.append(new Rectangle2D.Float(x[counter]-2, y[counter]-2, 4, 4), false);
	}

	return (Shape)generalPath;
    }
    
    public Shape getAzimuthPath() {

	//logger.debug("*******************************************************");
	int size = azimuths.size();
	ParticleMotion particleMotion = (ParticleMotion)displays.get(0);
	AmpRangeConfig ampRangeConfig = particleMotion.vAmpRangeConfig;
	UnitRangeImpl unitRangeImpl = vunitRangeImpl;//ampRangeConfig.getAmpRange(particleMotion.vseis);
	double vmin = unitRangeImpl.getMinValue();
	double vmax = unitRangeImpl.getMaxValue();
	unitRangeImpl =  hunitRangeImpl;//particleMotion.hAmpRangeConfig.getAmpRange(particleMotion.hseis);
	double hmin = unitRangeImpl.getMinValue();
	double hmax = unitRangeImpl.getMaxValue();
	
	Insets insets = getInsets();
       
	double  fmin = getSize().getWidth() - insets.left - insets.right;
	double fmax = getSize().getHeight() - insets.top - insets.bottom;
	int originx = (int)(fmin/2);//(int)((hmax - hmin) /4);
	int originy = (int)(fmax/2);//((vmax - vmin) /4);
       	int newx = originx; 
	    //(int)(((originx-hmin) * fmin)/ (hmax - hmin));
	    //(int)(((max - min) / fmin * me.getX()) - max);
	//(int)(max - (((max - min) * (fmin - me.getX()) )/ (fmax - me.getY())));
	int newy =  originy;
	    //(int)(fmax - (((originy-vmin)*fmax)/(vmax - vmin)));
	    //(int)(((min - max) / fmax * me.getY()) + max);
	//logger.debug(" min= "+hmin+" max= "+hmax+" fmin= "+fmin+" fmax= "+fmax);
	//logger.debug("newx = "+newx+" newy = "+newy);
	GeneralPath generalPath = new GeneralPath();
	for(int counter = 0; counter < size; counter++) {
	    
	    double degrees = ((Double)azimuths.get(counter)).doubleValue();
	    degrees = degrees;
	    //logger.debug("The degrees are "+degrees);
 	    int x = (int)(fmin * Math.cos(Math.toRadians(degrees)));
	    int y = (int)(fmax * Math.sin(Math.toRadians(degrees)));

	    //logger.debug("x = "+x+" y= "+y);
	    generalPath.moveTo(newx+x, newy-y);
	    generalPath.lineTo(newx-x, newy+y);
	    //logger.debug("resulttwox = "+(newx-x)+" resulttwoy = "+(newy+y));
	    //logger.debug("resultonex = "+(newx+x)+" resultoney = "+(newy-y));
	      
	}
	//logger.debug("-----------------------------------------------------------");
	return (Shape)generalPath;
    }
    public Shape getSectorShape() {

	ParticleMotion particleMotion = (ParticleMotion)displays.get(0);
	AmpRangeConfig ampRangeConfig = particleMotion.vAmpRangeConfig;
	UnitRangeImpl unitRangeImpl = vunitRangeImpl;//ampRangeConfig.getAmpRange(particleMotion.vseis);
	double vmin = unitRangeImpl.getMinValue();
	double vmax = unitRangeImpl.getMaxValue();
	unitRangeImpl =  hunitRangeImpl;//particleMotion.hAmpRangeConfig.getAmpRange(particleMotion.hseis);
	double hmin = hunitRangeImpl.getMinValue();
	double hmax = hunitRangeImpl.getMaxValue();
	
	Insets insets = getInsets();
	double  fmin = getSize().getWidth() - insets.left - insets.right;
	double fmax = getSize().getHeight() - insets.top - insets.bottom;
	int originx = (int)(fmin/2);//(int)((hmax - hmin) /4);
	int originy = (int)(fmax/2);//((vmax - vmin) /4);

	int newx = originx;
	//(int)(((originx-hmin) * fmin)/ (hmax - hmin));//(int)((max * fmin)/ (max - min));
	int newy = originy;
	//(int)(fmax - (int)(((originy-vmin)*fmax)/(vmax - vmin)));

	GeneralPath generalPath = new GeneralPath();

	int size = sectors.size();
				  
	for(int counter = 0; counter < size; counter++) {

	    Point2D.Double point = (Point2D.Double)sectors.get(counter);
	    double degreeone = point.getX();
	    double degreetwo = point.getY();
	    
	    int xone = (int)(fmin * Math.cos(Math.toRadians(degreeone)));
	    int yone = (int)(fmax * Math.sin(Math.toRadians(degreeone)));
	    generalPath.moveTo(newx+xone, newy-yone);
	    generalPath.lineTo(newx-xone, newy+yone);

	    int xtwo = (int)(fmin * Math.cos(Math.toRadians(degreetwo)));
	    int ytwo = (int)(fmax * Math.sin(Math.toRadians(degreetwo)));
	    generalPath.lineTo(newx-xtwo, newy+ytwo);
	    generalPath.lineTo(newx+xtwo, newy-ytwo);
	    generalPath.lineTo(newx+xone, newy-yone);
	    
	}
	return (Shape)generalPath;
    }
    
    public void addParticleMotionDisplay(LocalSeismogramImpl hseis,
					 LocalSeismogramImpl vseis,
					 AmpRangeConfig hAmpRangeConfig,
					 AmpRangeConfig vAmpRangeConfig) {
	ParticleMotion particleMotion = new ParticleMotion(hseis,
							   vseis,
							   hAmpRangeConfig,
							   vAmpRangeConfig);
	displays.add(particleMotion);

	hunitRangeImpl = new UnitRangeImpl(getMinHorizontalAmplitude(),
					   getMaxHorizontalAmplitude(),
					   UnitImpl.COUNT);
	vunitRangeImpl = new UnitRangeImpl(getMinVerticalAmplitude(),
					   getMaxVerticalAmplitude(),
					   UnitImpl.COUNT);
	particleMotionDisplay.updateHorizontalAmpScale(hunitRangeImpl);
	particleMotionDisplay.updateVerticalAmpScale(vunitRangeImpl);
					   
    }

    
    public void addSector(double degreeone, double degreetwo) {

	sectors.add(new java.awt.geom.Point2D.Double(degreeone, degreetwo));
	
    }

    public double getMinHorizontalAmplitude() {
	
	int size = displays.size();
	double min = Double.POSITIVE_INFINITY;
	for(int counter = 0; counter < size; counter++) {

	    ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
	    AmpRangeConfig ampRangeConfig = particleMotion.hAmpRangeConfig;
	    UnitRangeImpl unitRangeImpl = ampRangeConfig.getAmpRange(particleMotion.hseis);
	    if(min > unitRangeImpl.getMinValue()) { min = unitRangeImpl.getMinValue();}
	}
	return min;
    }

    public double getMaxHorizontalAmplitude() {
	
	int size = displays.size();
	double max = Double.NEGATIVE_INFINITY;
	for(int counter = 0; counter < size; counter++) {

	    ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
	    AmpRangeConfig ampRangeConfig = particleMotion.hAmpRangeConfig;
	    UnitRangeImpl unitRangeImpl = ampRangeConfig.getAmpRange(particleMotion.hseis);
	    if(max < unitRangeImpl.getMaxValue()) { max = unitRangeImpl.getMaxValue();}
	}
	return max;
    }

    public double getMinVerticalAmplitude() {
	
	int size = displays.size();
	double min = Double.POSITIVE_INFINITY;
	for(int counter = 0; counter < size; counter++) {
	    
	    ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
	    AmpRangeConfig ampRangeConfig = particleMotion.vAmpRangeConfig;
	    UnitRangeImpl unitRangeImpl = ampRangeConfig.getAmpRange(particleMotion.vseis);
	    if(min > unitRangeImpl.getMinValue()) { min = unitRangeImpl.getMinValue();}
	}
	return min;
    }
    
    public double getMaxVerticalAmplitude() {
	
	int size = displays.size();
	double max = Double.NEGATIVE_INFINITY;
	for(int counter = 0; counter < size; counter++) {

	    ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
	    AmpRangeConfig ampRangeConfig = particleMotion.vAmpRangeConfig;
	    UnitRangeImpl unitRangeImpl = ampRangeConfig.getAmpRange(particleMotion.vseis);
	    if(max < unitRangeImpl.getMaxValue()) { max = unitRangeImpl.getMaxValue();}
	}
	return max;
    }
    

    public void addAzimuthLine(double degrees) {

	azimuths.add(new Double(degrees));
    }

    /** must be square */
    public void setSize(Dimension d) {
        if (d.width < d.height) {
            super.setSize(new Dimension(d.width, d.width));
        } else {
            super.setSize(new Dimension(d.height, d.height));
        }
    }


    public void setZoomIn(boolean value) {

	this.zoomIn  = true;
	this.zoomOut = false;
    }
    
    public void setZoomOut(boolean value) {

	this.zoomIn = false;
	this.zoomOut = true;
    
    }

    
    public void updateTimeRange(MicroSecondTimeRange microSecondTimeRange) {
	this.microSecondTimeRange = microSecondTimeRange;
	hunitRangeImpl = new UnitRangeImpl(getMinHorizontalAmplitude(),
					   getMaxHorizontalAmplitude(),
					   UnitImpl.COUNT);
	vunitRangeImpl = new UnitRangeImpl(getMinVerticalAmplitude(),
					   getMaxVerticalAmplitude(),
					   UnitImpl.COUNT);
	particleMotionDisplay.updateHorizontalAmpScale(hunitRangeImpl);
	particleMotionDisplay.updateVerticalAmpScale(vunitRangeImpl);
    }

    private boolean zoomIn = false;
    private boolean zoomOut = false;
    
    LinkedList displays = new LinkedList();
    LinkedList azimuths = new LinkedList();
    LinkedList sectors = new LinkedList();
    UnitRangeImpl hunitRangeImpl = null;
    UnitRangeImpl vunitRangeImpl = null;
    java.awt.geom.Point2D.Float startPoint;
    java.awt.geom.Point2D.Float endPoint;
    MicroSecondTimeRange microSecondTimeRange = null;

    private static int RGBCOLOR = 0;
    private ParticleMotionDisplay particleMotionDisplay; 

    private static final Color[]  COLORS = { Color.red,
					   Color.orange,
					   Color.pink,
					   Color.yellow,
					   Color.green,
					   Color.magenta,
					   Color.cyan,
					   Color.blue,
					   Color.white,
					   Color.black};

    class ParticleMotion{
	public ParticleMotion(final LocalSeismogramImpl hseis, 
			       LocalSeismogramImpl vseis,
			       final AmpRangeConfig hAmpRangeConfig,
			       AmpRangeConfig vAmpRangeConfig) {

	    this.hseis = hseis;
	    this.vseis = vseis;
	    this.hAmpRangeConfig = hAmpRangeConfig;
	    this.vAmpRangeConfig = vAmpRangeConfig;
	}

	public void setShape(Shape shape) {
	    this.shape = shape;
	}

	public Shape getShape() {
	    return shape;  
	} 
	public void setColor(Color color) {
	      this.color = color;
	}

	public Color getColor() {
	    if(selected) return Color.cyan;
	    return this.color;
	}

	public boolean isSelected() {
	    return this.selected;
	}

	public void setSelected(boolean value) {

	    this.selected = value;
	}

	public LocalSeismogramImpl hseis;
	public LocalSeismogramImpl vseis;
	public AmpRangeConfig hAmpRangeConfig;
	public AmpRangeConfig vAmpRangeConfig;
	private Shape shape;
	private Color color = null;
	private boolean selected = false;
    }

    static Category logger = 
        Category.getInstance(ParticleMotionView.class.getName());

}// ParticleMotionView
