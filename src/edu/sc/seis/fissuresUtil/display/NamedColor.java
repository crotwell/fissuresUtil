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
    public NamedColor(int r, int b, int g, int a, String name){
	super(r, b, g, a);
	this.name = name;
    }

    public String toString(){ return name; }

    public void setName(String name){ this.name = name; }

    public String getName(){ return name; }

    private String name;
    
}// NamedColor
