package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.print.*;

import javax.swing.JComponent;

/**
 * ComponentPrintable.java
 *
 *
 * Created: Wed Jul  3 16:16:53 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class ComponentPrintable implements Printable{
    public ComponentPrintable (Component c){
	mComponent = c;
	System.out.println("set waiting");
	mComponent.setSize(new Dimension(648, 468));
    }
    
    public int print(Graphics g, PageFormat pageFormat, int pageIndex){
	System.out.println("PRIIIIINT");
	if (pageIndex > 0) return NO_SUCH_PAGE;
	pageFormat.setOrientation(PageFormat.LANDSCAPE);
	Graphics2D g2 = (Graphics2D)g;
	g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	boolean wasBuffered = disableDoubleBuffering(mComponent);
	Dimension currentSize = mComponent.getSize();
	System.out.println((int)pageFormat.getImageableWidth() + " " +  (int)pageFormat.getImageableHeight());
	//mComponent.setSize(new Dimension((int)pageFormat.getImageableWidth(), (int)pageFormat.getImageableHeight()));	   
	mComponent.paint(g2);
	mComponent.setSize(currentSize);
	restoreDoubleBuffering(mComponent, wasBuffered);
	return PAGE_EXISTS;
    }
    
    private boolean disableDoubleBuffering(Component c){
	if(c instanceof JComponent == false) return false;
	JComponent jc = (JComponent)c;
	boolean wasBuffered = jc.isDoubleBuffered();
	jc.setDoubleBuffered(false);
	return wasBuffered;
    }
    
    private void restoreDoubleBuffering(Component c, boolean wasBuffered){
	if(c instanceof JComponent)
	    ((JComponent)c).setDoubleBuffered(wasBuffered);
    }

    private Thread printing;

    private Component mComponent;
    
}// ComponentPrintable
