package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * DrawableSeismogram.java
 *
 */



public class DrawableSeismogram implements NamedDrawable{
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
        this(parent, new SeismogramShape(parent, seis), color, name,
             new SeismogramRemover(seis, parent));
    }

    protected DrawableSeismogram(SeismogramDisplay parent,
                                 SeismogramShape shape,
                                 Color color,
                                 String name,
                                 SeismogramRemover remover){
        this.parent = parent;
        this.color = color;
        this.name = name;
        this.shape = shape;
        this.remover = remover;
    }

    public SeismogramDisplay getParent() {
        return parent;
    }

    public void setVisibility(boolean b){
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
            if(shape.update(currentTime.getTime(shape.getSeismogram()),
                            currentAmp.getAmp(shape.getSeismogram()),
                            size)){
                canvas.setPaint(color);
                canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
                canvas.draw(shape);
            }
        }
    }

    public boolean drawName(Graphics2D canvas, int xPosition, int yPosition){
        if(visible){
            remover.draw(canvas, xPosition, yPosition - 7);
            canvas.setPaint(color);
            if(remover.getDrawColor() == Color.RED){
                canvas.setFont(DisplayUtils.BOLD_FONT);
                canvas.setColor(Color.RED);
            }
            canvas.drawString(getName() + " " + shape.getDataStatus(), xPosition + 10, yPosition);
            canvas.setFont(DisplayUtils.DEFAULT_FONT);
            return true;
        }
        return false;
    }

    public String getName(){ return getSeismogram().getName(); }

    public String toString(){ return getName(); }

    public DataSetSeismogram getSeismogram(){ return shape.getSeismogram(); }

    private SeismogramDisplay parent;

    private Color color;

    private String name;

    protected SeismogramShape shape;

    private boolean visible = true;

    private SeismogramRemover remover;
}

