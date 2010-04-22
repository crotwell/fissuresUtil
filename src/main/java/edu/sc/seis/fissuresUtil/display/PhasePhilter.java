package edu.sc.seis.fissuresUtil.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauP_Time;

/**
 * @author oliverpa Created on Aug 25, 2004
 */
public class PhasePhilter {

    public static class PhaseRenamer {

        public PhaseRenamer() {}

        public PhaseRenamer(Node n) throws TransformerException {
            NodeList nl = XPathAPI.selectNodeList(n, "mapping");
            matchers = new Pattern[nl.getLength()];
            replacements = new String[nl.getLength()];
            for(int i = 0; i < nl.getLength(); i++) {
                Element el = (Element)nl.item(i);
                matchers[i] = Pattern.compile(extractValue(el, "pattern/text()"));
                replacements[i] = extractValue(el, "replacement/text()");
            }
        }

        public PhaseRenamer(Pattern[] patterns, String[] replacements) {
            this.matchers = patterns;
            this.replacements = replacements;
        }

        private static String extractValue(Element el, String xpath)
                throws TransformerException {
            return XPathAPI.selectSingleNode(el, xpath).getNodeValue();
        }

        public String rename(Arrival a) {
            for(int i = 0; i < matchers.length; i++) {
                if(matchers[i].matcher(a.getName()).find()) {
                    return replacements[i];
                } 
            }
            return a.getName();
        }

        private Pattern[] matchers = new Pattern[0];

        private String[] replacements = new String[0];
    }

    public static List<Arrival> filter(List<Arrival> arrivals, TimeInterval offset) {
        LinkedList<Arrival> list = new LinkedList<Arrival>();
        for (Arrival a : arrivals) {
            list.add(a);
        }
        for(int i = 0; i < list.size(); i++) {
            ListIterator<Arrival> it = list.listIterator(i);
            Arrival a = (Arrival)it.next(); // get first arrival for comparison
            while(it.hasNext()) {
                Arrival b = (Arrival)it.next();
                if(b.getName().equals(a.getName())
                        && Math.abs(b.getTime() - a.getTime()) < offset.convertTo(UnitImpl.SECOND).value) {
                    it.remove();
                }
            }
        }
        return list;
    }

    /**
     * @returns an arrival array containing only first p, first s and all other
     *          arrival from the input arrivals
     */
    public static List<Arrival> mindPsAndSs(List<Arrival> arrivals) {
        List<String> pPhases = TauP_Time.getPhaseNames("ttp");
        List<String> sPhases = TauP_Time.getPhaseNames("tts");
        boolean foundP = false, foundS = false;
        List<Arrival> resultantArrivals = new ArrayList<Arrival>();
        for (Arrival arrival : arrivals) {
            if(pPhases.contains(arrival.getName())) {
                if(!foundP) {
                    resultantArrivals.add(arrival);
                    foundP = true;
                }
            } else if(sPhases.contains(arrival.getName())) {
                if(!foundS) {
                    resultantArrivals.add(arrival);
                    foundS = true;
                }
            } else {
                resultantArrivals.add(arrival);
            }
        }
        return resultantArrivals;
    }
}