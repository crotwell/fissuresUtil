package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.*;

import java.util.*;

public class EventQueue extends AbstractQueue {


	public EventQueue() {
		super();
		
	}

	public synchronized void push(java.lang.Object obj) {
		
	    while(list.size() == 4) {
		try {
		    wait();
		} catch(InterruptedException ie) { }
	    }
	    list.add(0, obj);
	    notifyAll();
	}

	public synchronized java.lang.Object pop() {
	    
	    while(list.size() == 0) {
		try {
		    wait();
		} catch(InterruptedException ie) { }

	    }
	    java.lang.Object obj = list.get(list.size()-1);
	    list.remove(list.size() - 1);
	    notifyAll();
	    return obj;

	}

	public synchronized int getLength() {
	
	    return list.size();
	
	}
	
    private List list = new LinkedList();
	
}
