package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.print.*;
import javax.swing.JPanel;
import javax.swing.JComponent;

/**
 * SeismogramPrinter.java
 *
 *
 * Created: Sun Oct 13 17:06:40 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class SeismogramPrinter implements Printable{
    public SeismogramPrinter (BasicSeismogramDisplay[] displays, int seisPerPage){
	this.displays = displays;
	System.out.println("number of displays: " + displays.length + " number of displays per page: " + seisPerPage);
	this.seisPerPage = seisPerPage;
    }
    
    public int print(Graphics g, PageFormat pageFormat, int pageIndex){
	System.out.println(pageIndex);
	if (pageIndex >= displays.length/seisPerPage) return NO_SUCH_PAGE;
	Graphics2D g2 = (Graphics2D)g;
	g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	Paper page = pageFormat.getPaper();
	double height = page.getHeight();
	int displayStart = pageIndex * seisPerPage;
	boolean[] wasBuffered = new boolean[seisPerPage];
	Dimension[] currentSizes = new Dimension[seisPerPage];
	for(int i = 0; i < seisPerPage; i++){
	    System.out.println("adding display " + displays[i + displayStart].getName() + " to the printout");
	    wasBuffered[i] = disableDoubleBuffering(displays[i + displayStart]);
	    currentSizes[i] = displays[i + displayStart].getSize();
	    displays[i+displayStart].setSize(new Dimension((int)page.getWidth(),(int)(height/seisPerPage)));
	    displays[i+displayStart].paint(g2);
	    g2.translate(0, (int)(height/seisPerPage));
	}
	for(int i = 0; i < wasBuffered.length; i++){
	    restoreDoubleBuffering(displays[i+ displayStart], wasBuffered[i]);
	    displays[i+displayStart].setSize(currentSizes[i]);
	}
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

    private BasicSeismogramDisplay[] displays;

    private int seisPerPage;
    
}// SeismogramPrinter
