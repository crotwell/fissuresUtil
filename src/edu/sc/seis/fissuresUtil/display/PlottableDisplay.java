package edu.sc.seis.fissuresUtil.display;

import java.awt.*;

import com.sun.media.jai.codec.PNGEncodeParam;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.utility.Logger;
import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import java.util.ArrayList;
import java.util.*;

/**
 * PlottableDisplay.java
 *
 *
 * Created: Wed Jul 18 11:08:24 2001
 *
 * @author Srinivasa Telukutla
 * Modified: Georgina Coleman
 * @version
 */


public  class PlottableDisplay extends JComponent {

 
    public PlottableDisplay() 
    {
        super();
	
        removeAll();
        final Color bg = Color.white;
        final Color fg = Color.yellow;
     
        //Initialize drawing colors, border, opacity.
        setBackground(bg);
        setForeground(fg);	
        setBorder(BorderFactory.createCompoundBorder(
						     BorderFactory.createRaisedBevelBorder(),
						     BorderFactory.createLoweredBevelBorder()));  
        setLayout(new BorderLayout());
        selectionList = new LinkedList();
        //	add(imagePanel, BorderLayout.CENTER);
        this.colorFactory = new ColorFactory();
        PlottableMouseListener plottableMouseListener = new PlottableMouseListener(this);
        this.addMouseListener(plottableMouseListener);
        this.addMouseMotionListener(plottableMouseListener);
        configChanged();
	
    }

    public PlottableDisplay(ChannelId channelId) {
	this();
	this.channelId = channelId;
    }

    public void setChannelId(ChannelId channelId) {
	this.channelId = channelId;
    }

    public void setDate(Date date) {
	this.date = date;
    }

    public void setOffset(int offset) {
        plotoffset = offset;
        configChanged();
    }

    public void setEvents(EventAccess[] eventAccess) {
	this.eventAccess = eventAccess;
	repaint();
    }

    public void setAmpScale(float ampScalePercent) {
	if (this.ampScalePercent != ampScalePercent) {
	    this.ampScalePercent = ampScalePercent;
	    //	this.ampScale = ampScalePercent;
	    //System.out.println("AmpScale "+ampScalePercent+" "+ampScale);
	    configChanged();
	}
    }

    void configChanged() {
	image = null;
	// signal any drawing thread to stop
	currentImageGraphics = null;

	//System.out.println("psgramwidth size:"+getSize().toString());
	
	int newpsgramwidth = plot_x/plotrows +2*labelXShift;
	int newpsgramheight = plot_y +(plotoffset * (plotrows-1))+titleYShift;
	//System.out.println(plotoffset+"111 psgramwidth" + newpsgramwidth + "psgramheight" + newpsgramheight);
	setPreferredSize(new java.awt.Dimension(newpsgramwidth, newpsgramheight));
	repaint();
    }

    public void setPlottable(edu.iris.Fissures.Plottable[] clientPlott, 
			     String nameofstation) {
        removeAll();

	// signal any drawing thread to stop
	currentImageGraphics = null;

        this.arrayplottable = clientPlott;
	int[] minmax = findMinMax(arrayplottable);
	min = minmax[0];
	max = minmax[1];
	ampScale = plot_y*1f/(max-min);

	plottablename = nameofstation;

	if (arrayplottable == null) {
            Logger.warning("setPlottable:Plottable is NULL.");
	    this.arrayplottable = new Plottable[0];
	}

	plottablename = nameofstation;
	plottableShape = makeShape(clientPlott);
	configChanged();
    }
  
    public void paintComponent(Graphics g) {
        super.paintComponent(g);      //clears the background
	if (image == null) {
	   image = createImage();
	}
	//	g.fillRect(beginx, beginy, (endx - beginx), (endy - beginy));
	g.drawImage(image, 0, 0, this);
	//System.out.println("Repaiting high light region");
	//drawHighlightRegion(g);
    drawSelections(g);
	addEventInfo(this.eventAccess, g);
    }

    protected void drawComponent(Graphics g) {
        drawTitle(g);
        drawTimeTicks(g);

        if (arrayplottable== null ) {
            Logger.warning("Plottable is NULL.");        
            return;
        }

        // for time label on left and title at top
        g.translate(labelXShift, 
                    titleYShift); 
        //System.out.println("clipRect "+ plot_x/plotrows+"  "+ 
        //plot_y +(plotoffset * (plotrows-1)));
        g.clipRect(0, 0, 
                   plot_x/plotrows, 
                   plot_y +(plotoffset * (plotrows-1)));
        drawPlottableNew(g, arrayplottable);
        //drawHighlightRegion(g);
        drawSelections(g);
    }


    void drawTitle(Graphics g) {
	Graphics2D g2 = (Graphics2D)g; 
	g2.setPaint(Color.blue);
	g2.drawString(plottablename, 50, 20);

	String myt = "Time";
	String mygmt = "GMT";

	g2.setPaint(Color.black);
	g2.drawString(myt, 10, 40);
	g2.drawString(myt,sizerow + 50, 40);
	g2.drawString(mygmt, 10, 50);
	g2.drawString(mygmt,sizerow + 50, 50);
       
	return;
    }

    void drawTimeTicks(Graphics g) {

	Graphics2D g2 = (Graphics2D)g; 
    
        int hour=0;
	String minutes = ":00 ";
	int hourinterval=totalhours/plotrows;
	String hourmin = hour+minutes;;
	   
	int houroffset=10;
	int xShift = plot_x/plotrows+labelXShift;
	for (int currRow = 0; currRow < plotrows; currRow++) {

	    if (currRow % 2 == 0) {
		g2.setPaint(Color.black);
	    } else {
		g2.setPaint(Color.blue);
	    }
	    g2.drawString(hourmin, 
			  houroffset, 
			  titleYShift+plot_y/2 + plotoffset*currRow); 

	    hour+=hourinterval; 
	    hourmin = hour+minutes;
	    if(hour>=10) {  houroffset = 5; }
	    g2.drawString(hour+minutes, 
			  xShift+houroffset, 
			  titleYShift+plot_y/2 + plotoffset*currRow); 
	}

    }

    void drawPlottableNew(Graphics g, Plottable[] plot) {

	int xShift = plot_x/plotrows;
	mean = getMean();
	//stem.out.println("plottable.length"+plot.length);

	// get new graphics to avoid messing up original
	Graphics2D newG = (Graphics2D)g.create(); 
	
	int[] selectedRows = getSelectedRows(beginy, endy);

	//stem.out.println("The plot_y is "+plot_y+" plottoffset is "+plotoffset);
	for (int currRow = 0; currRow < plotrows; currRow++) {
	    if (g != currentImageGraphics) return;

	    //System.out.println(currRow);

	    // shift for row (left so time is in window, 
	    //down to correct row on screen, plus
	    //	    newG.translate(xShift*currRow, plot_y/2 + plotoffset*currRow);
	    java.awt.geom.AffineTransform original = newG.getTransform();
	    java.awt.geom.AffineTransform affine = newG.getTransform();
	    
	    affine.concatenate(affine.getTranslateInstance(-1*xShift*currRow,
						 plot_y/2+plotoffset*currRow));
	    // account for graphics y positive down
	    affine.concatenate(affine.getScaleInstance(1, -1));

	    newG.setTransform(affine);
 	    newG.setPaint(Color.red);

	    newG.drawLine(0, 0, 6000, 0);
	    AlphaComposite originalComposite = (AlphaComposite)newG.getComposite();
	    AlphaComposite newComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
								     .4f);
	    newG.setComposite(originalComposite);
	    affine.concatenate(affine.getScaleInstance(1, ampScale));
	    affine.concatenate(affine.getScaleInstance(1, ampScalePercent));
	  
	    // translate max so mean is in middle
	    affine.concatenate(affine.getTranslateInstance(0, -1*mean));
	    newG.setTransform(affine);
	    
	    //System.out.println(currRow+": "+(-1*currRow*xShift)+", "+currRow*plotoffset+"  "+mean);
	    if (currRow % 2 == 0) {
		newG.setPaint(Color.black);
	    } else {
		newG.setPaint(Color.blue);
	    }
	    if (plottableShape != null) {
		if (g != currentImageGraphics) return;
		newG.draw(plottableShape);
	    } // end of if (plottableShape != null)

	    newG.setTransform(original);

	    // draw partial image
	    if (g != currentImageGraphics) return;
	    repaint();
	}
	    newG.dispose();
    }

    private Shape makeShape( Plottable[] plot) {
	final int SHAPESIZE = 100;
	GeneralPath wholeShape = 
	    new GeneralPath(GeneralPath.WIND_EVEN_ODD);	
	for (int a=0; a<plot.length; a++) {
	    if(plot[a].x_coor.length >= 2){
		GeneralPath currentShape = 
		    new GeneralPath(GeneralPath.WIND_EVEN_ODD, 
				    SHAPESIZE+1);
		currentShape.moveTo(plot[a].x_coor[0], 
				    plot[a].y_coor[0]);
		for(int i = 1; i < plot[a].x_coor.length; i++) {
		    //split into smaller shapes
		    if (i % SHAPESIZE == 0) {
			// duplicate last point
			if (plot[a].x_coor[i-1] == plot[a].x_coor[i]-1) {
			    currentShape.moveTo(plot[a].x_coor[i], 
						plot[a].y_coor[i]);
			} else {
			    currentShape.lineTo(plot[a].x_coor[i], 
						plot[a].y_coor[i]);
			    
			} // end of else
			//System.out.println("Bounds "+currentShape.getBounds().width+" "+currentShape.getBounds().x);
			 wholeShape.append(currentShape, false);
			 currentShape = 
			     new GeneralPath(GeneralPath.WIND_EVEN_ODD, 
					     SHAPESIZE+1);
		    } // end of if (i % 100 == 0)
		    
		    if (plot[a].x_coor[i-1] == plot[a].x_coor[i]-1) {
			currentShape.moveTo(plot[a].x_coor[i], 
					    plot[a].y_coor[i]);
		    } else {
			currentShape.lineTo(plot[a].x_coor[i], 
					    plot[a].y_coor[i]);
			
		    } // end of else
		}
		wholeShape.append(currentShape, false);
		
	    } else if (plot[a].x_coor.length == 1){
		GeneralPath currentShape = 
		    new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
		currentShape.moveTo(plot[a].x_coor[0], 
				    plot[a].y_coor[0]);
		currentShape.lineTo(plot[a].x_coor[0], 
				    plot[a].y_coor[0]);
		wholeShape.append(currentShape, false);
	    }
	    // only do first row
	    // break;
	}
	return wholeShape;
    }

    


    public void psgramResize(int psgramwidth,int psgramheight ) {
	setSize(new java.awt.Dimension (psgramwidth, psgramheight));
	setPreferredSize(new java.awt.Dimension (psgramwidth,psgramheight));
	return;
    }

    protected int getMean() {
	if (arrayplottable == null
	    || arrayplottable.length == 0
	    || arrayplottable[0].y_coor.length == 0) {
	    return 0;
	} 
	
	long mean=arrayplottable[0].y_coor[0];
	int numPoints=0;

	for (int i=0; i< arrayplottable.length; i++) {
	    for (int j=0; j<arrayplottable[i].y_coor.length; j++) {
		mean += arrayplottable[i].y_coor[j];
	    }
	    numPoints += arrayplottable[i].y_coor.length;
	}
	mean = mean / numPoints;
	return (int)mean;
    }

    /** breaks up large Plottables into smaller ones to make drawing faster,
     *  ie, less has to be drawn to get the rows plotted.
     */
    protected void makeSegments() {

    }

    public Image createImage() {
        
   	final int width = plot_x/plotrows +2*labelXShift;
	final int height = plot_y +(plotoffset * (plotrows-1))+titleYShift;

	final Image offImg = super.createImage(width, height);
	//Image offImg = 
	//   imagePanel.createImage(new MemoryImageSource(width, height, 
	//					 new int[width*height],
	//					 0, width));

	Thread t = new Thread() {
		public void run() {
		    //  Graphics2D g = offImg.createGraphics();
		    Graphics2D g = (Graphics2D)offImg.getGraphics();
		    currentImageGraphics = g;
		    g.setBackground(Color.white);

		    // clear canvas
		    g.clearRect(0, 0, width, height);

		    drawComponent(g);
		    g.dispose();
		    repaint();
		}
	    };
	t.start();


        return offImg;
    }

  
    public void showImage( Image image){

	// Display the image.
	//System.out.println("Displaying image");
	
	// Get the image width and height.
   	int width = plot_x/plotrows +2*labelXShift;
	int height = plot_y +(plotoffset * (plotrows-1))+titleYShift;

	// Attach the image to a scrolling panel to be displayed.
	//	ScrollingImagePanel panel = new ScrollingImagePanel(
	//						    image, width, height);

	JScrollPane scroll = new JScrollPane(imagePanel);

	// Create a Frame to contain the panel.
	Frame window = new Frame("created picture");
	window.add(scroll);
	window.pack();
	window.show(); 
         
    }

    public void nonGUIwritePNG(String fileToWriteTo) {

        File outputFile = new File(fileToWriteTo+".png");
	writePNG(outputFile);
    }

    public void writePNG(File fileToWriteTo) {

        /* Receives an image from the Graphics that was drawn */
	    Image g_image = createImage();
     
	    // Create the ParameterBlock.
	    ParameterBlock pb = new ParameterBlock();
	    pb.add(g_image);

	    // Create the AWTImage operation.
	    PlanarImage image= (PlanarImage)JAI.create("awtImage", pb);

	    javax.media.jai.RenderedImageAdapter rendimage = 
		new javax.media.jai.RenderedImageAdapter(image);

	    //if(image == null) System.out.println("Planar image is null");
	    //else System.out.println(rendimage.toString());

	    try{
		FileOutputStream os = new FileOutputStream(fileToWriteTo);
		com.sun.media.jai.codec.PNGEncodeParam.RGB  param = new PNGEncodeParam.RGB();
		param.setBitDepth(16);
	   
		JAI.create("encode", image, os, "PNG", param);

		os.close();
	    }catch (FileNotFoundException e){
	    }catch(IOException ioe){
	    } 

    }//close writePNG
    public int[] findMinMax(Plottable[] arrayplottable) {
	if (arrayplottable.length == 0) {
	    int[] minandmax = new int[2];
	    minandmax[0]= -1;
	    minandmax[1]= 1;
	    return minandmax;
	} // end of if (arrayplottable.length == 0)

       int min = arrayplottable[0].y_coor[0];
       int max = arrayplottable[0].y_coor[0];
       for(int arrayi=0; arrayi<arrayplottable.length ; arrayi++) {        
	   for(int ploti=0; ploti<arrayplottable[arrayi].y_coor.length ; ploti++) {            
	       min = Math.min(min, arrayplottable[arrayi].y_coor[ploti]);
	       max = Math.max(max, arrayplottable[arrayi].y_coor[ploti]);
	   } 
       }

       int[] minandmax = new int[2];
       minandmax[0]= min;
       minandmax[1]= max;

       //       System.out.println("Array Min:"+min+" ArrayMax:"+max);

       return minandmax;
   }
    
    public void setSelectedSelection(int beginx, int beginy) {
        this.plottableSelection = null;
        Iterator iterator = selectionList.iterator();
        while(iterator.hasNext()) {

            PlottableSelection plottableSelection = (PlottableSelection) iterator.next();
            if(plottableSelection.isSelectionSelected(beginx, beginy)) {
                this.plottableSelection = plottableSelection;
                return;
            }
        }
        
        PlottableSelection selection = new PlottableSelection(this,
                                                              colorFactory.getNextColor());
        selection.startXY(beginx, beginy);
        selectionList.add(selection);
        this.plottableSelection = selection;
     }

    

    public void setSelectedRectangle(int beginx, int beginy, int endx, int endy) {
       
        this.beginx = beginx;
        this.beginy = beginy;
        this.endx = endx;
        this.endy = endy;
        if(this.plottableSelection != null) {
            plottableSelection.setXY(endx, endy);
            repaint();
        }
    }

    private void drawSelections(Graphics g) {
        Iterator iterator = selectionList.iterator();
        while(iterator.hasNext()) {
            PlottableSelection plottableSelection = (PlottableSelection) iterator.next();
            plottableSelection.draw((Graphics2D)g, null, null, null);
        }
    }
    
    private int[] getSelectedRows(int beginy, int endy) {
        
        if(beginy == -1 || endy == -1) return new int[0];
        // beginy = (int)(beginy * this.ampScalePercent);
        //endy = (int)(endy * this.ampScalePercent);
        ArrayList arrayList = new ArrayList();
        int selectionOffset = plotoffset / 2;
        for(int counter = 0; counter < plotrows; counter++) {
	    int value =  (plot_y/2 + titleYShift + plotoffset*counter);
   
        //if(((value - selectionOffset) <= beginy) &&
        if( (beginy <= (value + selectionOffset)) &&
           (endy >= beginy) && 
            (endy > (value - selectionOffset))) {
            //(endy <= (value + selectionOffset))) { 
            arrayList.add(new Integer(counter));
        }
        }
        int[] rtnValues = new int[arrayList.size()];
        for(int counter = 0; counter < arrayList.size(); counter++) {
            rtnValues[counter] = ((Integer)arrayList.get(counter)).intValue();
            //System.out.println("The row selected is "+rtnValues[counter]);
        }
        return rtnValues;
    }


    private boolean isRowSelected(int[] rows, int currrow) {
        for(int counter = 0; counter < rows.length; counter++) {
            if(rows[counter] == currrow) return true;
        }
        return false;
    }

    private void drawHighlightRegion(Graphics g) {
	// get new graphics to avoid messing up original
	Graphics2D newG = (Graphics2D)g.create(); 
	int[] minmax = findMinMax(arrayplottable);
	if(g != currentImageGraphics) {
	    newG.translate(labelXShift,
			   titleYShift);
	    newG.clipRect(0, 0, 
			  plot_x/plotrows, 
			  plot_y +(plotoffset * (plotrows-1)));
	}

	int xShift = plot_x/plotrows;
	int mean = getMean();
	int[] selectedRows = getSelectedRows(beginy, endy);
	for (int currRow = 0; currRow < plotrows; currRow++) {

	    // shift for row (left so time is in window, 
	    //down to correct row on screen, plus
	    //	    newG.translate(xShift*currRow, plot_y/2 + plotoffset*currRow);
	    java.awt.geom.AffineTransform original = newG.getTransform();
	    java.awt.geom.AffineTransform affine = newG.getTransform();
	  
	    affine.concatenate(affine.getTranslateInstance(-1*xShift*currRow,
						 plot_y/2+plotoffset*currRow));
	    // account for graphics y positive down
	    affine.concatenate(affine.getScaleInstance(1, -1));

	    newG.setTransform(affine);
 	    newG.setPaint(Color.red);

	    AlphaComposite originalComposite = (AlphaComposite)newG.getComposite();
	    AlphaComposite newComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
								     .4f);
	    Point2D.Float beginPoint = new Point2D.Float(beginx, endx);
	    newG.setPaint(Color.green);
	    newG.setComposite(newComposite);
	    if(isRowSelected(selectedRows, currRow)) {
		int bx = 0;
		int ex = 0;
		int by =  -plotoffset/2 + 10;
		int ey = plotoffset - 10;
		if(currRow == selectedRows[0] ) {
		    //System.out.println("Calculating values for start row");
		    bx =  beginx + xShift*currRow -labelXShift;// + beginx;
		    if(selectedRows.length  != 1) {
                ex = 6000;
		    } else {
                ex = endx - beginx;
		    }
        } else if(currRow == selectedRows[selectedRows.length -1 ]) {
		    //System.out.println("Caculating values for end row "+currRow);
		    bx = xShift*currRow - labelXShift;;
		    ex = (endx);
		} else {
		    bx = 0;
		    ex = 6000;
        }
        
		//System.out.println("NOW DRAW THE rectangle for row "+currRow);
		newG.drawRect(bx, by, ex, ey);
		newG.fillRect(bx, by, ex, ey);
		
	    }//end of if
	    newG.setTransform(original);
	}//end of for
	//getRequestFilter();
	    newG.dispose();
	   
    }

    public RequestFilter getRequestFilter() {
	if(endx == -1) return null;
	int[] selectedRows = getSelectedRows(beginy, endy);
	if(selectedRows.length == 0) return null;
	int rowvalue = 24/plotrows;
	int plotwidth = plot_x/plotrows;
        float beginvalue = ((beginx/(float)plotwidth)) * rowvalue + selectedRows[0] * rowvalue;
	float endvalue = (endx/(float)plotwidth) * rowvalue + selectedRows[selectedRows.length - 1] * rowvalue;
	return new RequestFilter(this.channelId, 
				 getTime(beginvalue).getFissuresTime(),
				 getTime(endvalue).getFissuresTime());
	//edu.iris.Fissures.Time begin_time ;
	
// 	System.out.println(" b value is "+((beginx/plotwidth)));
// 	System.out.println(" b value is "+((endx/plotwidth)));
// 	System.out.println("The beginValue is "+beginvalue);
// 	System.out.println("The endValue is "+endvalue);
// 	System.out.println("The end time of selection is "+ getTime(endvalue));

    }

    private MicroSecondDate getTime(float rowoffsetvalue) {
	
	int tempmilliseconds =(int) (rowoffsetvalue * 60 * 60 * 1000);
	int hours = tempmilliseconds / (60 * 60 * 1000);
	tempmilliseconds = tempmilliseconds - hours * 60 * 60 * 1000;
	int minutes = tempmilliseconds / (60 * 1000);
	tempmilliseconds = tempmilliseconds - minutes * 60 * 1000;
	int seconds = tempmilliseconds / 1000;
	tempmilliseconds = tempmilliseconds - seconds * 1000;
 	Calendar calendar = Calendar.getInstance();
	calendar.setTime(this.date);
	GregorianCalendar gregorianCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR),
								    calendar.get(Calendar.MONTH),
								    calendar.get(Calendar.DATE),
								    hours,
								    minutes,
								    seconds);
								    
								    

	gregorianCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
	return new MicroSecondDate(gregorianCalendar.getTime());
    }


    public void addEventInfo(EventAccess[] eventAccessArray, Graphics g) {
	System.out.println("The length of the eventArray is "+eventAccessArray.length);
	int[] rows = new int[eventAccessArray.length];
	int[] cols = new int[eventAccessArray.length];
	for(int  counter = 0; counter < eventAccessArray.length; counter++) {
	    rows[counter] = getEventRow(eventAccessArray[counter]);
	    cols[counter] = getEventColumn(eventAccessArray[counter]);
	}
	drawEvents(eventAccessArray, rows, cols, g);
    }

    private int getEventRow(EventAccess eventAccess) {
	try {
	    Origin origin = eventAccess.get_preferred_origin();
	    edu.iris.Fissures.Time time = origin.origin_time;
	    System.out.println("ORIGIN TIME: "+new MicroSecondDate(time));
	    
	    long microSeconds =  ( new MicroSecondDate(time)).getMicroSecondTime();
	    float colhours = microSeconds/(1000 * 1000 * 60 * 60);
	    Calendar calendar = Calendar.getInstance();
	    Date date = new Date(microSeconds/1000);
	    System.out.println("The date rebuilt ORIGIN TIME: "+new MicroSecondDate(date));
	    calendar.setTime(date);
	    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
	    int hours = calendar.get(Calendar.HOUR_OF_DAY);
	    System.out.println("THe hour of the day is "+hours);
	    return hours/2; 
				     
	} catch(Exception e) {

	}
	return -1;

    }


     private int getEventColumn(EventAccess eventAccess) {
	try {
	    Origin origin = eventAccess.get_preferred_origin();
	    edu.iris.Fissures.Time time = origin.origin_time;
	    long microSeconds =  ( new MicroSecondDate(time)).getMicroSecondTime();
	    Calendar calendar = Calendar.getInstance();
	    Date date = new Date(microSeconds/1000);
	    calendar.setTime(date);
	    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
	    int minutes = calendar.get(Calendar.MINUTE);
	    int seconds = calendar.get(Calendar.SECOND);
	    
	    float colhours = (minutes * 60 + seconds) / (float) (60 * 60);
	    
	    System.out.println("The value of colhours is "+colhours);
	    int rowvalue = 24/plotrows;
	    int plotwidth = plot_x/plotrows;
	    int  rtnvalue = (int)(((float)plotwidth/rowvalue) * colhours);
	    
	    return rtnvalue;
				     
	} catch(Exception e) {

	}
	return -1;

    }

    private void drawEvents(EventAccess[] eventAccessArray, int[] rows, int[] columns, Graphics g) {
	// get new graphics to avoid messing up original
	Graphics2D newG = (Graphics2D)g.create(); 
	int[] minmax = findMinMax(arrayplottable);
	if(g != currentImageGraphics) {
	    newG.translate(labelXShift,
			   titleYShift);
	    newG.clipRect(0, 0, 
			  plot_x/plotrows, 
			  plot_y +(plotoffset * (plotrows-1)));
	}
	
	int xShift = plot_x/plotrows;
	int mean = getMean();
	int[] selectedRows = getSelectedRows(beginy, endy);
	for (int currRow = 0; currRow < plotrows; currRow++) {

	    // shift for row (left so time is in window, 
	    //down to correct row on screen, plus
	    //	    newG.translate(xShift*currRow, plot_y/2 + plotoffset*currRow);
	    java.awt.geom.AffineTransform original = newG.getTransform();
	    java.awt.geom.AffineTransform affine = newG.getTransform();
	  
	    affine.concatenate(affine.getTranslateInstance(-1*xShift*currRow,
						 plot_y/2+plotoffset*currRow));
	    // account for graphics y positive down
	    affine.concatenate(affine.getScaleInstance(1, -1));

	    newG.setTransform(affine);
 	    newG.setPaint(Color.red);

	    AlphaComposite originalComposite = (AlphaComposite)newG.getComposite();
	    AlphaComposite newComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
								     .4f);
	    newG.setComposite(newComposite);
	    
	    for(int counter = 0; counter < eventAccessArray.length; counter++) {
		if(rows[counter] == currRow) {
		    System.out.println("Drawing on row currRow "+currRow+" column = "+columns[counter]);
		    newG.drawRect( xShift*currRow + columns[counter] - 5, -20,10,40);
		    newG.fillRect( xShift*currRow +  columns[counter] - 5, -20,10,40);
		}
	    }
	    newG.setTransform(original);
	}//end of for
	//getRequestFilter();
	newG.dispose();
    }

    
    private ColorFactory colorFactory;
    private LinkedList selectionList;

    private PlottableSelection plottableSelection = null;

    /** Solely for use to d3etermine if drawing thread is still current. */
    protected Graphics2D currentImageGraphics = null;
    
    private EventAccess[] eventAccess = new EventAccess[0];

    protected JLabel imagePanel = new JLabel("no image");

    private edu.iris.Fissures.Plottable[] arrayplottable = new edu.iris.Fissures.Plottable[0] ; 
    private String  plottablename="Please, choose a SCEPP station and then click refresh on the menu above.";
    Image image = null;
    Shape plottableShape = null;

    /* Defaults for plottable */
    public  int plotrows=12;
    public int sizerow;
    public int plotoffset=60; 
    public String plottitle="true";  
    public int plot_x=6000; 
    public int plot_y=2000;
    public int plotwidth=700;
    public int plotheight=2600;
    public int totalhours = 24;

    public ChannelId channelId;
    public int min;
    public int max;
    public int mean;
    float ampScale = 1.0f;
    float ampScalePercent = 1.0f;
    int titleYShift = 40;
    int labelXShift = 50;

    int beginx = -1;
    int beginy = -1;

    int endx = -1;
    int endy  = -1;
    protected Date date;

    }/*close class*/

