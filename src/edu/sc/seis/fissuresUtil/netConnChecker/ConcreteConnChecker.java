package edu.sc.seis.fissuresUtil.netConnChecker;

import java.util.*;
/**
 * Description: This class implements the interface ConnChecker. 
 *
 *
 * Created: Fri May 10 13:41:34 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public abstract class ConcreteConnChecker implements ConnChecker{
    /**
     * Creates a new <code>ConcreteConnChecker</code> instance.
     *
     * @param description a <code>String</code> value
     */
    public ConcreteConnChecker (String description){
	statusChangeListeners = new Vector();
	this.description = description;
    }
    /**
     * returns true if the ConnectionStatus is FINISHED else false.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isFinished() {
	return finished;
    }
    /**
     * returns true if the ConnectionStatus is SUCCESSFUL else false.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isSuccessful() {
	return successful;
    }

    /**
     * returns true if the ConnectionStatus is UNKNOWN else false.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isUnknown() {
	return unknown;
    }

    /**
     * returns true if the ConnectionStatus is TRYING else false.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isTrying() {

	return trying;
    }
    
    /**
     * sets the value of finished.
     *
     * @param value a <code>boolean</code> value
     */
    public void setFinished(boolean value) {
	this.finished = value;
    }

    /**
     * sets the value of successful.
     *
     * @param value a <code>boolean</code> value
     */
    public void setSuccessful(boolean value) {

	this.successful = value;
    }

    /**
     * sets the value of unknown.
     *
     * @param value a <code>boolean</code> value
     */
    public void setUnknown(boolean value) {

	this.unknown = value;
    }
    
    /**
     * sets the value of trying.
     *
     * @param value a <code>boolean</code> value
     */
    public void setTrying(boolean value) {

	this.trying = value;
    }

    /**
     * returns the status of the ConnChecker
     *
     * @return a <code>ConnStatus</code> value
     */
    public ConnStatus getStatus() {

	return null;
    }

    /**
     * returns the Description of the ConnChecker.
     *
     * @return a <code>String</code> value
     */
    public String getDescription() {
	return this.description;
    }

    /**
     * adds a ConnStatusChangedListener to the list of ConnStatusChangedListeners.
     *
     * @param listener a <code>ConnStatusChangedListener</code> value
     */
    public void addConnStatusChangedListener(ConnStatusChangedListener listener) {
	statusChangeListeners.add(listener);
	System.out.println("The size of Listeners is after Adding is ************************** "+statusChangeListeners.size());
    }

    /**
     * removes a ConnStatusChangedListener from the list of ConnStatusChangedListeners.
     *
     * @param listener a <code>ConnStatusChangedListener</code> value
     */
    public void removeConnStatusChangedListener(ConnStatusChangedListener listener) {
	statusChangeListeners.remove(listener);
    }

    /**
     * fires a status changed Event to all the registered ConnStatusChangedListeners.
     *
     * @param urlStr a <code>String</code> value
     * @param connectionStatus a <code>ConnStatus</code> value
     */
    public synchronized void fireStatusChanged(String urlStr, ConnStatus connectionStatus) {
	System.out.println("The size of Listeners is "+statusChangeListeners.size());
	for(int counter = 0; counter < statusChangeListeners.size(); counter++) {

	    System.out.println("Function fireStatusChanged is invoked by "+urlStr+" ----> "+connectionStatus);

	    ConnStatusChangedListener listener = (ConnStatusChangedListener) statusChangeListeners.elementAt(counter);
	    listener.statusChanged(new StatusChangedEvent(this, urlStr, connectionStatus));
	}
    }
    
    private  String description;
    private boolean finished = false;
    private boolean successful = false;
    private boolean unknown = false;
    private boolean trying = true;
    private Vector statusChangeListeners = new Vector();
}// ConcreteConnChecker
