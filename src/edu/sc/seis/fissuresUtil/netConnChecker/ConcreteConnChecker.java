package edu.sc.seis.fissuresUtil.netConnChecker;

import java.util.Vector;

public abstract class ConcreteConnChecker implements ConnChecker{
    public ConcreteConnChecker (String description){
        statusChangeListeners = new Vector();
        this.description = description;
    }
    
    public void setFinished(boolean value) { this.finished = value; }
    
    public void setSuccessful(boolean value) { this.successful = value; }
    
    public void setUnknown(boolean value) {   this.unknown = value; }
    
    public void setTrying(boolean value) { this.trying = value;   }
    
    public ConnStatus getStatus() {
        if(successful) return ConnStatus.SUCCESSFUL;
        else if(unknown) return ConnStatus.UNKNOWN;
        else if(trying) return ConnStatus.TRYING;
        else return ConnStatus.FAILED;
    }
    
    public String getDescription() { return description; }
    
    public String toString() { return getDescription(); }
    
    public void addConnStatusChangedListener(ConnStatusChangedListener listener) {
        statusChangeListeners.add(listener);
    }
    
    public void removeConnStatusChangedListener(ConnStatusChangedListener listener) {
        statusChangeListeners.remove(listener);
    }
    
    public synchronized void fireStatusChanged(String urlStr, ConnStatus connectionStatus) {
        for(int counter = 0; counter < statusChangeListeners.size(); counter++) {
            ConnStatusChangedListener listener = (ConnStatusChangedListener) statusChangeListeners.elementAt(counter);
            listener.statusChanged(new StatusChangedEvent(this, urlStr, connectionStatus));
        }
    }
    
    private  String description;
    private boolean successful = false;
    private boolean unknown = false;
    private boolean trying = true;
    private boolean finished = false;
    private Vector statusChangeListeners = new Vector();
}// ConcreteConnChecker
