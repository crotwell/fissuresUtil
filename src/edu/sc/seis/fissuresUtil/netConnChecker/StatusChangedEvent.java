package edu.sc.seis.fissuresUtil.netConnChecker;

/**
 * StatusChangedEvent.java
 *
 *
 * Created: Wed Jan 30 11:07:29 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class StatusChangedEvent {
    public StatusChangedEvent (Checker checker, String urlStr, ConnStatus connectionStatus){

	setChecker(checker);
	setConnStatus(connectionStatus);
	setURLStr(urlStr);
	
    }

    public void setChecker(Checker checker) {

	this.checker = checker;

    }

    public void setConnStatus(ConnStatus connectionStatus) {

	this.connectionStatus = connectionStatus;

    }

    public void setURLStr(String urlStr) {

	this.urlStr = urlStr;

    }

    public Checker getChecker() {

	return checker;

    }

    public ConnStatus getConnStatus() {


	return connectionStatus;

    }

    public String getURLStr() {

	return urlStr;

    }

    private Checker checker;
    
    private ConnStatus connectionStatus;

    private String urlStr;
    
}// StatusChangedEvent
