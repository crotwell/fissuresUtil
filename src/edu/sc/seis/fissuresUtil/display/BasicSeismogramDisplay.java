package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.drawable.*;
import edu.sc.seis.fissuresUtil.display.registrar.*;
import java.awt.*;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.borders.AmpBorder;
import edu.sc.seis.fissuresUtil.display.borders.TimeBorder;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.freq.NamedFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import org.apache.log4j.Category;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BasicSeismogramDisplay extends SeismogramDisplay implements TimeListener,
    AmpListener{

    public BasicSeismogramDisplay(VerticalSeismogramDisplay parent)throws IllegalArgumentException{
        this(new BasicTimeConfig(), new RMeanAmpConfig(), parent);
    }

    public BasicSeismogramDisplay(TimeConfig tc, VerticalSeismogramDisplay parent)
        throws IllegalArgumentException{
        this(tc, new RMeanAmpConfig(), parent);
    }
    public BasicSeismogramDisplay(AmpConfig ac, VerticalSeismogramDisplay parent)
        throws IllegalArgumentException{
        this(new BasicTimeConfig(), ac, parent);
    }

    public BasicSeismogramDisplay(TimeConfig tc, AmpConfig ac,
                                  VerticalSeismogramDisplay parent)throws IllegalArgumentException{
        this(tc, ac, parent, null);
    }

    public BasicSeismogramDisplay(TimeConfig tc, AmpConfig ac,
                                  VerticalSeismogramDisplay parent, Color borderColor)throws IllegalArgumentException{
        this.parent = parent;
        plotPainter.addMouseMotionListener(getMouseMotionForwarder());
        plotPainter.addMouseListener(getMouseForwarder());
        addBorder(new AmpBorder(this), CENTER_LEFT);
        addBorder(new TimeBorder(this), TOP_CENTER);
        add(new DisplayRemover(this));
        setTimeConfig(tc);
        setAmpConfig(ac);
    }

    public SeismogramDisplayProvider getCenterPanel() {
        if(plotPainter == null){
            plotPainter = new PlotPainter();
            plotPainter.setPreferredSize(new Dimension(PREFERRED_WIDTH,
                                                       PREFERRED_HEIGHT));
        }
        return plotPainter;
    }

    public void add(DataSetSeismogram seis, Color color) {
        // TODO
    }

    public void add(DataSetSeismogram[] seismos){
        add(seismos, null);
    }

    public void add(DataSetSeismogram[] seismos, Color color){
        for(int i = 0; i < seismos.length; i++){
            if(seismos[i] != null){
                seismograms.add(seismos[i]);
                drawables.add(new DrawableSeismogram(this, seismos[i], color));
                addDrawablesFromAuxData(seismos[i]);
            }
        }
        Iterator e = activeFilters.iterator();
        while(e.hasNext()){
            DisplayUtils.applyFilter((NamedFilter)e.next(), new DrawableIterator(DrawableSeismogram.class,
                                                                                 drawables));
        }
        seismogramArray = null;
    }

    public void remove(Drawable drawable) {
        drawables.remove(drawable);
    }

    public void add(Drawable drawable) {
        if(!drawables.contains(drawable)){
            drawables.add(drawable);
            repaint();
        }
    }

    public DrawableIterator getDrawables(MouseEvent e) {
        // TODO
        return new DrawableIterator(Drawable.class, EMPTY_LIST);
    }
    private static List EMPTY_LIST = new ArrayList();

    public DataSetSeismogram[] getSeismograms(){
        if(seismogramArray == null){
            seismogramArray = (DataSetSeismogram[])seismograms.toArray(new DataSetSeismogram[seismograms.size()]);
        }
        return seismogramArray;
    }

    public List getSeismogramList(){ return seismograms; }

    public void reset(){
        tc.reset();
        ac.reset();
    }

    public void reset(DataSetSeismogram[] seismograms){
        tc.reset(seismograms);
        ac.reset(seismograms);
    }

    public static MicroSecondDate getTime(int x, Insets insets, Dimension dim,
                                          MicroSecondTimeRange timeRange){
        int insetsX = insets.right + insets.left;
        double xPercent = (x - insets.left)/(dim.getWidth() - insetsX);
        long beginTime = timeRange.getBeginTime().getMicroSecondTime();
        double interval =  timeRange.getInterval().getValue();
        return new MicroSecondDate((long)(beginTime + (interval * xPercent)));
    }

    public VerticalSeismogramDisplay getParentDisplay(){ return parent; }

    public void updateAmp(AmpEvent event){
        currentAmpEvent = event;
        repaint();
    }

    public void setAmpConfig(AmpConfig ac){
        if(this.ac != null){
            this.ac.removeListener(this);
            tc.removeListener(this.ac);
            this.ac.remove(getSeismograms());
        }
        this.ac = ac;
        ac.addListener(this);
        tc.addListener(ac);
        ac.add(getSeismograms());
    }

    public void setGlobalizedAmpConfig(AmpConfig ac){ setAmpConfig(ac); }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        setAmpConfig(new IndividualizedAmpConfig(ac));
    }

    public AmpConfig getAmpConfig(){ return ac; }

    public void updateTime(TimeEvent event){
        currentTimeEvent = event;
        repaint();
    }

    public void setTimeConfig(TimeConfig tc){
        if(this.tc != null){
            this.tc.removeListener(this);
            this.tc.removeListener(ac);
            this.tc.add(getSeismograms());
        }
        this.tc = tc;
        tc.addListener(this);
        tc.addListener(ac);
        tc.add(getSeismograms());
    }
    public TimeConfig getTimeConfig(){ return tc; }

    public MicroSecondTimeRange getTime(){
        return currentTimeEvent.getTime();
    }

    /**
     * @returns the time for the given pixel value.
     */
    public MicroSecondDate getTime(int pixel) {
        return SimplePlotUtil.getValue(getWidth()-getInsets().left-getInsets().right,
                                       getTime().getBeginTime(),
                                       getTime().getEndTime(),
                                       pixel-getInsets().left);
    }

    /**
     * @returns the pixel for the given time.
     */
    public int getPixel(MicroSecondDate date) {
        return SimplePlotUtil.getPixel(getWidth()-getInsets().left-getInsets().right,
                                       getTime().getBeginTime(),
                                       getTime().getEndTime(),
                                       date);
    }

    public DrawableIterator iterator(Class drawableClass) {
        return  new DrawableIterator(drawableClass, drawables);
    }

    public TimeAmpLabel getTimeAmpLabel(){
        DrawableIterator pi = new DrawableIterator(TimeAmpLabel.class, drawables);
        return (TimeAmpLabel)pi.next();
    }

    public void clearSelections(){
        Iterator it = drawables.iterator();
        while(it.hasNext()){
            Drawable current = (Drawable)it.next();
            if(current instanceof Selection){
                it.remove();
            }
        }
        repaint();
    }

    public void addSelection(Selection newSelection){
        if(!drawables.contains(newSelection)){
            drawables.add(newSelection);
            repaint();
        }
    }

    public void remove(Selection old){
        if(drawables.remove(old)){
            repaint();
        }
    }

    public void print(){
        parent.print();
    }

    public boolean contains(DataSetSeismogram seismo){
        if(seismograms.contains(seismo)){
            return true;
        }
        return false;
    }

    public void clear(){
        remove();
    }

    public void remove(DataSetSeismogram[] seismos){
        for(int i = 0; i < seismos.length; i++){
            if(seismograms.contains(seismos[i])){
                seismograms.remove(seismos[i]);
                Iterator it = drawables.iterator();
                while(it.hasNext()){
                    Drawable current = (Drawable)it.next();
                    if(current instanceof DrawableSeismogram){
                        if(((DrawableSeismogram)current).getSeismogram() == seismos[i]){
                            it.remove();
                            repaint();
                        }
                    }
                }
            }
        }
        if(seismograms.size() == 0){
            clear();
        }
        tc.remove(seismos);
        ac.remove(seismos);
    }

    /** removes this Basic SeismogramDisplay from the parent. */
    public void remove(){
        parent.removeDisplay(this);
        destroy();
    }

    void destroy(){
        clearSelections();
        tc.removeListener(this);
        ac.removeListener(this);
        tc.remove(getSeismograms());
        ac.remove(getSeismograms());
    }

    public void drawSeismograms(Graphics2D g2, Dimension size){
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle2D.Float(0,0, size.width, size.height));
        g2.setFont(DisplayUtils.DEFAULT_FONT);
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D stringBounds = fm.getStringBounds("test", g2);
        Rectangle2D topLeftFilled = new Rectangle2D.Float(0,0,0,(float)stringBounds.getHeight());
        for (int i = 0; i < drawables.size(); i++){
            Drawable current = (Drawable)drawables.get(i);
            current.draw(g2, size, currentTimeEvent, currentAmpEvent);
            if(current instanceof TimeAmpLabel && !PRINTING){
                TimeAmpLabel taPlotter = (TimeAmpLabel)current;
                g2.setFont(DisplayUtils.MONOSPACED_FONT);
                FontMetrics monoMetrics = g2.getFontMetrics();
                stringBounds = monoMetrics.getStringBounds(taPlotter.getText(), g2);
                taPlotter.drawName(g2,(int)(size.width - stringBounds.getWidth()),
                                   size.height - 3);
                g2.setFont(DisplayUtils.DEFAULT_FONT);
            }else if(current instanceof NamedDrawable){
                Rectangle2D drawnSize = ((NamedDrawable)current).drawName(g2, 5, (int)topLeftFilled.getHeight());
                topLeftFilled.setRect(0,0, drawnSize.getWidth(),
                                      topLeftFilled.getHeight() +
                                          drawnSize.getHeight());
            }
        }
        if(getCurrentTimeFlag()){
            currentTimeFlag.draw(g2, size, currentTimeEvent, currentAmpEvent);
        }
    }



    private class PlotPainter extends SeismogramDisplayProvider{
        public SeismogramDisplay provide() {return BasicSeismogramDisplay.this;}

        public void paintComponent(Graphics g){
            drawSeismograms((Graphics2D)g, getSize());
        }
    }


    public void addSoundPlay(){
        try{
            //drawables.add(new SoundPlay(this, new SeismogramContainer(getSeismograms()[0])));
        }
        catch(NullPointerException e){
            GlobalExceptionHandler.handle("Sample Rate cannot be calculated, so sound is not permitted.", e);
        }
    }

    public void removeSoundPlay(){
        Iterator it = drawables.iterator();
        while(it.hasNext()){
            Drawable current = (Drawable)it.next();
            if(current instanceof SoundPlay){
                it.remove();
            }
        }
    }

    public void outputToPNG(String filename) throws IOException{
        Dimension size = getSize();
        BufferedImage bImg = new BufferedImage(size.width, size.height,
                                               BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bImg.createGraphics();
        boolean notAllHere = true;
        int wait = 0;
        while(notAllHere && wait < 10000){
            Iterator seisIt = iterator(DrawableSeismogram.class);
            while(seisIt.hasNext()){
                DrawableSeismogram cur = (DrawableSeismogram)seisIt.next();
                String status = cur.getDataStatus();
                if(status == SeismogramContainer.GETTING_DATA){
                    cur.getData();
                    try {
                        wait += 100;
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                    break;
                }
            }
            notAllHere = false;
        }
        paint(g2d);

        File loc = new File(filename);
        loc.getCanonicalFile().getParentFile().mkdirs();
        File temp = File.createTempFile(loc.getName(), null);
        ImageIO.write(bImg, "png", temp);
        loc.delete();
        temp.renameTo(loc);
    }

    public void addDrawablesFromAuxData(DataSetSeismogram seis){
        Iterator it = seis.getAuxillaryDataKeys().iterator();
        while (it.hasNext()){
            Object cur = it.next();
            if (cur.toString().startsWith(StdAuxillaryDataNames.PICK_FLAG)){
                logger.debug("aux data pick_flag: " + cur.toString());
                Element auxDatEl = (Element)seis.getAuxillaryData(cur);
                logger.debug(auxDatEl.getTagName());
                add(Flag.getFlagFromElement(auxDatEl));
            }
        }
    }

    public int countDrawables(){
        return drawables.size();
    }

    public Color getColor(){ return color; }

    public final static int PREFERRED_HEIGHT = 150;

    public final static int PREFERRED_WIDTH = 250;

    private VerticalSeismogramDisplay parent;

    private LinkedList seismograms = new LinkedList();

    private LinkedList drawables =new LinkedList();

    private TimeConfig tc;

    private AmpConfig ac;

    private PlotPainter plotPainter;

    private TimeEvent currentTimeEvent;

    private AmpEvent currentAmpEvent;

    private ScaleBorder scale;

    private DataSetSeismogram[] seismogramArray;

    private CurrentTimeFlag currentTimeFlag = new CurrentTimeFlag();

    private Color color;

    public static boolean PRINTING = false;

    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay

