package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplayListener;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DrawableSeismogram.java
 *
 */



public class DrawableSeismogram implements NamedDrawable, SeismogramDisplayListener{
    public DrawableSeismogram(SeismogramDisplay parent, DataSetSeismogram seis){
        this(parent, seis, Color.blue);
    }

    public DrawableSeismogram(SeismogramDisplay parent, DataSetSeismogram seis, Color color){
        this(parent, seis, color, seis.toString());
    }

    public DrawableSeismogram(SeismogramDisplay parent,
                              DataSetSeismogram seis,
                              Color color,
                              String name){
        this(parent, new SeismogramShape(parent, seis), color, name);
    }

    protected DrawableSeismogram(SeismogramDisplay parent,
                                 SeismogramShape shape,
                                 Color color,
                                 String name){
        this.parent = parent;
        this.color = color;
        this.name = name;
        this.shape = shape;
        setRemover(new SeismogramRemover(shape.getSeismogram(), parent));
        setVisibility(true);
        parent.add(this);
    }

    protected void setRemover(SeismogramRemover remover){
        this.remover = remover;
    }

    public SeismogramDisplay getParent() {
        return parent;
    }

    public void setVisibility(boolean b){
        DataSetSeismogram[] seis = { getSeismogram() };
        if(b){
            parent.getTimeConfig().add(seis);
            parent.getAmpConfig().add(seis);
        }else{
            parent.getTimeConfig().remove(seis);
            parent.getAmpConfig().remove(seis);
        }
        if(visible != b){
            parent.repaint();
        }
        visible = b;
    }

    public boolean getVisiblity(){ return visible; }

    public void toggleVisibility(){
        setVisibility(!visible);
    }

    public void draw(Graphics2D canvas,
                     Dimension size,
                     TimeEvent currentTime,
                     AmpEvent currentAmp){
        if(visible && size.width > 0 && size.height > 0){
            if(!currentTime.contains(shape.getSeismogram())){
                DataSetSeismogram[] seis = { shape.getSeismogram() };
                parent.getTimeConfig().add(seis);
            } else if(!currentAmp.contains(shape.getSeismogram())){
                DataSetSeismogram[] seis = { shape.getSeismogram() };
                parent.getAmpConfig().add(seis);
            }else if(shape.update(currentTime.getTime(shape.getSeismogram()),
                                  currentAmp.getAmp(shape.getSeismogram()),
                                  size)){
                canvas.setPaint(color);
                canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
                canvas.draw(shape);
            }
        }
        Iterator it = children.iterator();
        while(it.hasNext()){
            Drawable cur = (Drawable)it.next();
            cur.draw(canvas, size, currentTime, currentAmp);
        }
    }

    public Rectangle2D drawName(Graphics2D canvas, int xPosition, int yPosition){
        remover.draw(canvas, xPosition, yPosition - 7);
        canvas.setPaint(color);
        if(!visible){
            canvas.setPaint(Color.GRAY);
        }
        if(remover.getDrawColor() == Color.RED){
            canvas.setFont(DisplayUtils.BOLD_FONT);
            canvas.setColor(Color.RED);
        }
        xPosition += 10;
        String name = getName() + " " + shape.getDataStatus();
        canvas.drawString(name, xPosition, yPosition);
        FontMetrics fm = canvas.getFontMetrics();
        Rectangle2D stringBounds = fm.getStringBounds(name, canvas);
        stringBounds.setRect(stringBounds.getX() + xPosition,
                             stringBounds.getY() + yPosition,
                             stringBounds.getWidth(),
                             stringBounds.getHeight());
        canvas.setFont(DisplayUtils.DEFAULT_FONT);
        Iterator it = children.iterator();
        while(it.hasNext()){
            Drawable cur = (Drawable)it.next();
            if(cur instanceof NamedDrawable){
                stringBounds.add(((NamedDrawable)cur).drawName(canvas, (int)(xPosition + stringBounds.getWidth()), yPosition));
            }
        }
        return stringBounds;
    }

    public void add(Drawable child){
        children.add(child);
        parent.repaint();
    }

    public void remove(Drawable child){
        children.remove(child);
        parent.repaint();
    }

    public void clear(Class drawableClass){
        Iterator it = children.iterator();
        while(it.hasNext()){
            if(drawableClass.isInstance(it.next())){
                it.remove();
            }
        }
        parent.repaint();
    }

    public DrawableIterator iterator(Class drawableClass){
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

    public String getName(){ return getSeismogram().getName(); }

    public String toString(){ return getName(); }

    public DataSetSeismogram getSeismogram(){ return shape.getSeismogram(); }

    private SeismogramDisplay parent;

    private List children = new ArrayList();

    private Color color;

    private String name;

    protected SeismogramShape shape;

    private boolean visible = true;

    private SeismogramRemover remover;
}
