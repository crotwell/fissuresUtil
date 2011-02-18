package edu.sc.seis.fissuresUtil.display;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

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
    }
    
    public int print(Graphics g, PageFormat pageFormat, int pageIndex){
	if (pageIndex > 0) return NO_SUCH_PAGE;
	pageFormat.setOrientation(PageFormat.LANDSCAPE);
	Graphics2D g2 = (Graphics2D)g;
	g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	boolean wasBuffered = disableDoubleBuffering(mComponent);
	mComponent.paint(g2);
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
