package edu.sc.seis.fissuresUtil.netConnChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: This class checks for HTTP connections. An HTTPChecker can be instantiated by
 * passing the parameters description and url
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class HTTPChecker extends ConcreteConnChecker  {
    public HTTPChecker(String description, String url){
        super(description);
        this.url = url;
    }// constructor

    public void run ()  {
        try{
            URL seis = new URL(this.url);
            URLConnection seisConnection = seis.openConnection();
            InputStreamReader buffer = new InputStreamReader(seisConnection.getInputStream());
            BufferedReader bufferread = new BufferedReader(buffer);
            setTrying(false);
            setFinished(true);
            setSuccessful(true);
            fireStatusChanged(getDescription(), ConnStatus.SUCCESSFUL);
        } catch (MalformedURLException urle) {
            cause = urle;
            setTrying(false);
            setFinished(true);
            setSuccessful(false);
            setUnknown(true);
            fireStatusChanged(getDescription(), ConnStatus.UNKNOWN);
        } catch (IOException ioe) {
            cause=ioe;
            setTrying(false);
            setFinished(true);
            setSuccessful(false);
            fireStatusChanged(getDescription(), ConnStatus.FAILED);
        } catch(Exception e) {
            cause=e;
            setTrying(false);
            setFinished(true);
            setSuccessful(false);
            fireStatusChanged(getDescription(), ConnStatus.FAILED);
        }
    } // run

    public String getURL(){ return url; }

    private String url;
    
    private static Logger logger = LoggerFactory.getLogger(HTTPChecker.class);

} // HTTPChecker class
