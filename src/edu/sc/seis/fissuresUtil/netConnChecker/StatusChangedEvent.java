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

    /**
     * Creates a new <code>StatusChangedEvent</code> instance.
     *
     * @param checker a <code>java.lang.Object</code> value
     * @param urlStr a <code>String</code> value
     * @param connectionStatus a <code>ConnStatus</code> value
     */
    public StatusChangedEvent (java.lang.Object checker, String urlStr, ConnStatus connectionStatus){

	setObject(checker);
	setConnStatus(connectionStatus);
	setURLStr(urlStr);
	
    }

    /**
     * Describe <code>setObject</code> method here.
     *
     * @param checker a <code>java.lang.Object</code> value
     */
    public void setObject(java.lang.Object checker) {

	this.checker = checker;

    }

    /**
     * Describe <code>setConnStatus</code> method here.
     *
     * @param connectionStatus a <code>ConnStatus</code> value
     */
    public void setConnStatus(ConnStatus connectionStatus) {

	this.connectionStatus = connectionStatus;

    }

    /**
     * Describe <code>setURLStr</code> method here.
     *
     * @param urlStr a <code>String</code> value
     */
    public void setURLStr(String urlStr) {

	this.urlStr = urlStr;

    }

    /**
     * Describe <code>getObject</code> method here.
     *
     * @return an <code>Object</code> value
     */
    public Object getObject() {

	return checker;

    }

    /**
     * Describe <code>getConnStatus</code> method here.
     *
     * @return a <code>ConnStatus</code> value
     */
    public ConnStatus getConnStatus() {


	return connectionStatus;

    }

    /**
     * Describe <code>getURLStr</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getURLStr() {

	return urlStr;

    }

    private java.lang.Object checker;
    
    private ConnStatus connectionStatus;

    private String urlStr;
    
}// StatusChangedEvent
