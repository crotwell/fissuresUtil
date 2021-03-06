package edu.sc.seis.fissuresUtil.display;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;
import edu.sc.seis.fissuresUtil.display.borders.TitleBorder;
import edu.sc.seis.fissuresUtil.display.drawable.EventBoxes;
import edu.sc.seis.fissuresUtil.display.drawable.EventFlag;
import edu.sc.seis.fissuresUtil.display.drawable.PlottableSelection;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * PlottableDisplay.java Created: Wed Jul 18 11:08:24 2001
 * 
 * @author Srinivasa Telukutla Modified: Georgina Coleman
 * @version
 */
public class PlottableDisplay extends JComponent implements Graphics2DRenderer {

    public PlottableDisplay() {
        this(TOTAL_WIDTH);
    }

    public PlottableDisplay(int pixelsPerDay) {
        this(pixelsPerDay, true);
    }

    public PlottableDisplay(int pixelsPerDay, boolean includeText) {
        super();
        totalWidth = pixelsPerDay;
        rowWidth = totalWidth / rows;
        removeAll();
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                                                     BorderFactory.createLoweredBevelBorder()));
        setLayout(new BorderLayout());
        tempSelection = new PlottableSelection(this);
        selection = new PlottableSelection(this);
        configChanged();
        this.includeText = includeText;
        if(!includeText) {
            titleHeight = 30;
            configChanged();
        }
    }

    public void displayEventsAsBoxes() {
        useFlags = false;
    }

    public void displayEventsAsFlags() {
        useFlags = true;
    }

    public void setColors(Color even, Color odd, Color axis, Color background) {
        evenColor = even;
        oddColor = odd;
        axisColor = axis;
        backgroundColor = background;
    }

    public void setOffset(int offset) {
        if(rowOffset != offset) {
            rowOffset = offset;
            configChanged();
        }
    }

    public void setAmpScale(float ampScalePercent) {
        if(this.ampScalePercent != ampScalePercent) {
            this.ampScalePercent = ampScalePercent;
            configChanged();
        }
    }

    void configChanged() {
        currentImageGraphics = null;
        synchronized(this) {
            image = null;
        }
        int newWidth = totalWidth / rows + 2 * LABEL_X_SHIFT;
        int newHeight = rowOffset * rows + titleHeight;
        if(newHeight == getPreferredSize().height
                && newWidth == getPreferredSize().width) {
            repaint();
        } else {
            setPreferredSize(new Dimension(newWidth, newHeight));
            revalidate();
        }
    }

    public void setPlottable(Plottable[] clientPlott,
                             String nameofstation,
                             String orientationName,
                             Date date,
                             ChannelId channelId) {
        removeAll();
        this.arrayplottable = clientPlott;
        int[] minmax = findMinMax(arrayplottable);
        ampScale = rowOffset * (1f / (minmax[1] - minmax[0]));
        if(arrayplottable == null) {
            logger.warn("setPlottable:Plottable is NULL.");
            this.arrayplottable = new Plottable[0];
        }
        stationName = nameofstation;
        this.orientationName = orientationName;
        dateName = dateFormater.format(date);
        if(!date.equals(this.date)) {
            selection = new PlottableSelection(this);
        }
        this.date = date;
        calendar.setTime(date);
        this.channelId = channelId;
        plottableShape = makeShape(clientPlott);
        configChanged();
    }

    public void setPlottable(Plottable[] clientPlott,
                             String nameofstation,
                             String orientationName,
                             Date date,
                             ChannelId channelId,
                             EventAccessOperations[] events,
                             Arrival[][] arrivals) {
        eventPlotterList = new LinkedList();
        addEventPlotterInfo(events, arrivals);
        setPlottable(clientPlott,
                     nameofstation,
                     orientationName,
                     date,
                     channelId);
    }

    public void paintComponent(Graphics g) {
        synchronized(this) {
            if(image == null) {
                image = createImage();
            } else {
                g.drawImage(image, 0, 0, this);
            }
        }
        drawEventFlags(g);
        drawSelection(g);
    }

    protected void drawComponent(Graphics g) {
        drawTitle(g);
        if(date != null) {
            drawTimeTicks(g, date);
        }
        if(arrayplottable == null) {
            logger.warn("Plottable is NULL.");
            return;
        }
        drawPlottableNew(g);
    }

    int[] drawTitle(int y, String title, String text, Graphics2D g2) {
        return drawTitle(y, title, text, g2, oddColor);
    }

    int[] drawTitle(int y, String title, String text, Graphics2D g2, Color color) {
        return drawTitle(y, LABEL_X_SHIFT, title, text, g2, color);
    }

    int[] drawTitle(int y,
                    int x,
                    String title,
                    String text,
                    Graphics2D g2,
                    Color color) {
        g2.setPaint(color);
        g2.setFont(DisplayUtils.BOLD_FONT);
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D stringBounds = fm.getStringBounds(title, g2);
        int titleWidth = (int)stringBounds.getWidth();
        int titleHeight = (int)stringBounds.getHeight();
        g2.drawString(title, x, y);
        g2.setFont(DisplayUtils.DEFAULT_FONT);
        g2.drawString(text, x + titleWidth, y);
        stringBounds = fm.getStringBounds(text, g2);
        int[] heightWidth = {titleHeight,
                             titleWidth + (int)stringBounds.getWidth()};
        return heightWidth;
    }

    void drawTitle(Graphics g) {
        if(includeText) {
            Graphics2D g2 = (Graphics2D)g;
            int titleYPos = 20;
            if(stationName.length() > 0) {
                titleYPos += drawTitle(titleYPos, "Station: ", stationName, g2)[0];
            }
            if(dateName.length() > 0) {
                titleYPos += drawTitle(titleYPos, "Date: ", dateName, g2)[0];
            }
            if(orientationName.length() > 0) {
                titleYPos += drawTitle(titleYPos,
                                       "Orientation: ",
                                       orientationName,
                                       g2)[0];
            }
            Iterator it = eventPlotterList.iterator();
            while(it.hasNext()) {
                EventFlag cur = (EventFlag)it.next();
                int[] size = drawTitle(titleYPos,
                                       LABEL_X_SHIFT,
                                       "Event: ",
                                       cur.getTitle(),
                                       g2,
                                       cur.getColor());
                cur.setTitleLoc(LABEL_X_SHIFT,
                                titleYPos - size[0],
                                size[1],
                                size[0]);
                titleYPos += size[0];
            }
            String myt = "Time";
            String mygmt = "GMT";
            g2.setPaint(evenColor);
            if(titleYPos < 30)
                titleYPos = 40;
            g2.drawString(myt, 10, titleYPos - 10);
            g2.drawString(myt, rowWidth + LABEL_X_SHIFT, titleYPos - 10);
            g2.drawString(mygmt, 10, titleYPos - 20);
            g2.drawString(mygmt, rowWidth + LABEL_X_SHIFT, titleYPos - 20);
            if(titleHeight != titleYPos + rowOffset) {
                titleHeight = titleYPos + rowOffset;
                configChanged();
            }
            return;
        }
    }

    void drawTimeTicks(Graphics g, Date date) {
        Graphics2D g2 = (Graphics2D)g;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String minutes = ":" + formatMinutes(date.getMinutes()) + " ";
        int hourinterval = totalHours / rows;
        int houroffset;
        int xShift = rowWidth + LABEL_X_SHIFT;
        for(int currRow = 0; currRow < rows; currRow++) {
            houroffset = calcHourOffset(hour);
            if(currRow % 2 == 0) {
                g2.setPaint(evenColor);
            } else {
                g2.setPaint(oddColor);
            }
            g2.drawString(hour + minutes, houroffset, titleHeight + rowOffset
                    * currRow);
            hour = (hour + hourinterval) % 24;
            houroffset = calcHourOffset(hour);
            g2.drawString(hour + minutes, xShift + houroffset, titleHeight
                    + rowOffset * currRow);
        }
    }

    private static int calcHourOffset(int hour) {
        if(hour >= 10) {
            return 5;
        }
        return 10;
    }

    private static String formatMinutes(int minutes) {
        if(minutes < 10) {
            return "0" + minutes;
        }
        return "" + minutes;
    }

    private void drawPlottableNew(Graphics g) {
        int mean = getMean();
        // get new graphics to avoid messing up original
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setClip(LABEL_X_SHIFT, titleHeight-rowOffset, rowWidth, rowOffset * (rows + 1));
        int xShift = LABEL_X_SHIFT;
        int yShift = titleHeight;
        AffineTransform originalTransform = AffineTransform.getTranslateInstance(xShift,
                                                                                 yShift);
        if(PDF) {
            // we need to add to the transform from the pdf graphic rather than
            // making a new one
            originalTransform = g2.getTransform();
            originalTransform.translate(xShift, yShift);
        }
        for(int row = 0; row < rows && currentImageGraphics == g; row++) {
            // use title and label transform to draw red center lines
            g2.setTransform(originalTransform);
            g2.setPaint(axisColor);
            int yLoc = rowOffset * row;
            g2.drawLine(0, yLoc, rowWidth, yLoc);
            // shift the shape left to get to the correct point for this row
            // and down to get to the correct draw height
            g2.translate(-1 * rowWidth * row, yLoc);
            // scale the width to fit page if PDF
            if(PDF) {
                g2.scale(rowScale, 1);
            }
            // flip the y axis to make going lower positive
            g2.scale(1, -1);
            // scale for the amplitude slider
            g2.scale(1, ampScale);
            g2.scale(1, ampScalePercent);
            // center the mean
            g2.translate(0, -1 * mean);
            if(row % 2 == 0) {
                g2.setPaint(evenColor);
            } else {
                g2.setPaint(oddColor);
            }
            if(plottableShape != null) {
                if(PDF) {
                    g2.fill((new BasicStroke(1f)).createStrokedShape(plottableShape));
                } else {
                    g2.draw(plottableShape);
                }
            }
        }
        repaint();
    }

    private Shape makeShape(Plottable[] plot) {
        final int SHAPESIZE = 100;
        GeneralPath wholeShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        for(int a = 0; a < plot.length; a++) {
            if(plot[a].x_coor.length >= 2) {
                GeneralPath currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                                                           SHAPESIZE + 1);
                currentShape.moveTo(plot[a].x_coor[0], plot[a].y_coor[0]);
                for(int i = 1; i < plot[a].x_coor.length; i++) {
                    // split into smaller shapes
                    if(i % SHAPESIZE == 0) {
                        // duplicate last point
                        if(plot[a].x_coor[i - 1] == plot[a].x_coor[i] - 1) {
                            currentShape.moveTo(plot[a].x_coor[i],
                                                plot[a].y_coor[i]);
                        } else {
                            currentShape.lineTo(plot[a].x_coor[i],
                                                plot[a].y_coor[i]);
                        }
                        wholeShape.append(currentShape, false);
                        currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                                                       SHAPESIZE + 1);
                        currentShape.moveTo(plot[a].x_coor[i],
                                            plot[a].y_coor[i]);
                    }
                    if(plot[a].x_coor[i - 1] == plot[a].x_coor[i] - 1) {
                        currentShape.moveTo(plot[a].x_coor[i],
                                            plot[a].y_coor[i]);
                    } else {
                        currentShape.lineTo(plot[a].x_coor[i],
                                            plot[a].y_coor[i]);
                    }
                }
                wholeShape.append(currentShape, false);
            } else if(plot[a].x_coor.length == 1) {
                GeneralPath currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                                                           2);
                currentShape.moveTo(plot[a].x_coor[0], plot[a].y_coor[0]);
                currentShape.lineTo(plot[a].x_coor[0], plot[a].y_coor[0]);
                wholeShape.append(currentShape, false);
            }
        }
        return wholeShape;
    }

    public int getMean() {
        if(arrayplottable == null || arrayplottable.length == 0
                || arrayplottable[0].y_coor.length == 0) {
            return 0;
        }
        long mean = arrayplottable[0].y_coor[0];
        int numPoints = 0;
        for(int i = 0; i < arrayplottable.length; i++) {
            for(int j = 0; j < arrayplottable[i].y_coor.length; j++) {
                mean += arrayplottable[i].y_coor[j];
            }
            numPoints += arrayplottable[i].y_coor.length;
        }
        mean = mean / numPoints;
        return (int)mean;
    }

    public Image createImage() {
        final int width = getSize().width;
        final int height = getSize().height;
        final Image offImg = new BufferedImage(width,
                                               height,
                                               BufferedImage.TYPE_INT_RGB);
        Thread t = new Thread("Plottable Image Creator") {

            public void run() {
                try {
                    Graphics2D g = (Graphics2D)offImg.getGraphics();
                    currentImageGraphics = g;
                    g.setBackground(backgroundColor);
                    // clear canvas
                    g.clearRect(0, 0, width, height);
                    drawComponent(g);
                    g.dispose();
                    repaint();
                } catch(Throwable t) {
                    GlobalExceptionHandler.handle("Problem drawing the plottable image.",
                                                  t);
                }
            }
        };
        t.start();
        return offImg;
    }

    public void renderToGraphics(Graphics2D g) {
        renderToGraphics(g, getPreferredSize());
    }

    public void renderToGraphics(Graphics2D g, Dimension size) {
        if(getRootPane() == null) {
            addNotify();
        }
        Graphics curGraphics = currentImageGraphics;
        currentImageGraphics = g;
        Dimension curSize = getSize();
        setSize(size);
        validate();
        setDoubleBuffered(false);
        g.setBackground(backgroundColor);
        g.clearRect(0, 0, size.width, size.height);
        drawComponent(g);
        drawEventFlags(g);
        drawSelection(g);
        repaint();
        setDoubleBuffered(true);
        currentImageGraphics = curGraphics;
        setSize(curSize);
        validate();
    }

    public void prepForOutput() {
        configChanged();
        if(getRootPane() == null) {
            addNotify();
            repaint();
        }
    }

    public void outputToPNG(String filename) throws IOException {
        outputToPNG(new File(filename));
    }

    public void outputToPNG(File file) throws IOException {
        file.getCanonicalFile().getParentFile().mkdirs();
        File temp = File.createTempFile(file.getName(),
                                        null,
                                        file.getParentFile());
        outputToPNG(new FileOutputStream(temp));
        file.delete();
        temp.renameTo(file);
    }

    public void outputToPNG(OutputStream out) throws IOException {
        prepForOutput();
        BufferedImage img = new BufferedImage(getPreferredSize().width,
                                              getPreferredSize().height,
                                              BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        renderToGraphics(g2d, getSize());
        ImageIO.write(img, "png", out);
    }

    public void outputToPDF(String fileName) throws IOException {
        outputToPDF(new File(fileName));
    }

    public void outputToPDF(File file) throws IOException {
        file.getCanonicalFile().getParentFile().mkdirs();
        File temp = File.createTempFile(file.getName(),
                                        null,
                                        file.getParentFile());
        outputToPDF(new FileOutputStream(temp));
        file.delete();
        temp.renameTo(file);
    }

    public void outputToPDF(OutputStream out) {
        outputToPDF(out, null);
    }

    public void outputToPDF(OutputStream out, TitleBorder header) {
        prepForOutput();
        PDF = true;
        SeismogramPDFBuilder builder = new SeismogramPDFBuilder();
        int oldRowWidth = rowWidth;
        int oldRowOffset = rowOffset;
        rowScale = (builder.getPrintableSize().getWidth() - (2.0 * LABEL_X_SHIFT))
                / oldRowWidth;
        rowWidth = (int)(oldRowWidth * rowScale);
        int headerHeight = 0;
        if(header != null) {
            header.setSize(header.getPreferredSize());
            headerHeight = header.getHeight();
            builder.setHeader(header);
        }
        rowOffset = (int)((builder.getPrintableSize().getHeight() - headerHeight) / rows);
        builder.createPDF(this, out);
        rowWidth = oldRowWidth;
        rowOffset = oldRowOffset;
        PDF = false;
    }

    public int[] findMinMax(Plottable[] arrayplottable) {
        int totLen = 0;
        for(int arrayi = 0; arrayi < arrayplottable.length; arrayi++) {
            totLen += arrayplottable[arrayi].y_coor.length;
        }
        if(totLen == 0) {
            int[] minandmax = new int[2];
            minandmax[0] = -1;
            minandmax[1] = 1;
            return minandmax;
        } // end of if (arrayplottable.length == 0)
        
        int[] allY = new int[totLen];
        int allYIndex = 0;
        for(int arrayi = 0; arrayi < arrayplottable.length; arrayi++) {
            System.arraycopy(arrayplottable[arrayi].y_coor, 0, allY, allYIndex, arrayplottable[arrayi].y_coor.length);
            allYIndex += arrayplottable[arrayi].y_coor.length;
        }
        Statistics stat = new Statistics(allY);
        double stddev = stat.stddev();
        double mean = stat.mean();

        int[] minandmax = {(int)Math.round(mean-2*stddev), (int)Math.round(mean+2*stddev)};
        
        return minandmax;
    }

    public void addToSelection(int x, int y) {
        selection.addXY(x, y);
        repaint();
    }

    public void setSelection(int x, int y) {
        setSelection(x, y, 10);
    }

    public void setSelection(int x, int y, int width) {
        selection.setXY(x, y, width);
        repaint();
    }

    public boolean bordersSelection(int x, int y) {
        if(selection.borders(x, y)) {
            return true;
        }
        return false;
    }

    private void drawSelection(Graphics g) {
        selection.draw(g);
        tempSelection.draw(g);
    }

    public void removeSelection() {
        selection = new PlottableSelection(this);
        repaint();
    }

    private void drawEventFlags(Graphics g) {
        Iterator iterator = eventPlotterList.iterator();
        while(iterator.hasNext()) {
            EventFlag plotter = (EventFlag)iterator.next();
            // plotter.setAlpha(127);
            plotter.draw(g);
        }
    }

    public PlottableSelection getTempSelection() {
        return tempSelection;
    }

    public void placeTempSelection() {
        selection = tempSelection;
        tempSelection = new PlottableSelection(this);
        selection.setPlaced(true);
        repaint();
    }

    public LinkedList getEvents() {
        return eventPlotterList;
    }

    public RequestFilter getRequestFilter(int x, int y) {
        if(selection.intersectsExtract(x, y))
            return selection.getRequestFilter();
        return null;
    }

    public List getEventPlotters() {
        return eventPlotterList;
    }

    public EventAccessOperations[] getSelectedEvents() {
        int[][] selectedArea = selection.getSelectedArea();
        Iterator it = eventPlotterList.iterator();
        java.util.List selectedEvents = new ArrayList();
        while(it.hasNext()) {
            EventFlag cur = (EventFlag)it.next();
            if(cur.isSelected(selectedArea)) {
                selectedEvents.add(cur.getEvent());
            }
        }
        EventAccessOperations[] eventArray = new EventAccessOperations[selectedEvents.size()];
        return (EventAccessOperations[])selectedEvents.toArray(eventArray);
    }

    private void addEventPlotterInfo(EventAccessOperations[] eventAccessArray,
                                     Arrival[][] arrivals) {
        for(int i = 0; i < eventAccessArray.length; i++) {
            if(useFlags) {
                eventPlotterList.add(new EventFlag(this,
                                                   eventAccessArray[i],
                                                   arrivals[i]));
            } else {
                eventPlotterList.add(new EventBoxes(this,
                                                    eventAccessArray[i],
                                                    arrivals[i]));
            }
        }
    }

    public Date getDate() {
        return date;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    /* Defaults for plottable */
    public static final int ROWS = 12;

    public static final int TOTAL_WIDTH = 6000;

    public static final int OFFSET = 30;

    public int titleHeight = 140;

    public static final int LABEL_X_SHIFT = 50;

    // Plottable instance values
    public int getRows() {
        return rows;
    }

    private int rows = ROWS;

    private int totalWidth = TOTAL_WIDTH;

    public int getRowWidth() {
        return rowWidth;
    }

    private int rowWidth;

    public int getRowOffset() {
        return rowOffset;
    }

    private int rowOffset = OFFSET;

    public int getPlotWidth() {
        return totalWidth;
    }

    public int getTotalHours() {
        return totalHours;
    }

    private boolean useFlags = true;

    private Color backgroundColor = Color.WHITE;

    private Color evenColor = Color.BLACK;

    private Color oddColor = Color.BLUE;

    private Color axisColor = Color.RED;

    private int totalHours = 24;

    private ChannelId channelId;

    private float ampScale = 1.0f;

    private float ampScalePercent = 1.0f;

    private Date date;

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private PlottableSelection selection, tempSelection;

    private LinkedList eventPlotterList = new LinkedList();

    private Plottable[] arrayplottable = new Plottable[0];

    private String stationName = "";

    private String orientationName = "";

    private String dateName = "";

    private Image image = null;

    private Shape plottableShape = null;

    private Graphics currentImageGraphics = null;

    private boolean includeText;

    private boolean PDF = false;

    private double rowScale;

    private static ThreadSafeSimpleDateFormat dateFormater = new ThreadSafeSimpleDateFormat("EEEE, d MMMM yyyy", TimeZone.getTimeZone("GMT"));

    private static Logger logger = LoggerFactory.getLogger(PlottableDisplay.class);
}
