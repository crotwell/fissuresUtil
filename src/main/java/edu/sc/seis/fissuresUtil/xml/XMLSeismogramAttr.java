package edu.sc.seis.fissuresUtil.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.IfSeismogramDC.Property;
import edu.iris.Fissures.IfSeismogramDC.SeismogramAttr;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;

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

    /**
     * StAX insert
     */
    public static void insert(XMLStreamWriter writer, SeismogramAttr seismogramAttr)
        throws XMLStreamException{

        XMLUtil.writeTextElement(writer, "id", seismogramAttr.get_id());

        for (int i = 0; i < seismogramAttr.properties.length; i++) {
            writer.writeStartElement("dataset");
            XMLProperty.insert(writer, seismogramAttr.properties[i]);
            XMLUtil.writeEndElementWithNewLine(writer);
        }

        writer.writeStartElement("begin_time");
        XMLTime.insert(writer, seismogramAttr.begin_time);
        XMLUtil.writeEndElementWithNewLine(writer);

        XMLUtil.writeTextElement(writer, "num_points", ""+seismogramAttr.num_points);

        writer.writeStartElement("sampling_info");
        XMLSampling.insert(writer, seismogramAttr.sampling_info);
        XMLUtil.writeEndElementWithNewLine(writer);

        writer.writeStartElement("y_unit");
        XMLUnit.insert(writer, seismogramAttr.y_unit);
        XMLUtil.writeEndElementWithNewLine(writer);

        writer.writeStartElement("channel_id");
        XMLChannelId.insert(writer, seismogramAttr.channel_id);
        XMLUtil.writeEndElementWithNewLine(writer);

        for (int i = 0; i < seismogramAttr.parm_ids.length; i++) {
            writer.writeStartElement("parameter");
            XMLParameter.insert(writer,
                                seismogramAttr.parm_ids[i].a_id,
                                seismogramAttr.parm_ids[i].creator);
            XMLUtil.writeEndElementWithNewLine(writer);
        }

        for (int i = 0; i < seismogramAttr.time_corrections.length; i++) {
            writer.writeStartElement("time_correction");
            XMLQuantity.insert(writer, seismogramAttr.time_corrections[i]);
            XMLUtil.writeEndElementWithNewLine(writer);
        }

        for (int i = 0; i < seismogramAttr.sample_rate_history.length; i++) {
            writer.writeStartElement("sample_rate_history");
            XMLSampling.insert(writer, seismogramAttr.sample_rate_history[i]);
            XMLUtil.writeEndElementWithNewLine(writer);
        }
    }

    /**
     * DOM insert
     */
    public static void insert(Element element, SeismogramAttr seismogramAttr) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "id",
                                                      seismogramAttr.get_id()));

        for(int counter = 0; counter < seismogramAttr.properties.length; counter++) {
            if(seismogramAttr.properties[counter] != null) {
                Element child = doc.createElement("dataset");
                XMLProperty.insert(child, seismogramAttr.properties[counter]);
                element.appendChild(child);
            }
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

    public synchronized static SeismogramAttr getSeismogramAttr(Element base) {
        String id = XMLUtil.getText(XMLUtil.getElement(base, "id"));
        //System.out.println("The id of the SeismogramAttr is "+id);

        //Get the Properties.
        Element[] property = XMLUtil.getElementArray(base, "property");
        Property[] properties = new Property[0];
        if(property != null && property.length != 0) {
            properties = new Property[property.length];
            for(int counter = 0; counter < property.length; counter++) {
                properties[counter] = XMLProperty.getProperty(property[counter]);
            }
        }


        //Get the begin Time.
        edu.iris.Fissures.Time begin_time = new edu.iris.Fissures.Time();
        Element begin_time_node = XMLUtil.getElement(base, "begin_time");
        if(begin_time_node != null) {
            begin_time = XMLTime.getFissuresTime(begin_time_node);
        }

        //get num_points

        int num_points = Integer.parseInt(XMLUtil.getText(XMLUtil.getElement(base, "num_points")));

        //Get the sampling_info
        Sampling sampling_info = null;
        Element sampling_info_node = XMLUtil.getElement(base, "sampling_info");
        if(sampling_info_node != null) {
            sampling_info = XMLSampling.getSampling(sampling_info_node);
        }

        //get the y_unit

        Unit  y_unit = null;
        Element y_unit_node = XMLUtil.getElement(base, "y_unit");
        if(y_unit_node != null) {
            y_unit = XMLUnit.getUnit(y_unit_node);
        }

        //get the channel_id
        ChannelId channel_id = null;
        Element channel_id_node = XMLUtil.getElement(base, "channel_id");
        if(channel_id_node != null) {

            channel_id = XMLChannelId.getChannelId(channel_id_node);
        }

        //get the parameters
        ParameterRef[] parm_ids = new ParameterRef[0];
        Element[] params = XMLUtil.getElementArray(base, "parameter");
        if(params != null && params.length != 0) {
            parm_ids = new ParameterRef[params.length];
            for(int counter = 0; counter < params.length; counter++) {
                parm_ids[counter] = (ParameterRef)XMLParameter.getParameter(params[counter]);
            }
        }

        //get the time_corrections
        Quantity[] time_corrections = new Quantity[0];
        Element[] time_corrections_list = XMLUtil.getElementArray(base, "time_correction");
        if(time_corrections_list != null && time_corrections_list.length != 0) {
            time_corrections = new Quantity[time_corrections_list.length];
            for(int counter = 0; counter < time_corrections_list.length; counter++) {

                time_corrections[counter] = XMLQuantity.getQuantity(time_corrections_list[counter]);
            }
        }

        //get the sample_rate_history
        Sampling[] sample_rate_history = new Sampling[0];
        Element[] sample_rate_history_list = XMLUtil.getElementArray(base, "sample_rate_history");
        if(sample_rate_history_list != null && sample_rate_history_list.length != 0) {
            sample_rate_history = new Sampling[sample_rate_history_list.length];
            for(int counter = 0; counter < sample_rate_history_list.length; counter++) {

                sample_rate_history[counter] = XMLSampling.getSampling(sample_rate_history_list[counter]);
            }

        }

        return new SeismogramAttrImpl(id,
                                      properties,
                                      begin_time,
                                      num_points,
                                      sampling_info,
                                      y_unit,
                                      channel_id,
                                      parm_ids,
                                      time_corrections,
                                      sample_rate_history);

    }

    //  public synchronized static SeismogramAttr getSeismogramAttr(Element base) {
    //  String id = XMLUtil.evalString(base, "id");
    //  //System.out.println("The id of the SeismogramAttr is "+id);

    //  //Get the Properties.
    //  NodeList property = XMLUtil.evalNodeList(base, "property");
    //  Property[] properties = new Property[0];
    //  if(property != null && property.getLength() != 0) {
    //      properties = new Property[property.getLength()];
    //      for(int counter = 0; counter < property.getLength(); counter++) {
    //      properties[counter] = XMLProperty.getProperty((Element)property.item(counter));
    //      }
    //  }


    //  //Get the begin Time.
    //  edu.iris.Fissures.Time begin_time = new edu.iris.Fissures.Time();
    //  NodeList begin_time_node = XMLUtil.evalNodeList(base, "begin_time");
    //  if(begin_time_node != null && begin_time_node.getLength() != 0) {
    //      begin_time = XMLTime.getFissuresTime((Element)begin_time_node.item(0));
    //  }

    //  //get num_points

    //  int num_points = Integer.parseInt(XMLUtil.evalString(base, "num_points"));

    //  //Get the sampling_info
    //  Sampling sampling_info = null;
    //  NodeList sampling_info_node = XMLUtil.evalNodeList(base, "sampling_info");
    //  if(sampling_info_node != null && sampling_info_node.getLength() != 0) {
    //      sampling_info = XMLSampling.getSampling((Element)sampling_info_node.item(0));
    //  }

    //  //get the y_unit

    //  Unit  y_unit = null;
    //  NodeList y_unit_node = XMLUtil.evalNodeList(base, "y_unit");
    //  if(y_unit_node != null && y_unit_node.getLength() != 0 ) {
    //      y_unit = XMLUnit.getUnit((Element)y_unit_node.item(0));
    //  }

    //  //get the channel_id
    //  ChannelId channel_id = null;
    //  NodeList channel_id_node = XMLUtil.evalNodeList(base, "channel_id");
    //  if(channel_id_node != null && channel_id_node.getLength() != 0) {

    //      channel_id = XMLChannelId.getChannelId((Element)channel_id_node.item(0));
    //  }

    //  //get the parameters
    //  ParameterRef[] parm_ids = new ParameterRef[0];
    //  NodeList params = XMLUtil.evalNodeList(base, "parameter");
    //  if(params != null && params.getLength() != 0) {
    //      for(int counter = 0; counter < params.getLength(); counter++) {
    //      parm_ids[counter] = (ParameterRef)XMLParameter.getParameter((Element)params.item(counter));
    //      }
    //  }

    //  //get the time_corrections
    //  Quantity[] time_corrections = new Quantity[0];
    //  NodeList time_corrections_list = XMLUtil.evalNodeList(base, "time_correction");
    //  if(time_corrections_list != null && time_corrections_list.getLength() != 0) {
    //      for(int counter = 0; counter < time_corrections_list.getLength(); counter++) {

    //      time_corrections[counter] = XMLQuantity.getQuantity((Element)time_corrections_list.item(counter));
    //      }
    //  }

    //  //get the sample_rate_history
    //  Sampling[] sample_rate_history = new Sampling[0];
    //  NodeList sample_rate_history_list = XMLUtil.evalNodeList(base, "sample_rate_history");
    //  if(sample_rate_history_list != null && sample_rate_history_list.getLength() != 0) {

    //      for(int counter = 0; counter < sample_rate_history_list.getLength(); counter++) {

    //      sample_rate_history[counter] = XMLSampling.getSampling((Element)sample_rate_history_list.item(0));
    //      }

    //  }

    //  return new SeismogramAttrImpl(id,
    //                    properties,
    //                    begin_time,
    //                    num_points,
    //                    sampling_info,
    //                    y_unit,
    //                    channel_id,
    //                    parm_ids,
    //                    time_corrections,
    //                    sample_rate_history);

    //     }
}// XMLSeismogramAttr
