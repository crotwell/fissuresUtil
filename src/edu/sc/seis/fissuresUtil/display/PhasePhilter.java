package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.util.ListIterator;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;


/**
 * @author oliverpa
 * Created on Aug 25, 2004
 */
public class PhasePhilter {
    
    public static Arrival[] filter(Arrival[] arrivals, TimeInterval offset) {
        LinkedList list = new LinkedList();
        for (int i = 0; i < arrivals.length; i++) {
            list.add(arrivals[i]);
        }
        for (int i = 0; i < list.size(); i++) {
            ListIterator it = list.listIterator(i);
            Arrival a = (Arrival)it.next(); // get first arrival for comparison
            while (it.hasNext()) {
                Arrival b = (Arrival)it.next();
                if (b.getName().equals(a.getName()) &&
                    Math.abs(b.getTime() - a.getTime()) < offset.convertTo(UnitImpl.SECOND).value) {
                    it.remove();
                }
            }
        }
        return (Arrival[])list.toArray(new Arrival[list.size()]);
    }
    
}
