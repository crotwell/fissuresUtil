package edu.sc.seis.fissuresUtil.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;


/**
 * DrawableSeismogram.java
 *
 */



public class DrawableSeismogram implements NamedPlotter{
    public DrawableSeismogram(DataSetSeismogram seis){
        this(seis, Color.blue, seis.toString());
    }
    
    public DrawableSeismogram(DataSetSeismogram seis, Color color){
        this(seis, color, seis.toString());
    }
    
    public DrawableSeismogram(DataSetSeismogram seis, Color color, String name){
        this.color = color;
        this.name = name;
        shape = new SeismogramShape(seis);
    }
    
    public DrawableSeismogram(SeismogramShape shape, Color color, String name){
        this.color = color;
        this.name = name;
        this.shape = shape;
    }
    
    public void setVisibility(boolean b){
        visible = b;
    }
    
    public boolean getVisiblity(){ return visible; }
    
    public void toggleVisibility(){
    }
    
    public void draw(Graphics2D canvas,
                     Dimension size,
                     TimeEvent currentTime,
                     AmpEvent currentAmp){
        if(visible && size.width > 0 && size.height > 0){
            canvas.setPaint(color);
            shape.update(currentTime.getTime(shape.getSeismogram()),
                         currentAmp.getAmp(shape.getSeismogram()),
                         size);
            canvas.draw(shape);
        }
    }
    
    public boolean drawName(Graphics2D canvas, int xPosition, int yPosition){
        if(visible){
            canvas.setPaint(color);
            canvas.drawString(name, xPosition, yPosition);
            return true;
        }
        return false;
    }
    
    public String getName(){ return name; }
    
    public String toString(){ return name; }
    
    public DataSetSeismogram getSeismogram(){ return shape.getSeismogram(); }
    
    private Color color;
    
    private String name;
    
    protected SeismogramShape shape;
    
    private boolean visible = true;
    
}

