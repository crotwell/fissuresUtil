package edu.sc.seis.fissuresUtil.freq;

import java.awt.Color;

/**
 * ColoredFilter.java
 *
 *
 * Created: Mon Jul 15 10:12:01 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class ColoredFilter extends ButterworthFilter{
    public ColoredFilter (SeisGramText localeText, double lowFreqCorner, double highFreqCorner, 
			  int numPoles, int filterType, Color color){
	this(localeText, lowFreqCorner, highFreqCorner, numPoles, filterType, color, "Unnamed Filter");
    }    

    public ColoredFilter (SeisGramText localeText, double lowFreqCorner, double highFreqCorner, 
			  int numPoles, int filterType, Color color, String name){
	super(localeText, lowFreqCorner, highFreqCorner, numPoles, filterType);
	this.filterColor = color;
	this.name = name;
    }
    
    public String getName(){ return name; } 

    public Color getColor(){ return filterColor; }
    
    public void setVisibility(boolean b){ visible = b; }

    public boolean toggleVisibility(){ 
	visible = !visible;
	return visible;
    }

    public boolean getVisibility(){ return visible; }

    private boolean visible = false;

    private final Color filterColor;

    private final String name;
}// ColoredFilter
