/**
 * UpdateCheckerJob.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.fissuresUtil.chooser;

import com.isti.util.updatechecker.LocationUpdate;
import com.isti.util.updatechecker.UpdateAction;
import com.isti.util.updatechecker.UpdateInformation;
import com.isti.util.updatechecker.XMLUpdateCheckerClient;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.AbstractJob;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

public class UpdateCheckerJob  extends AbstractJob {
    public UpdateCheckerJob(String displayName, String programName, String version, String updateURL, boolean gui) {
        this(displayName, programName, version, updateURL, gui, false);
    }

    public UpdateCheckerJob(String displayName, String programName, String version, String updateURL, boolean gui, boolean forceCheck) {
        super(displayName);
        this.programName = programName;
        this.updateURL = updateURL;
        this.isGui = gui;
        this.forceCheck = forceCheck;
        this.version = version;
        prefs = Preferences.userNodeForPackage(this.getClass());
    }

    public void run() {
        // only check if have not yet checked, or if forceCheck is true
        if ( !forceCheck && checkedYet) {
            setFinished();
            return;
        }
        checkedYet = true;

        boolean checkNeeded = true;
        prefs = prefs.node("UpdateCheckerTask");
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                if (keys[i].equals(getName()+"/"+NEXT_CHECK_DATE)) {
                    String nextCheckDate = prefs.get(getName()+"/"+NEXT_CHECK_DATE, "");
                    MicroSecondDate date = new MicroSecondDate(new Time(nextCheckDate, -1));
                    MicroSecondDate now = ClockUtil.now();
                    if (date.after(now) && ! forceCheck) {
                        // don't check
                        logger.debug("no updated wanted until "+date);
                        setFinished(true);
                        return;
                    }
                }
            }
        } catch (BackingStoreException e) {
            GlobalExceptionHandler.handle("Trouble getting prefereces for update checker.", e);
        }
        setStatus("Connect to server");

        XMLUpdateCheckerClient updateChecker =
            new XMLUpdateCheckerClient(version,
                                       updateURL);
        setStatus("Check for update");
        if (updateChecker.isUpdateAvailable()) {
            UpdateInformation[] updates = updateChecker.getUpdates();
            logger.info("our version is "+version+", update version is "+updates[updates.length-1].getVersion());
            UpdateAction[] actions = updates[updates.length-1].getUpdateActions();
            LocationUpdate locationUpdate = (LocationUpdate)actions[0];
            if (isGui) {
                handleUpdateGUI(locationUpdate);
            } else {
                handleUpdateNonGUI(locationUpdate);
            }
        }else if(showNoUpdate) {
            JOptionPane.showMessageDialog(null,
                                          "No update is available",
                                          "Update Check",
                                          JOptionPane.INFORMATION_MESSAGE);
            logger.info("No update is available");
        }
        setFinished();
    }

    protected void handleUpdateGUI(LocationUpdate locationUpdate) {
        Object[] options = new String[3];
        options[0] = "Go To Update Page";
        options[1] = "Remind in a fortnight";
        options[2] = "Remind in a month";
        int n = JOptionPane.showOptionDialog(null,
                                             "An updated version of "+programName+" is available!\nPlease go to\n"+locationUpdate.getLocation()+"\nto get the latest version.",
                                             "An updated version of "+programName+" is available!",
                                             JOptionPane.YES_NO_OPTION,
                                             JOptionPane.QUESTION_MESSAGE,
                                             null,     //don't use a custom Icon
                                             options,  //the titles of buttons
                                             options[0]); //default button title
        logger.debug("return val is "+n);
        TimeInterval nextInterval = new TimeInterval(6, UnitImpl.HOUR);
        if (n == JOptionPane.YES_OPTION) {
            logger.debug("Opening browser");
            setStatus("Opening browser");
            locationUpdate.run();

        } else if (n == 1) {
            nextInterval = new TimeInterval(14, UnitImpl.DAY);
        } else if (n == 2) {
            nextInterval = new TimeInterval(30, UnitImpl.DAY);
        }
        MicroSecondDate nextCheck = ClockUtil.now().add(nextInterval);
        prefs.put(getName()+"/"+NEXT_CHECK_DATE, nextCheck.getFissuresTime().date_time);
        logger.debug("no update check wanted for "+nextInterval+", next at "+nextCheck);
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            GlobalExceptionHandler.handle("trouble flushing preferences for updatechecker", e);
        }
    }

    protected void handleUpdateNonGUI(LocationUpdate locationUpdate) {
        System.out.println("*******************************************************");
        System.out.println();
        System.out.println("An updated version of "+programName+" is available!");
        System.out.println("Please go to");
        System.out.println();
        System.out.println(locationUpdate.getLocation());
        System.out.println();
        System.out.println("to get the latest version.");
        System.out.println();
        System.out.println();
        System.out.println("Description: "+locationUpdate.getDescription());
        System.out.println();
        System.out.println("*******************************************************");
    }

    protected String version;

    protected String programName;

    protected boolean forceCheck;

    protected boolean showNoUpdate = true;

    protected boolean isGui;

    static boolean checkedYet = false;

    protected String updateURL;

    protected Preferences prefs;

    static final String NEXT_CHECK_DATE = "nextCheckDate";

    private static final Logger logger = Logger.getLogger(UpdateCheckerJob.class);

}

