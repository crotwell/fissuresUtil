package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.print.*;
import javax.swing.JPanel;
import javax.swing.JComponent;
import java.util.HashMap;

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

    public SeismogramPrinter(BasicSeismogramDisplay[] displays){
	this.displays = displays;
	sizes = new Dimension[displays.length];
	bottomBorder = new boolean[displays.length];
	topBorder = new boolean[displays.length];
	for(int i = 0; i < displays.length; i++){
	    disableDoubleBuffering(displays[i]);
	    sizes[i] = displays[i].getSize();
	    bottomBorder[i] = displays[i].hasBottomTimeBorder();
	    topBorder[i] = displays[i].hasTopTimeBorder();
	    displays[i].addBottomTimeBorder();
	    displays[i].addTopTimeBorder();
	    
	}
    }
    
    public void restore(){
	for(int i = 0; i < displays.length; i++){
	    restoreDoubleBuffering(displays[i]);
	    if(!bottomBorder[i]){
		displays[i].removeBottomTimeBorder();
	    }
	    if(!topBorder[i]){
		displays[i].removeTopTimeBorder();
	    }
	    displays[i].setSize(sizes[i]);
	    displays[i].resize();
	}
    }
    
    public int print(Graphics g, PageFormat pageFormat, int pageIndex){
	if(pageIndex >= displays.length)  return NO_SUCH_PAGE;
	Graphics2D g2 = (Graphics2D)g;
	g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	Dimension imageableSize = new Dimension();
	imageableSize.setSize(pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
	displays[pageIndex].setSize(imageableSize);
	displays[pageIndex].resize();
	displays[pageIndex].paint(g2);
	return PAGE_EXISTS;
    }
    
    private void disableDoubleBuffering(JComponent jc){
	jc.setDoubleBuffered(false);
    }
    
    private void restoreDoubleBuffering(JComponent jc){
	jc.setDoubleBuffered(true);
    }

    private Dimension[] sizes;

    private boolean[] topBorder;

    private boolean[] bottomBorder;
 
    private BasicSeismogramDisplay[] displays;
 
}// SeismogramPrinter
