package edu.sc.seis.fissuresUtil.display;

import java.awt.Color;

/**
 * NamedColor.java
 *
 *
 * Created: Fri Jul  5 16:52:03 2002
 *
 * @author <a href="mailto:groves@piglet">Charlie Groves</a>
 * @version
 */

public class NamedColor extends Color {
    public NamedColor(Color color, String name){
		this(color.getRed(), color.getBlue(), color.getGreen(), color.getAlpha(), name);
    }
	
    public NamedColor(Color color, int alpha, String name){
		this(color.getRed(), color.getBlue(), color.getGreen(), alpha, name);
    }
	
    public NamedColor(int r, int b, int g, int a, String name){
		super(r, g, b, a);
		this.name = name;
    }
	
	/**
	 * @return  the Color behind this named color
	 */
	public Color getColor() {
		return new Color(getRed(), getGreen(), getBlue(), getAlpha());
	}
	
    public String toString(){ return name; }
	
    public void setName(String name){ this.name = name; }
	
    public String getName(){ return name; }
	
    private String name;
	
}// NamedColor
