package edu.sc.seis.fissuresUtil.netConnChecker;

/**
 * Description: StatusChangedEvent is fired when ever the status of
 * ConnChecker is changed.
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
    public StatusChangedEvent (java.lang.Object checker, String urlStr, ConnStatusResult connectionStatus){

    setObject(checker);
    setConnStatus(connectionStatus);
    setURLStr(urlStr);

    }

    /**
     * sets the source object whose status is changed.
     *
     * @param checker a <code>java.lang.Object</code> value
     */
    public void setObject(java.lang.Object checker) {

    this.checker = checker;

    }

    /**
     * sets the Connection status for this Event.
     *
     * @param connectionStatus a <code>ConnStatus</code> value
     */
    public void setConnStatus(ConnStatusResult connectionStatus) {

    this.connectionStatus = connectionStatus;

    }

    /**
     * sets the URL that is checked for ConnectionStatus.
     *
     * @param urlStr a <code>String</code> value
     */
    public void setURLStr(String urlStr) {

    this.urlStr = urlStr;

    }

    /**
     * returns the Source Object whose status is changed.
     *
     * @return an <code>Object</code> value
     */
    public Object getObject() {

    return checker;

    }

    /**
     * returns the ConnStatus.
     *
     * @return a <code>ConnStatus</code> value
     */
    public ConnStatusResult getConnStatus() {


    return connectionStatus;

    }

    /**
     * returns the URLString.
     *
     * @return a <code>String</code> value
     */
    public String getURLStr() {

    return urlStr;

    }

    private java.lang.Object checker;

    private ConnStatusResult connectionStatus;

    private String urlStr;

}// StatusChangedEvent
