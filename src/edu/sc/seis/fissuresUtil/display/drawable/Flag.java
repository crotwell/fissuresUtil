package edu.sc.seis.fissuresUtil.display.drawable;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * FlagPlotter.java
 *
 *
 * Created: Wed Jul  3 11:50:13 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class Flag implements Drawable{
    
    public Flag(MicroSecondDate flagTime, String name) {
        this(flagTime, name, null);
    }
    
    public Flag(MicroSecondDate flagTime, String name, DrawableSeismogram seis) {
        this.flagTime = flagTime;
        this.name = name;
        this.seis = seis;
    }
    
    public void draw(Graphics2D canvas, Dimension size, TimeEvent timeEvent, AmpEvent ampEvent) {
        if(visible){
            MicroSecondTimeRange timeRange = timeEvent.getTime();
            if(seis != null) {
                if(timeEvent.contains(seis.getSeismogram())){
                    timeRange = timeEvent.getTime(seis.getSeismogram());
                }else{
                    DataSetSeismogram[] seismo = { seis.getSeismogram() };
                    seis.getParent().getTimeConfig().add(seismo);
                    seis.getParent().repaint();
                    return;
                }
            }
            if(flagTime.before(timeRange.getBeginTime()) || flagTime.after(timeRange.getEndTime()))
                return;
            canvas.setFont(DisplayUtils.BOLD_FONT);
            double offset = flagTime.difference(timeRange.getBeginTime()).getValue()/timeRange.getInterval().getValue();
            int location = (int)(offset * (double)size.width);
            Rectangle2D.Float stringBounds = new Rectangle2D.Float();
            stringBounds.setRect(canvas.getFontMetrics().getStringBounds(name, canvas));
            if(flag == null){
                synchronized(this){
                    Area pole = new Area(new Rectangle(location, 0, 1, size.height));
                    flag = new Area(new Rectangle(location, 0,
                                                      (int)(stringBounds.width + PADDING),
                                                      (int)(stringBounds.height + PADDING)));
                    flag.add(pole);
                    prevLocation = location;
                }
            }else{
                synchronized(flag){
                    double xShift = location - prevLocation;
                    flag.transform(AffineTransform.getTranslateInstance(xShift, 0));
                    prevLocation = location;
                }
            }
            canvas.setColor(color);
            canvas.fill(flag);
            canvas.setColor(Color.BLACK);
            canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            canvas.draw(flag);
            if(BasicSeismogramDisplay.PRINTING) canvas.setColor(Color.WHITE);
            canvas.drawString(name, location + PADDING/2, stringBounds.height - PADDING/2);
        }
    }
	
	public static Flag getFlagFromElement(Element el){
		String name = el.getAttribute("name");
		logger.debug("Flag name: " + name);
		logger.debug("Flag time from element: " + el.getAttribute("time"));
		MicroSecondDate time = new MicroSecondDate(new Time(el.getAttribute("time"), 0));
		logger.debug("Flag time: " + time.getFissuresTime().date_time);
		
		return new Flag(time, name);
	}
	
	public static Element createFlagElement(String name, MicroSecondDate time) throws ParserConfigurationException{
		Document doc = XMLDataSet.getDocumentBuilder().newDocument();
		Element el = doc.createElement("pickFlag");
		el.setAttribute("name", name);
		el.setAttribute("time", time.getFissuresTime().date_time);
		return el;
	}
	
    private Area flag;
    
    private int prevLocation;
    
    public Color getColor(){ return color; }
    
    public void setColor(Color color){ this.color = color; }
    
    public void setVisibility(boolean b){ visible = b; }
    
    public MicroSecondDate getFlagTime(){ return flagTime; }
    
    public void setFlagTime(MicroSecondDate flagTime) {
        this.flagTime = flagTime;
    }
    
    private Color color = Color.RED;
    
    private boolean visible = true;
    
    private MicroSecondDate flagTime;
    
    private String name;
    
    private DrawableSeismogram seis;
    
    //pixels of space of flag around the font
    private static final int PADDING = 4;
    
    private static Category logger = Category.getInstance(Flag.class.getName());
	
	private ToolTipManager tipManager = ToolTipManager.sharedInstance();
    
}// FlagPlotter
