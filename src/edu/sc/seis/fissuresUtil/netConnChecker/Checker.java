package edu.sc.seis.fissuresUtil.netConnChecker;

//import edu.sc.seis.sac.*;
import edu.sc.seis.fissuresUtil.exceptionHandler.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.apache.log4j.*;


/**
 * Description: The Checker class receives a Collection of ConnCheckerConfig
 * objects with the name, type of connection to perform and its associated
 * information, such as a website address in the case of an HTML connection or
 * a Corba object in the case of a Corba connection.
 * Each attempt to a connection runs in independent threads, the HTTPChecker
 * and CorbaChecker make the connection and update the fields in the
 * ConnCheckerConfig object of this collection.
 *
 * Checker.java
 */

public class Checker {


    /**  Constructor receives a collection of ConnCheckerConfig objects
     * @param  connCheckerCollectionReceived A Collection of ConncheckerConfig objects
     * @returns
     *
     */
    public Checker(Collection connCheckerCollectionReceived) {

        ConnCheckerCollection = connCheckerCollectionReceived;
        try{
            runChecks();
        }catch(IOException e){
            logger.warn("Could not perform makeChecksFromConfig");
        }
    }

    public Checker() {
    }

    public void runChecks() throws IOException {
        int sizeofCollection = ConnCheckerCollection.size();

        Iterator collectionExe = ConnCheckerCollection.iterator();

        while(collectionExe.hasNext()){
            ConnChecker connChecker =  (ConnChecker)collectionExe.next();
            Thread th = new Thread(checkerThreadGroup,
                                   connChecker,
                                   "ConnChecker"+getThreadNum());
            th.start();
        }

    } // close runChecks


    public int getNumFinished(){

        ConnChecker objfromcollection;
        Iterator collectionExe = ConnCheckerCollection.iterator();
        int sizeofCollection = ConnCheckerCollection.size();
        int totalfinished = 0;

        while(collectionExe.hasNext()){
            objfromcollection = (ConnChecker) collectionExe.next();
            if(objfromcollection.isFinished() == true){
                totalfinished+=1;
            }
        }

        logger.debug("\nTotal finished connections are: "+totalfinished+" from "+sizeofCollection);
        return totalfinished;

    }

    /** Returns a Collection of a list of successful connections.
     *
     */
    public Collection getSuccessfulConnections(){

        ConnChecker  objfromcollection;
        Iterator collectionExe = ConnCheckerCollection.iterator();
        boolean conn = false;
        Collection collectionofSuccessful=new LinkedList();

        logger.debug("\nThe succesful connections are: ");

        while(collectionExe.hasNext()){

            objfromcollection = (ConnChecker) collectionExe.next();

            if(objfromcollection.isSuccessful()){
                collectionofSuccessful.add(objfromcollection);
                //          logger.debug("Successful: " + objfromcollection.getName());
            }

        }//close while
        return collectionofSuccessful;

    }

    /**  Returns a Collection of a list of finished connections.
     *
     */
    public Collection getFinishedConnections(){

        ConnChecker objfromcollection;
        Iterator collectionExe = ConnCheckerCollection.iterator();
        boolean conn = false;
        Collection collectionofFinished=new LinkedList();
        logger.debug("\nThe finished connections are: ");
        while(collectionExe.hasNext()){
            objfromcollection = (ConnChecker) collectionExe.next();
            if(objfromcollection.isFinished()){
                collectionofFinished.add(objfromcollection);
                //  logger.debug("Finished: " + objfromcollection.getName());
            }
        }//close while
        return collectionofFinished;

    }

    /** Returns a Collection of a list of unsuccessful connections.
     *
     */
    public Collection  getUnsuccessfulConnections(){

        ConnChecker objfromcollection;
        Iterator collectionExe = ConnCheckerCollection.iterator();
        boolean conn = false;
        Collection collectionofFailed=new LinkedList();
        logger.debug("\nThe failed connections are: ");
        while(collectionExe.hasNext()){
            objfromcollection = (ConnChecker) collectionExe.next();
            if(objfromcollection.isFinished() && objfromcollection.isSuccessful() == false){
                collectionofFailed.add(objfromcollection);
                //   logger.debug("Failed: " + objfromcollection.getName());
            }
        }//close while
        return collectionofFailed;

    }

    /**  Returns a Collection of a list of unfinished connections.
     *
     */
    public Collection getUnfinishedConnections(){

        ConnChecker  objfromcollection;
        Iterator collectionExe = ConnCheckerCollection.iterator();
        boolean conn = false;
        Collection collectionofUnfinished=new LinkedList();
        logger.debug("\nThe unfinished connections are: ");
        while(collectionExe.hasNext()){
            objfromcollection = (ConnChecker) collectionExe.next();
            if(objfromcollection.isFinished() == false){
                collectionofUnfinished.add(objfromcollection);
                // logger.debug("Unfinished: " + objfromcollection.getName());
            }
        }//close while
        return collectionofUnfinished;
    }

    public java.util.HashMap getStatus() {

        ConnChecker objfromcollection;
        Iterator collectionExe = ConnCheckerCollection.iterator();
        boolean conn = false;
        java.util.HashMap hashMap = new java.util.HashMap();

        while(collectionExe.hasNext()){
            objfromcollection = (ConnChecker) collectionExe.next();
            conn = objfromcollection.isSuccessful();
            if(conn == true){
                hashMap.put(objfromcollection.getDescription(), ConnStatus.SUCCESSFUL);
            } else if(objfromcollection.isFinished() == true) {
                if(objfromcollection.isUnknown() == true) {
                    hashMap.put(objfromcollection.getDescription(), ConnStatus.UNKNOWN);
                }
                else {
                    hashMap.put(objfromcollection.getDescription(), ConnStatus.FAILED);
                }
            }  else {
                hashMap.put(objfromcollection.getDescription(), ConnStatus.TRYING);
            }
        }//close while
        // return collectionofFailed;
        return hashMap;
    }
    /***
     public void addHTTPConnection(HTTPCHe) {
     ***
     URL urltocheck = null;
     ConnCheckerConfig configobj;
     try {
     urltocheck = new URL(url);
     } catch(MalformedURLException mfue) {

     ExceptionHandlerGUI.handleException(mfue);
     return;
     }

     configobj = new HTTPConfig(description, urltocheck, this);
     ConnCheckerCollection.add(configobj);
     ConnChecker site = new HTTPChecker((HTTPConfig)configobj, this);
     Thread th = new Thread(site);
     th.start();
     System.out.println("Added the http connection "+description+" to the list of connections to check");
     ****

     }

     public void addCORBAConnection(String description,  org.omg.CORBA.Object object) {
     ***
     ConnCheckerConfig configobj;
     configobj = new CORBAConfig(description, object, this);
     ConnCheckerCollection.add(configobj);

     ConnChecker site = new CorbaChecker((CORBAConfig)configobj, this);
     Thread th = new Thread(site);
     th.start();
     System.out.println("Added the corba connection "+description+" to the list of connections to check");
     ***

     }**/

    public void addConnCheckerConnection(ConnChecker connChecker) {

        ConnCheckerCollection.add(connChecker);
        Thread th = new Thread(connChecker, "Conn Checker");
        th.start();

    }

    public synchronized void fireStatusChanged(String urlStr, ConnStatus connectionStatus) {

        /*
         for(int counter = 0; counter < statusChangeListeners.size(); counter++) {

         System.out.println("Function fireStatusChanged is invoked by "+urlStr+" ----> "+connectionStatus);

         ConnStatusChangedListener listener = (ConnStatusChangedListener) statusChangeListeners.elementAt(counter);
         listener.statusChanged(new StatusChangedEvent(this, urlStr, connectionStatus));

         }
         */
    }

    public void addConnStatusChangedListener(ConnStatusChangedListener listener) {

        Iterator collectionExe = ConnCheckerCollection.iterator();
        while(collectionExe.hasNext()){
            ConnChecker connChecker =  (ConnChecker)collectionExe.next();
            connChecker.addConnStatusChangedListener(listener);
        }
    }

    public void removeConnStatusChangedListener(ConnStatusChangedListener listener) {
        Iterator collectionExe = ConnCheckerCollection.iterator();
        while(collectionExe.hasNext()){
            ConnChecker connChecker =  (ConnChecker)collectionExe.next();
            connChecker.removeConnStatusChangedListener(listener);
        }
    }

    private Collection ConnCheckerCollection;/*LinkedList collection*/

    //private Vector statusChangeListeners = new Vector();

    static Category logger = Category.getInstance(Checker.class);


    private static int threadNum = 0;

    private synchronized static int getThreadNum() {
        return threadNum++;
    }

    private ThreadGroup checkerThreadGroup = new ThreadGroup("Connection Checker");


}// Checker class

/************************************************************/
