/**
 * ConnStatusResult.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.netConnChecker;

public class ConnStatusResult
{

    public ConnStatusResult(ConnStatus status) {
        this.status = status;
    }

    public ConnStatusResult(ConnStatus status, String reason) {
        this(status);
        this.reason = reason;
    }

    public ConnStatusResult(ConnStatus status, String reason, Throwable cause) {
        this(status, reason);
        this.cause = cause;
    }

    public ConnStatus getStatus() {
        return status;
    }

    /** may be zero length string if there was no failure in the result. */
    public String getReason() {
        return reason;
    }

    /** may be null if there was no Throwable in the result. */
    public Throwable getCause() {
        return cause;
    }

    ConnStatus status;

    String reason;

    Throwable cause;

}

