package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLSeismogramAttr.java
 *
 *
 * Created: Mon Jul  1 15:07:32 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLSeismogramAttr {

    public static void insert(Element element, SeismogramAttr seismogramAttr) {
	Document doc = element.getOwnerDocument();
	element.appendChild(XMLUtil.createTextElement(doc,
						      "id",
						      seismogramAttr.get_id()));
	
	Element property;
	for(int counter = 0; counter < seismogramAttr.properties.length; counter++) {

	    property =  XMLProperty.createElement(doc, 
					   seismogramAttr.properties[counter],
					   "property");
	    element.appendChild(property);
	}

	Element begin_time = doc.createElement("begin_time");
	XMLTime.insert(begin_time, seismogramAttr.begin_time);
	element.appendChild(begin_time);

	element.appendChild(XMLUtil.createTextElement(doc,
						      "num_points",
						      ""+seismogramAttr.num_points));

	Element sampling_info = doc.createElement("sampling_info");
	XMLSampling.insert(sampling_info, seismogramAttr.sampling_info);
	element.appendChild(sampling_info);

	Element y_unit = doc.createElement("y_unit");
	XMLUnit.insert(y_unit, seismogramAttr.y_unit);
	element.appendChild(y_unit);

	Element channel_id = doc.createElement("channel_id");
	XMLChannelId.insert(channel_id, seismogramAttr.channel_id);
	element.appendChild(channel_id);

	Element parameter;
	for(int counter = 0; counter < seismogramAttr.parm_ids.length; counter++) {

	    parameter = doc.createElement("parameter");
	    XMLParameter.insert(parameter, 
				seismogramAttr.parm_ids[counter].a_id,
				seismogramAttr.parm_ids[counter].creator);
	    element.appendChild(parameter);
	}

	Element time_correction;
	for(int counter = 0; counter < seismogramAttr.time_corrections.length; counter++) {

	    time_correction = doc.createElement("time_correction");
	    XMLQuantity.insert(time_correction, seismogramAttr.time_corrections[counter]);
	    element.appendChild(time_correction);
	}

	Element sample_rate_history;
	for(int counter = 0; counter < seismogramAttr.sample_rate_history.length; counter++) {

	    sample_rate_history = doc.createElement("sample_rate_history");
	    XMLSampling.insert(sample_rate_history, seismogramAttr.sample_rate_history[counter]);
	    element.appendChild(sample_rate_history);
	}

    }
   
}// XMLSeismogramAttr
