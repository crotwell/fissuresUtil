/**
 * DefaultExtractor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandler;

import edu.iris.Fissures.FissuresException;
import java.sql.SQLException;

public class DefaultExtractor implements Extractor {

    public boolean canExtract(Throwable throwable) {
        return true;
    }

    public String extract(Throwable throwable) {
        String traceString = "";
        if (throwable instanceof FissuresException) {
            traceString += "Description: "+((FissuresException)throwable).the_error.error_description+"\n";
            traceString += "Error Code: "+((FissuresException)throwable).the_error.error_code+"\n";
        }
        if(throwable instanceof SQLException){
            traceString += "SQLState: " + ((SQLException)throwable).getSQLState()+ '\n';
            traceString += "Vendor code: " + ((SQLException)throwable).getErrorCode() + '\n';
        }
        traceString += throwable.toString();
        return traceString;
    }

    public Throwable getSubThrowable(Throwable throwable) {
        return throwable.getCause();
    }

}

