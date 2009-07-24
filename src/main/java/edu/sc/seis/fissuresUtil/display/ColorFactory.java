package edu.sc.seis.fissuresUtil.display;

import java.awt.Color;
import java.util.LinkedList;

/**
 * ColorFactory.java
 *
 *
 * Created: Fri Mar 28 12:28:21 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ColorFactory {
    public ColorFactory (){
        colors = new LinkedList();
        colors.add(Color.green);
        colors.add(Color.red);
        colors.add(Color.blue);
        colors.add(Color.lightGray);
        
    }
    
    public Color getNextColor() {
        if(counter == (colors.size() - 1)) counter = 0;
        else counter++;
        return (Color) colors.get(counter);
    }
    
    private int counter = -1;

    private LinkedList colors = new LinkedList();

}// ColorFactory
