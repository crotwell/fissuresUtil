package edu.sc.seis.fissuresUtil.display.drawable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.NamedColor;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplayListener;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class DrawableSeismogram implements NamedDrawable, SeismogramDisplayListener {
    public DrawableSeismogram(SeismogramDisplay parent, DataSetSeismogram seis, Color color) {
        this(parent, new SeismogramShape(parent, seis), seis.getName(), color);
    }

    public DrawableSeismogram(SeismogramDisplay parent,
                              DataSetSeismogram seis,
                              String name) {
        this(parent, new SeismogramShape(parent, seis), name, null);
    }

    protected DrawableSeismogram(SeismogramDisplay parent, SeismogramShape shape) {
        this(parent, shape, shape.getSeismogram().getName(), null);
    }

    protected DrawableSeismogram(SeismogramDisplay parent,
                                 SeismogramShape shape,
                                 String name, Color color) {
        this.parent = parent;
        if(color != null) {
            this.color = color;
        }
        else {
            this.color = parent.getNextColor(DrawableSeismogram.class);
        }
        this.name = name;
        if(color instanceof NamedColor) {
            this.name += "." + color;
            this.color = ((NamedColor)color).getColor();
        }
        this.shape = shape;
        setRemover(new SeismogramRemover(shape.getSeismogram(), parent));
        setVisibility(defaultVisibility);
        parent.add((SeismogramDisplayListener)this);
    }

    protected void setRemover(SeismogramRemover remover) {
        this.remover = remover;
    }

    public SeismogramDisplay getParent() {
        return parent;
    }

    public void setVisibility(boolean vis) {
        DataSetSeismogram[] seis = { getSeismogram() };
        if(vis) {
            parent.getTimeConfig().add(seis);
            parent.getAmpConfig().add(seis);
        }
        else {
            parent.getTimeConfig().remove(seis);
            parent.getAmpConfig().remove(seis);
        }
        if(visible != vis) {
            parent.repaint();
        }
        visible = vis;
    }

    public boolean getVisiblity(){ return visible; }

    public Color getColor(){ return color; }

    public void draw(Graphics2D canvas,
                     Dimension size,
                     TimeEvent currentTime,
                     AmpEvent currentAmp) {
        if(visible && size.width > 0 && size.height > 0) {
            if(!currentTime.contains(getSeismogram())) {
                DataSetSeismogram[] seis = { getSeismogram() };
                parent.getTimeConfig().add(seis);
            }
            else if(!currentAmp.contains(getSeismogram())) {
                DataSetSeismogram[] seis = { getSeismogram() };
                parent.getAmpConfig().add(seis);
            }
            else if(shape.update(currentTime.getTime(getSeismogram()),
                                 currentAmp.getAmp(getSeismogram()),
                                 size)) {
                canvas.setPaint(color);
                canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
                canvas.draw(shape);
                //by repainting after the first paint the borders always get drawn
                //with amp information.  sometimes, they receive info after the
                //trace first requests it
                if(firstPaint) {
                    parent.repaint();
                    firstPaint = false;
                }
            }
        }
        synchronized(children){
            Iterator it = children.iterator();
            while(it.hasNext()) {
                Drawable cur = (Drawable)it.next();
                cur.draw(canvas, size, currentTime, currentAmp);
            }
        }
    }

    //TODO remove this firstPaint crap and get borders to draw themselves correctly

    private boolean firstPaint = true;

    public Rectangle2D drawName(Graphics2D canvas, int xPosition, int yPosition) {
        remover.draw(canvas, xPosition, yPosition - 7);
        xPosition += 10;
        String name = getName() + " " + shape.getDataStatus();
        FontMetrics fm = canvas.getFontMetrics();
        Rectangle2D stringBounds = fm.getStringBounds(name, canvas);
        stringBounds.setRect(stringBounds.getX() + xPosition,
                             stringBounds.getY() + yPosition,
                             stringBounds.getWidth(),
                             stringBounds.getHeight());
        canvas.setColor(color);
        canvas.setFont(DisplayUtils.DEFAULT_FONT);
        if(!visible) {
            canvas.setPaint(Color.GRAY);
        }
        if(remover.getDrawColor() == Color.RED) {
            canvas.setFont(DisplayUtils.BOLD_FONT);
            canvas.setColor(Color.RED);
        }
        canvas.drawString(name, xPosition, yPosition);
        canvas.setFont(DisplayUtils.DEFAULT_FONT);
        synchronized(children){
            Iterator it = children.iterator();
            while(it.hasNext()) {
                Drawable cur = (Drawable)it.next();
                if(cur instanceof NamedDrawable) {
                    stringBounds.add(((NamedDrawable)cur).drawName(canvas, (int)(xPosition + stringBounds.getWidth()), yPosition));
                }
            }
        }
        return stringBounds;
    }

    public void add(Drawable child) {
        if(child instanceof Event) {
            ((Event)child).setColor(color);
        }
        children.add(child);
        parent.repaint();
    }

    public void add(Drawable child, Color color) {
        add(child);
        if(child instanceof Event) {
            ((Event)child).setColor(color);
        }
    }

    public void remove(Drawable child) {
        if(children.remove(child)) {
            parent.repaint();
        }
    }

    public void clear(Class drawableClass) {
        synchronized(children){
            Iterator it = children.iterator();
            while(it.hasNext()) {
                if(drawableClass.isInstance(it.next())) {
                    it.remove();
                }
            }
        }
        parent.repaint();
    }

    public DrawableIterator iterator(Class drawableClass) {
        return new DrawableIterator(drawableClass, children);
    }

    public void added(SeismogramDisplay recipient, Drawable drawable) {
        // TODO
    }

    public void removed(SeismogramDisplay bereaved, Drawable drawable) {
        // TODO
    }

    /**
     *called when the display <code>from</code> is being replaced by <code>to</code>
     */
    public void switching(SeismogramDisplay from, SeismogramDisplay to) {
        this.parent = to;
        setVisibility(visible);
    }

    public void switching(AmpConfig from, AmpConfig to) {
        // TODO
    }

    public void switching(TimeConfig from, TimeConfig to) {
        // TODO
    }


    public String getName(){ return name; }

    public String toString(){ return getName();}

    public DataSetSeismogram getSeismogram(){ return shape.getSeismogram(); }

    public String getDataStatus(){ return shape.getDataStatus();}

    public void getData(){ shape.getData(); }

    public static void setDefaultVisibility(boolean visible) {
        defaultVisibility = visible;
    }

    private SeismogramDisplay parent;

    private List children = Collections.synchronizedList(new ArrayList());

    private Color color;

    private String name;

    protected SeismogramShape shape;

    private static boolean defaultVisibility = true;

    private boolean visible;

    private SeismogramRemover remover;
}
