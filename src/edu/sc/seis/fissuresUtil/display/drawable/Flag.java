package edu.sc.seis.fissuresUtil.display.drawable;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.TextTable;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	
	public static TextTable getFlagData(DataSetSeismogram dss,
										EventAccessOperations event,
										String[] template){
		String[] header = getFlagDataHeader(template);
		TextTable table = new TextTable(header.length, true);
		Iterator it = dss.getAuxillaryDataKeys().iterator();
		while (it.hasNext()){
			String cur = (String)it.next();
			List dataCells = new ArrayList();
			if (cur.startsWith(StdAuxillaryDataNames.PICK_FLAG)){
				Flag flag = getFlagFromElement((Element)dss.getAuxillaryData(cur));
				for (int i = 0; i < template.length; i++) {
					if (template[i].equals(NAME)){ //Flag Name
						dataCells.add(flag.getName());
					}
					else if (template[i].equals(TIME)){ //Flag Time
						dataCells.add(flag.getFlagTime().toString());
					}
					else if (template[i].equals(CHANNEL)){ //Channel Id
						ChannelId chanId = dss.getRequestFilter().channel_id;
						dataCells.add(chanId.network_id.network_code
										  + '.'
										  + chanId.station_code
										  + '.'
										  + chanId.site_code
										  + '.'
										  + chanId.channel_code);
					}
					else if (template[i].equals(EVENT_NAME)){ //Event Name
						dataCells.add(getEventInfo(event, CacheEvent.LOC));
					}
					else if (template[i].equals(EVENT_MAG)){ //Event Magnitude
						dataCells.add(CacheEvent.getEventInfo(event, CacheEvent.MAG));
					}
					else if (template[i].equals(EVENT_ORIG)){ //Event Origin Time
						dataCells.add(CacheEvent.getEventInfo(event, CacheEvent.TIME));
					}
					else if (template[i].equals(EVENT_DEPTH)){ //Event Depth
						dataCells.add(CacheEvent.getEventInfo(event, CacheEvent.DEPTH
																  + ' '
																  + CacheEvent.DEPTH_UNIT));
					}
					else if (template[i].equals(EVENT_LAT)){ //Event Latitude
						dataCells.add(CacheEvent.getEventInfo(event, CacheEvent.LAT));
					}
					else if (template[i].equals(EVENT_LON)){ //Event Longitude
						dataCells.add(CacheEvent.getEventInfo(event, CacheEvent.LON));
					}else if (template[i].equals(ORIGIN_DIFF)){ //flagTime-originTime
						dataCells.add(formatTimeInterval(getTimeDifferenceFromOrigin(flag, event)));
					}else if (template[i].equals(DISTANCE_FROM_ORIG)){ //Distance from Origin, if that wasn't obvious
						//dataCells.add();
					}
				}
				table.addRow(((String[])dataCells.toArray(new String[0])));
			}
		}
		return table;
	}
	
	private static String getEventInfo(EventAccessOperations event, String format){
		if (event.get_origins().length > 0){
			return CacheEvent.getEventInfo(event, format);
		}
		return "...";
	}
	
	//There definitely won't be a great need for this method once
	//possible data options are implemented, but until then, this
	//is here to weed out the weirdness.
	public static String[] getFlagDataHeader(String[] template){
		List dataCells = new ArrayList();
		for (int i = 0; i < template.length; i++) {
			if (template[i].equals(NAME)){ //Flag Name
				dataCells.add(NAME);
			}
			else if (template[i].equals(TIME)){ //Flag Time
				dataCells.add(TIME);
			}
			else if (template[i].equals(CHANNEL)){ //Channel Id
				dataCells.add(CHANNEL);
			}
			else if (template[i].equals(EVENT_NAME)){ //Event Name
				dataCells.add(EVENT_NAME);
			}
			else if (template[i].equals(EVENT_MAG)){ //Event Magnitude
				dataCells.add(EVENT_MAG);
			}
			else if (template[i].equals(EVENT_ORIG)){ //Event Origin Time
				dataCells.add(EVENT_ORIG);
			}
			else if (template[i].equals(EVENT_DEPTH)){ //Event Depth
				dataCells.add(EVENT_DEPTH);
			}
			else if (template[i].equals(EVENT_LAT)){ //Event Latitude
				dataCells.add(EVENT_LAT);
			}
			else if (template[i].equals(EVENT_LON)){ //Event Longitude
				dataCells.add(EVENT_LON);
			}else if (template[i].equals(ORIGIN_DIFF)){ //flagTime-originTime
				dataCells.add(ORIGIN_DIFF);
			}else if (template[i].equals(DISTANCE_FROM_ORIG)){ //Distance from Origin, if that wasn't obvious
				//dataCells.add(DISTANCE_FROM_ORIG);
			}
		}
		return (String[])dataCells.toArray(new String[0]);
	}
	
	public static String formatTimeInterval(TimeInterval interval){
		if (interval != null){
			return interval.toString() + " " + interval.the_units.name;
		}
		return "...";
	}
	
	public static TimeInterval getTimeDifferenceFromOrigin(Flag flag, EventAccessOperations event){
		if (event.get_origins().length > 0){
			MicroSecondDate originTime = new MicroSecondDate(event.get_origins()[0].origin_time);
			MicroSecondDate flagTime = flag.getFlagTime();
			return originTime.difference(flagTime);
		}
		return null;
	}
	
    public String getName(){ return name; }
	
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
	
	//names for the data template
	public static final String NAME = "Flag Name";
	public static final String ORIGIN_DIFF = "Time from Origin"; //flag time minus origin time
	public static final String TAUP_P = "TauP P Wave"; //todo
	public static final String TIME_DIFF_ORIG_P = "FLAG_TIME_DIFF_ORIG_PWAVE"; //todo
	public static final String DISTANCE_FROM_ORIG = "Distance From Origin"; //todo
	public static final String BACK_AZIMUTH = "Back Azimuth"; //todo
	public static final String CHANNEL = "Channel";
	public static final String EVENT_NAME = "Event Name";
	public static final String EVENT_LAT = "Event Latitude";
	public static final String EVENT_LON = "Event Longitude";
	public static final String EVENT_DEPTH = "Event Depth";
	public static final String EVENT_MAG = "Magnitude";
	public static final String EVENT_ORIG = "Origin Time";
	public static final String TIME = "Flag Time";
	
	
}// FlagPlotter

