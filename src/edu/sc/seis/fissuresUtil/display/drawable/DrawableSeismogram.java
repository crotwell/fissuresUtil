package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 * DrawableSeismogram.java
 *
 */



public class DrawableSeismogram implements NamedPlotter{
    public DrawableSeismogram(JComponent parent, DataSetSeismogram seis){
        this(parent, seis, Color.blue);
    }

    public DrawableSeismogram(JComponent parent, DataSetSeismogram seis, Color color){
        this(parent, seis, color, seis.toString());
    }

    public DrawableSeismogram(JComponent parent,
                              DataSetSeismogram seis,
                              Color color,
                              String name){
        this(parent, new SeismogramShape(parent, seis), color, name);
    }

    public DrawableSeismogram(JComponent parent, SeismogramShape shape, Color color, String name){
        this.parent = parent;
        this.color = color;
        this.name = name;
        this.shape = shape;
    }

    public JComponent getParent() {
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
                canvas.setStroke(new BasicStroke(1));
                canvas.draw(shape);
            }
        }
    }

    public boolean drawName(Graphics2D canvas, int xPosition, int yPosition){
        if(visible){
            canvas.setPaint(color);
            canvas.drawString(getName() + " " + shape.getDataStatus(), xPosition, yPosition);
            return true;
        }
        return false;
    }

    public String getName(){ return getSeismogram().getName(); }

    public String toString(){ return getName(); }

    public DataSetSeismogram getSeismogram(){ return shape.getSeismogram(); }

    private JComponent parent;

    private Color color;

    private String name;

    protected SeismogramShape shape;

    private boolean visible = true;
}

