package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.*;

import java.util.*;

public class EventQueue extends AbstractQueue {


    public EventQueue() {
	super();
		
    }

    public synchronized void push(java.lang.Object obj) {
		
	while(list.size() == 24) {
	    try {
		System.out.println("Watiting in PUSH");
		wait();
	    } catch(InterruptedException ie) { }
	}
	list.add(0, obj);
	notifyAll();
    }

    public synchronized java.lang.Object pop() {
	
	while(list.size() == 0 && sourceAlive == true) {
	    try {
		System.out.println("Waiting in POP");
		wait();
	    } catch(InterruptedException ie) { }

	}
	if(list.size() == 0) return null;
	java.lang.Object obj = list.get(list.size()-1);
	list.remove(list.size() - 1);
	notifyAll();
	return obj;

    }

    public synchronized int getLength() {
	
	return list.size();
	
    }

    public synchronized void setSourceAlive(boolean value) {

	this.sourceAlive = value;
	notifyAll();
    }
    
    public synchronized boolean getSourceAlive() {
	return this.sourceAlive;
    }
	
    private boolean sourceAlive = true;

    private List list = new LinkedList();
	
}
