package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfPlottable.*;
import edu.iris.Fissures.display.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.utility.*;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import java.awt.image.*;
import java.awt.Frame;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.*;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import com.sun.media.jai.codec.FileSeekableStream;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.media.jai.PlanarImage;
import com.sun.media.jai.codec.*;

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

  

public  class PlottableDisplay extends JPanel {

 
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

	configChanged();
    }

    public void setOffset(int offset) {
	plotoffset = offset;
	configChanged();
    }

    public void setAmpScale(float ampScalePercent) {
	this.ampScalePercent = ampScalePercent;
	this.ampScale = ampScalePercent*plot_y/(max-min);
	System.out.println("AmpScale "+ampScalePercent+" "+ampScale);
	configChanged();
    }

    void configChanged() {
	bufferedImage = null;
	System.out.println("psgramwidth size:"+getSize().toString());
	
	int newpsgramwidth = plot_x/plotrows +2*labelXShift;
	int newpsgramheight = plot_y +(plotoffset * (plotrows-1))+titleYShift;
	System.out.println(plotoffset+"111 psgramwidth" + newpsgramwidth + "psgramheight" + newpsgramheight);
	setPreferredSize(new java.awt.Dimension(newpsgramwidth, newpsgramheight));
	repaint();
    }

    public void setPlottable(edu.iris.Fissures.Plottable[] clientPlott, 
			     String nameofstation) {
        removeAll();
        this.arrayplottable = clientPlott;
	int[] minmax = findMinMax(arrayplottable);
	min = minmax[0];
	max = minmax[1];
	setAmpScale(ampScalePercent);
	plottablename = nameofstation;

	if (arrayplottable == null) {
            Logger.warning("setPlottable:Plottable is NULL.");
	    this.arrayplottable = new Plottable[0];
	}

	plottablename = nameofstation;
	makeSegments();
	configChanged();
    }
  
    public void paintComponent(Graphics g) {
        super.paintComponent(g);      //clears the background
	if (bufferedImage == null) {
	   bufferedImage = createBufferedImage();
	} // end of if (bufferredImage == null)
	g.drawImage(bufferedImage, 0, 0, this);
    }

    protected void drawComponent(Graphics g) {
	drawTitle(g);
	drawTimeTicks(g);

	if (arrayplottable== null ) {
            Logger.warning("Plottable is NULL.");        
	    return;
	} 

	Graphics newG = g.create();

	// for time label on left and title at top
	newG.translate(labelXShift, 
		       titleYShift); 
	System.out.println("clipRect "+ plot_x/plotrows+"  "+ 
		      plot_y +(plotoffset * (plotrows-1)));
	newG.clipRect(0, 0, 
		      plot_x/plotrows, 
		      plot_y +(plotoffset * (plotrows-1)));
	drawPlottableNew(newG, arrayplottable);
	newG.dispose();
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
	for (int currRow = 0; currRow < plotrows; currRow++) {
	    System.out.println(currRow+" "+xShift+" "+min+" "+max+" "+ampScale+" "+(ampScale*min)+" "+(ampScale*max)+" "+plotoffset+" "+plot_y+" "+plot_x);
	    // get new graphics to avoid messing up original
	    Graphics2D newG = (Graphics2D)g.create(); 
	    // shift for row (left so time is in window, 
	    //down to correct row on screen, plus
	    //	    newG.translate(xShift*currRow, plot_y/2 + plotoffset*currRow);
	    newG.translate(-1*xShift*currRow,  plot_y/2+plotoffset*currRow);

	    // account for graphics y positive down
	    newG.scale(1, -1);

	    newG.scale(1, ampScale);

	    // translate max so mean is in middle
	    newG.translate(0, -1*mean);

 	    newG.setPaint(Color.red);
 	    newG.drawLine(0, 0, 6000, 0);

	     System.out.println(currRow+": "+(-1*currRow*xShift)+", "+currRow*plotoffset+"  "+getMean());
	    if (currRow % 2 == 0) {
		newG.setPaint(Color.black);
	    } else {
		newG.setPaint(Color.blue);
	    }
		
	    for (int i=0; i<arrayplottable.length; i++) {
		int lastXValue = 
		    arrayplottable[i].x_coor[arrayplottable[i].x_coor.length-1];
	
		// only draw plottable if it overlaps displayed part of row
		if (( (xShift*currRow) <= arrayplottable[i].x_coor[0]
		    && (xShift*(currRow+1)) <= arrayplottable[i].x_coor[0])
		    || (xShift*(currRow) >= lastXValue
			&& (xShift*(currRow+1)) >= lastXValue)) {
		    // no overlap

		} else {
		    newG.drawPolyline(arrayplottable[i].x_coor, 
				      arrayplottable[i].y_coor, 
				      arrayplottable[i].x_coor.length);
		} // end of else
		
	    }
	    

	    newG.dispose();
	    //break; // only do first row
	}
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

    public java.awt.image.BufferedImage createBufferedImage() {
        
   	int width = plot_x/plotrows +2*labelXShift;
	int height = plot_y +(plotoffset * (plotrows-1))+titleYShift;

	java.awt.image.BufferedImage offImg = new java.awt.image.BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);


        Graphics2D g = offImg.createGraphics();
        g.setBackground(Color.white);

        // clear canvas
	g.clearRect(0, 0, width, height);

        drawComponent(g);



        return offImg;
    }



    public void showImage( BufferedImage image){

	// Display the image.
	//System.out.println("Displaying image");
	
	// Get the image width and height.
   	int width = plot_x/plotrows +2*labelXShift;
	int height = plot_y +(plotoffset * (plotrows-1))+titleYShift;

	// Attach the image to a scrolling panel to be displayed.
	ScrollingImagePanel panel = new ScrollingImagePanel(
							    image, width, height);

	// Create a Frame to contain the panel.
	Frame window = new Frame("created picture");
	window.add(panel);
	window.pack();
	window.show(); 
         
    }

    public void nonGUIwritePNG(String fileToWriteTo) {

        File outputFile = new File(fileToWriteTo+".png");
	writePNG(outputFile);
    }

    public void writePNG(File fileToWriteTo) {

        /* Receives an image from the Graphics that was drawn */
	    java.awt.image.BufferedImage g_image = createBufferedImage();
     
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

       System.out.println("Array Min:"+min+" ArrayMax:"+max);

       return minandmax;
   }

    private edu.iris.Fissures.Plottable[] arrayplottable = new edu.iris.Fissures.Plottable[0] ; 
    private String  plottablename="Please, click on DataSource.";
    java.awt.image.BufferedImage bufferedImage = null;

    /* Defaults for plottable */
    public int plotrows=12;
    public int sizerow;
    public int plotoffset=60; 
    public String plottitle="true";  
    public int plot_x=6000; 
    public int plot_y=2000;
    public int plotwidth=700;
    public int plotheight=2600;
    public int totalhours = 24;

    public int min;
    public int max;
    public int mean;
    float ampScale = 1.0f;
    float ampScalePercent = 1.0f;
    int titleYShift = 40;
    int labelXShift = 50;
    }/*close class*/

