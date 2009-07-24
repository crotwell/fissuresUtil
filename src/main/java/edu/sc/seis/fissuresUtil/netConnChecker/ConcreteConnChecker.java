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

    public ConnStatusResult getStatus() {
        if(successful) return new ConnStatusResult(ConnStatus.SUCCESSFUL);
        else if(unknown) return new ConnStatusResult(ConnStatus.UNKNOWN);
        else if(trying) return new ConnStatusResult(ConnStatus.TRYING);
        else return new ConnStatusResult(ConnStatus.FAILED, reason, cause);
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
        ConnStatusResult result = new ConnStatusResult(connectionStatus, reason, cause);
        for(int counter = 0; counter < statusChangeListeners.size(); counter++) {
            ConnStatusChangedListener listener = (ConnStatusChangedListener) statusChangeListeners.elementAt(counter);
            listener.statusChanged(new StatusChangedEvent(this, urlStr, result));
        }
    }

    private  String description;
    protected String reason = "";
    protected Throwable cause = null;
    private boolean successful = false;
    private boolean unknown = false;
    private boolean trying = true;
    private boolean finished = false;
    private Vector statusChangeListeners = new Vector();
}// ConcreteConnChecker
