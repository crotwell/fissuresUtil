package edu.sc.seis.fissuresUtil.display;

import java.awt.*;

import com.sun.media.jai.codec.PNGEncodeParam;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.utility.Logger;
import edu.sc.seis.fissuresUtil.display.drawable.EventFlagPlotter;
import edu.sc.seis.fissuresUtil.display.drawable.PlottableSelection;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

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


    public PlottableDisplay() {
        super();
        removeAll();
        final Color bg = Color.white;
        final Color fg = Color.yellow;
        //Initialize drawing colors, border, opacity.
        setBackground(bg);
        setForeground(fg);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                                                     BorderFactory.createLoweredBevelBorder()));
        setLayout(new BorderLayout());
        eventPlotterList = new LinkedList();
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

    public void setEvents(EventAccessOperations[] eventAccess) {
        this.eventAccess = eventAccess;
        eventPlotterList = new LinkedList();
        addEventPlotterInfo(eventAccess);
        repaint();
    }

    public void setAmpScale(float ampScalePercent) {
        if (this.ampScalePercent != ampScalePercent) {
            this.ampScalePercent = ampScalePercent;
            configChanged();
        }
    }

    void configChanged() {
        image = null;
        // signal any drawing thread to stop
        currentImageGraphics = null;
        int newpsgramwidth = plot_x/plotrows +2*LABEL_X_SHIFT;
        int newpsgramheight = plot_y +(plotoffset * (plotrows-1))+TITLE_Y_SHIFT;
        setPreferredSize(new java.awt.Dimension(newpsgramwidth, newpsgramheight));
        repaint();
    }

    public void setPlottable(edu.iris.Fissures.Plottable[] clientPlott,
                             String nameofstation) {
        eventPlotterList = new LinkedList();
        selection = null;
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
        g.drawImage(image, 0, 0, this);
        drawSelections(g);
        drawEventFlagPlotters(g);
    }

    protected void drawComponent(Graphics g) {
        drawTitle(g);
        drawTimeTicks(g);

        if (arrayplottable== null ) {
            Logger.warning("Plottable is NULL.");
            return;
        }

        // for time label on left and title at top
        g.translate(LABEL_X_SHIFT,
                    TITLE_Y_SHIFT);
        g.clipRect(0, 0,
                   plot_x/plotrows,
                   plot_y +(plotoffset * (plotrows-1)));
        drawPlottableNew(g, arrayplottable);
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
        int xShift = plot_x/plotrows+LABEL_X_SHIFT;
        for (int currRow = 0; currRow < plotrows; currRow++) {

            if (currRow % 2 == 0) {
                g2.setPaint(Color.black);
            } else {
                g2.setPaint(Color.blue);
            }
            g2.drawString(hourmin,
                          houroffset,
                          TITLE_Y_SHIFT+plot_y/2 + plotoffset*currRow);

            hour+=hourinterval;
            hourmin = hour+minutes;
            if(hour>=10) {  houroffset = 5; }
            g2.drawString(hour+minutes,
                          xShift+houroffset,
                          TITLE_Y_SHIFT+plot_y/2 + plotoffset*currRow);
        }

    }

    void drawPlottableNew(Graphics g, Plottable[] plot) {
        int xShift = plot_x/plotrows;
        mean = getMean();
        // get new graphics to avoid messing up original
        Graphics2D newG = (Graphics2D)g.create();

        for (int currRow = 0; currRow < plotrows; currRow++) {
            if (g != currentImageGraphics) return;

            // shift for row (left so time is in window,
            //down to correct row on screen, plus
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
            newG.setComposite(originalComposite);
            affine.concatenate(affine.getScaleInstance(1, ampScale));
            affine.concatenate(affine.getScaleInstance(1, ampScalePercent));

            // translate max so mean is in middle
            affine.concatenate(affine.getTranslateInstance(0, -1*mean));
            newG.setTransform(affine);

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
        }
        return wholeShape;
    }

    public void psgramResize(int psgramwidth,int psgramheight ) {
        setSize(new java.awt.Dimension (psgramwidth, psgramheight));
        setPreferredSize(new java.awt.Dimension (psgramwidth,psgramheight));
        return;
    }

    public int getMean() {
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

    public Image createImage() {

        final int width = plot_x/plotrows +2*LABEL_X_SHIFT;
        final int height = plot_y +(plotoffset * (plotrows-1))+TITLE_Y_SHIFT;

        final Image offImg = super.createImage(width, height);

        Thread t = new Thread("Plottable Image Creator") {
            public void run() {
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
        PlanarImage image= JAI.create("awtImage", pb);

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
        int[] minandmax ={ min, max};
        return minandmax;
    }

    public void setSelectedSelection(int beginx, int beginy) {
        if(selection != null && selection.isSelectionSelected(beginx, beginy)) {
            return;
        }
        selection = new PlottableSelection(this,
                                           colorFactory.getNextColor(),
                                           beginx, beginy);
    }

    public void setSelectedRectangle(int beginx, int beginy, int endx, int endy) {
        this.beginx = beginx;
        this.beginy = beginy;
        this.endx = endx;
        this.endy = endy;
        if(selection != null) {
            selection.setXY(endx, endy);
            repaint();
        }
    }

    private void drawSelections(Graphics g) {
        if(selection != null){
            selection.draw((Graphics2D)g, null, null, null);
        }
    }

    private void drawEventFlagPlotters(Graphics g) {
        Iterator iterator = eventPlotterList.iterator();
        while(iterator.hasNext()) {
            EventFlagPlotter plotter = (EventFlagPlotter) iterator.next();
            plotter.draw((Graphics2D)g, null, null, null);
        }
    }

    public int[] getSelectedRows(int beginy, int endy) {
        if(beginy == -1 || endy == -1) return new int[0];
        ArrayList arrayList = new ArrayList();
        int selectionOffset = plotoffset / 2;
        for(int counter = 0; counter < plotrows; counter++) {
            int value =  (plot_y/2 + TITLE_Y_SHIFT + plotoffset*counter);
            if( (beginy <= (value + selectionOffset)) &&
                   (endy >= beginy) &&
                   (endy > (value - selectionOffset))) {
                arrayList.add(new Integer(counter));
            }
        }
        int[] rtnValues = new int[arrayList.size()];
        for(int counter = 0; counter < arrayList.size(); counter++) {
            rtnValues[counter] = ((Integer)arrayList.get(counter)).intValue();
        }
        return rtnValues;
    }

    public void setSelectedEventFlag(MouseEvent me) {
        if(me.getClickCount() == 2) {
            Iterator iterator = eventPlotterList.iterator();
            while(iterator.hasNext()) {
                EventFlagPlotter plotter = (EventFlagPlotter) iterator.next();
                if(plotter.isSelected(me.getX(), me.getY())) {
                    plotter.setSelected(true);
                } else {
                    plotter.setSelected(false);
                }
            }
        }
        repaint();
    }

    private boolean isRowSelected(int[] rows, int currrow) {
        for(int counter = 0; counter < rows.length; counter++) {
            if(rows[counter] == currrow) return true;
        }
        return false;
    }

    public RequestFilter getRequestFilter() {
        if(selection != null){
            return selection.getRequestFilter();
        }else{
            return null;
        }
    }

    public void addEventPlotterInfo(EventAccessOperations[] eventAccessArray) {
        for(int counter = 0; counter < eventAccessArray.length; counter++) {
            eventPlotterList.add(new EventFlagPlotter(this, eventAccess[counter]));
        }
    }

    private ColorFactory colorFactory;

    private PlottableSelection selection;

    private LinkedList eventPlotterList;

    /** Solely for use to d3etermine if drawing thread is still current. */
    public Graphics2D getCurrentImageGraphics(){ return currentImageGraphics; }

    private Graphics2D currentImageGraphics = null;

    private EventAccessOperations[] eventAccess = new EventAccess[0];

    protected JLabel imagePanel = new JLabel("no image");

    private edu.iris.Fissures.Plottable[] arrayplottable = new edu.iris.Fissures.Plottable[0] ;
    private String  plottablename="On the menu above, choose a SCEPP station from the SC icon and then click 'load data'.";
    private Image image = null;
    private Shape plottableShape = null;

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

    public ChannelId channelId;
    public int min;
    public int max;
    public int mean;
    float ampScale = 1.0f;
    float ampScalePercent = 1.0f;
    public static final int TITLE_Y_SHIFT = 40;
    public static final int LABEL_X_SHIFT = 50;

    int beginx = -1;
    int beginy = -1;

    int endx = -1;
    int endy  = -1;

    public Date getDate(){return date;}

    private Date date;
}/*close class*/
