package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauP_Time;

/**
 * @author oliverpa Created on Aug 25, 2004
 */
public class PhasePhilter {

    public static Arrival[] filter(Arrival[] arrivals, TimeInterval offset) {
        LinkedList list = new LinkedList();
        for(int i = 0; i < arrivals.length; i++) {
            list.add(arrivals[i]);
        }
        for(int i = 0; i < list.size(); i++) {
            ListIterator it = list.listIterator(i);
            Arrival a = (Arrival)it.next(); // get first arrival for comparison
            while(it.hasNext()) {
                Arrival b = (Arrival)it.next();
                if(b.getName().equals(a.getName())
                        && Math.abs(b.getTime() - a.getTime()) < offset.convertTo(UnitImpl.SECOND).value) {
                    it.remove();
                }
            }
        }
        return (Arrival[])list.toArray(new Arrival[list.size()]);
    }

    public static Arrival findFirstP(Arrival[] arrivals) {
        List phases = TauP_Time.getPhaseNames("ttp");
        for(int i = 0; i < arrivals.length; i++) {
            if(phases.contains(arrivals[i].getName())) { return arrivals[i]; }
        }
        return null;
    }

    public static Arrival findFirstS(Arrival[] arrivals) {
        List phases = TauP_Time.getPhaseNames("tts");
        for(int i = 0; i < arrivals.length; i++) {
            if(phases.contains(arrivals[i].getName())) { return arrivals[i]; }
        }
        return null;
    }
    
}