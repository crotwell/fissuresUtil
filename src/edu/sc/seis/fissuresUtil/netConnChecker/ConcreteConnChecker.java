package edu.sc.seis.fissuresUtil.netConnChecker;

import java.util.*;
/**
 * ConcreteConnChecker.java
 *
 *
 * Created: Fri May 10 13:41:34 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public abstract class ConcreteConnChecker implements ConnChecker{
    public ConcreteConnChecker (String description){
	statusChangeListeners = new Vector();
	this.description = description;
    }
    public boolean isFinished() {
	return finished;
    }
    public boolean isSuccessful() {
	return successful;
    }

    public boolean isUnknown() {
	return unknown;
    }

    public boolean isTrying() {

	return trying;
    }
    
    public void setFinished(boolean value) {
	this.finished = value;
    }

    public void setSuccessful(boolean value) {

	this.successful = value;
    }

    public void setUnknown(boolean value) {

	this.unknown = value;
    }
    
    public void setTrying(boolean value) {

	this.trying = value;
    }

    public ConnStatus getStatus() {

	return null;
    }

    public String getDescription() {
	return this.description;
    }

    public void addConnStatusChangedListener(ConnStatusChangedListener listener) {
	statusChangeListeners.add(listener);
	System.out.println("The size of Listeners is after Adding is ************************** "+statusChangeListeners.size());
    }

    public void removeConnStatusChangedListener(ConnStatusChangedListener listener) {
	statusChangeListeners.remove(listener);
    }

    public synchronized void fireStatusChanged(String urlStr, ConnStatus connectionStatus) {
	System.out.println("The size of Listeners is "+statusChangeListeners.size());
	for(int counter = 0; counter < statusChangeListeners.size(); counter++) {

	    System.out.println("Function fireStatusChanged is invoked by "+urlStr+" ----> "+connectionStatus);

	    ConnStatusChangedListener listener = (ConnStatusChangedListener) statusChangeListeners.elementAt(counter);
	    listener.statusChanged(new StatusChangedEvent(this, urlStr, connectionStatus));
	}
    }
    protected String description;
    private boolean finished = false;
    private boolean successful = false;
    private boolean unknown = false;
    private boolean trying = true;
    private Vector statusChangeListeners = new Vector();
}// ConcreteConnChecker
